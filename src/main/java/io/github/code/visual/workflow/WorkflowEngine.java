/*
 * Copyright (c) 2023-2024, levi li (levi.lideng@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.code.visual.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import io.github.code.visual.config.VisualFlowProperties;
import io.github.code.visual.model.DebugRequest;
import io.github.code.visual.model.Diagnostic;
import io.github.code.visual.model.ScriptMetadata;
import io.github.code.visual.model.ScriptRunStatus;
import io.github.code.visual.model.ScriptType;
import io.github.code.visual.model.WorkflowMetadata;
import io.github.code.visual.model.WorkflowTaskLog;
import io.github.code.visual.ruleengine.Rule;
import io.github.code.visual.ruleengine.RuleEngine;
import io.github.code.visual.utils.CommonUtils;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.runtime.EncodingGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Core workflow execution engine.
 * <p>
 * This is the main class users interact with to execute workflows programmatically.
 * Inject it as a Spring Bean and call {@link #execute} or {@link #debug}.
 *
 * @author Levi Li
 * @since 09/18/2023
 */
public class WorkflowEngine {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngine.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CompilerConfiguration compilerConfig;
    private final WorkflowRepository repository;
    private final VisualFlowProperties properties;
    private final List<WorkflowExecutionListener> listeners;
    private final ScheduledExecutorService timeoutScheduler;
    private GroovyClassLoader groovyClassLoader;

    public WorkflowEngine(CompilerConfiguration compilerConfig,
                          WorkflowRepository repository,
                          VisualFlowProperties properties,
                          List<WorkflowExecutionListener> listeners) {
        this.compilerConfig = compilerConfig;
        this.repository = repository;
        this.properties = properties;
        this.listeners = listeners != null ? listeners : Collections.emptyList();
        this.timeoutScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "script-timeout");
            t.setDaemon(true);
            return t;
        });
        this.groovyClassLoader = new GroovyClassLoader(
                Thread.currentThread().getContextClassLoader(), compilerConfig);
    }

    // ── Public API ──

    /**
     * Execute a workflow by ID.
     */
    @SuppressWarnings("rawtypes")
    public Map<Integer, List<WorkflowTaskLog>> execute(Integer workflowId, Map inputVariables) {
        WorkflowMetadata metadata = repository.findById(workflowId);
        if (metadata == null) {
            throw new IllegalArgumentException("Workflow not found: " + workflowId);
        }
        Map<Integer, List<WorkflowTaskLog>> logs = doExecute(metadata.getScriptMetadata(), inputVariables);
        notifyListeners(workflowId, metadata.getRevision(), logs);
        return logs;
    }

    /**
     * Execute a workflow by name.
     */
    @SuppressWarnings("rawtypes")
    public Map<Integer, List<WorkflowTaskLog>> execute(String workflowName, Map inputVariables) {
        WorkflowMetadata metadata = repository.findByName(workflowName);
        if (metadata == null) {
            throw new IllegalArgumentException("Workflow not found: " + workflowName);
        }
        Map<Integer, List<WorkflowTaskLog>> logs = doExecute(metadata.getScriptMetadata(), inputVariables);
        notifyListeners(metadata.getWorkflowId(), metadata.getRevision(), logs);
        return logs;
    }

    /**
     * Debug a workflow with provided script metadata and input values.
     */
    public Map<Integer, List<WorkflowTaskLog>> debug(DebugRequest debugRequest) {
        return debug(debugRequest, null);
    }

    /**
     * Debug a workflow with streaming callback.
     * Each node execution result is pushed to the callback in real-time.
     * If breakpointNodeId is set, execution stops after that node completes.
     */
    public Map<Integer, List<WorkflowTaskLog>> debug(DebugRequest debugRequest,
                                                      Consumer<WorkflowTaskLog> onNodeComplete) {
        if (debugRequest.getScriptMetadata() == null) {
            WorkflowTaskLog errorLog = new WorkflowTaskLog();
            errorLog.setScriptName("");
            errorLog.setScriptId("");
            errorLog.setScriptRunStatus(ScriptRunStatus.Error);
            errorLog.setScriptRunError("ScriptMetadata is null");
            if (onNodeComplete != null) onNodeComplete.accept(errorLog);
            return Collections.singletonMap(1, Collections.singletonList(errorLog));
        }
        return doExecute(debugRequest.getScriptMetadata(), debugRequest.getInputValues(),
                onNodeComplete, debugRequest.getBreakpointNodeId());
    }

    /**
     * Compile and validate Groovy code without executing it.
     * Returns errors and warnings from the Groovy compiler.
     */
    public List<Diagnostic> compileScript(String code) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        CompilerConfiguration config = new CompilerConfiguration(compilerConfig);
        config.setWarningLevel(WarningMessage.LIKELY_ERRORS);

        try (GroovyClassLoader tempLoader = new GroovyClassLoader(
                Thread.currentThread().getContextClassLoader(), config)) {

            CompilationUnit unit = new CompilationUnit(config, null, tempLoader);
            unit.addSource("UserScript", code);

            try {
                unit.compile(Phases.CANONICALIZATION);
            } catch (MultipleCompilationErrorsException e) {
                List<? extends Message> errors = e.getErrorCollector().getErrors();
                diagnostics.addAll(CommonUtils.getDiagnostics(errors, Diagnostic.SEVERITY_ERROR));
            }

            // Collect warnings from ErrorCollector (even if compilation succeeded)
            ErrorCollector collector = unit.getErrorCollector();
            if (collector.hasWarnings()) {
                List<? extends Message> warnings = collector.getWarnings();
                diagnostics.addAll(CommonUtils.getDiagnostics(warnings, Diagnostic.SEVERITY_WARNING));
            }

        } catch (Exception e) {
            Diagnostic diagnostic = new Diagnostic();
            diagnostic.setMessage(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            diagnostic.setSeverity(Diagnostic.SEVERITY_ERROR);
            diagnostics.add(diagnostic);
        }
        return diagnostics;
    }

    // ── CRUD (delegated to repository, used by Controller) ──

    public WorkflowMetadata createWorkflow(WorkflowMetadata metadata) {
        ScriptMetadata start = new ScriptMetadata();
        start.setScriptId("1");
        start.setScriptText("");
        start.setScriptName("Start");
        start.setScriptType(ScriptType.Start);
        start.setChildren(null);
        metadata.setScriptMetadata(start);
        repository.save(metadata);
        return metadata;
    }

    public WorkflowMetadata getWorkflow(Integer workflowId) {
        return repository.findById(workflowId);
    }

    public WorkflowMetadata updateWorkflow(WorkflowMetadata metadata) {
        repository.save(metadata);
        return metadata;
    }

    public void deleteWorkflow(Integer workflowId) {
        repository.deleteById(workflowId);
    }

    // ── Structure validation ──

    /**
     * Validate the tree structure before execution.
     * Returns a list of error messages; empty means valid.
     */
    public List<String> validateStructure(ScriptMetadata node) {
        List<String> errors = new ArrayList<>();
        doValidateStructure(node, errors);
        return errors;
    }

    private void doValidateStructure(ScriptMetadata node, List<String> errors) {
        List<ScriptMetadata> children = node.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            if (node.getScriptType() != ScriptType.End) {
                errors.add("Node [" + node.getScriptName() + "]: missing End node (leaf node must be End type)");
            }
            return;
        }

        boolean hasCondition = children.stream()
                .anyMatch(c -> c.getScriptType() == ScriptType.Condition);
        boolean hasNonCondition = children.stream()
                .anyMatch(c -> c.getScriptType() != ScriptType.Condition
                        && c.getScriptType() != ScriptType.End);
        boolean hasEnd = children.stream()
                .anyMatch(c -> c.getScriptType() == ScriptType.End);

        if (hasCondition && hasNonCondition) {
            errors.add("Node [" + node.getScriptName() + "]: Condition siblings must all be Condition type");
        }
        if (hasEnd && children.size() > 1) {
            errors.add("Node [" + node.getScriptName() + "]: End node cannot have siblings");
        }

        for (ScriptMetadata child : children) {
            doValidateStructure(child, errors);
        }
    }

    // ── Internal execution logic ──

    @SuppressWarnings("rawtypes")
    private Map<Integer, List<WorkflowTaskLog>> doExecute(ScriptMetadata script, Map inputVariables) {
        return doExecute(script, inputVariables, null, null);
    }

    @SuppressWarnings("rawtypes")
    private Map<Integer, List<WorkflowTaskLog>> doExecute(ScriptMetadata script, Map inputVariables,
                                                           Consumer<WorkflowTaskLog> onNodeComplete,
                                                           String breakpointNodeId) {
        // Validate structure only for full workflow execution (root is Start)
        List<String> validationErrors = script.getScriptType() == ScriptType.Start
                ? validateStructure(script) : Collections.emptyList();
        if (!validationErrors.isEmpty()) {
            Map<Integer, List<WorkflowTaskLog>> logs = new HashMap<>();
            WorkflowTaskLog errorLog = new WorkflowTaskLog();
            errorLog.setScriptId(script.getScriptId());
            errorLog.setScriptName(script.getScriptName());
            errorLog.setScriptRunStatus(ScriptRunStatus.Error);
            errorLog.setScriptRunError("Structure validation failed:\n" + String.join("\n", validationErrors));
            errorLog.setScriptRunTime(LocalDateTime.now());
            errorLog.setDurationMs(0L);
            if (onNodeComplete != null) onNodeComplete.accept(errorLog);
            logs.put(1, Collections.singletonList(errorLog));
            return logs;
        }

        Map<Integer, List<WorkflowTaskLog>> logs = new HashMap<>();
        recursiveAndExecute(script, new Binding(inputVariables), logs, 1, onNodeComplete, breakpointNodeId);
        return logs;
    }

    @SuppressWarnings("unchecked")
    private ExecutionSignal recursiveAndExecute(ScriptMetadata script, Binding binding,
                                        Map<Integer, List<WorkflowTaskLog>> logMap, int level,
                                        Consumer<WorkflowTaskLog> onNodeComplete,
                                        String breakpointNodeId) {
        List<WorkflowTaskLog> logList = logMap.computeIfAbsent(level, k -> new ArrayList<>());

        switch (script.getScriptType()) {
            case Start:
                logTerminalNode(script, binding, logList, ScriptRunStatus.Start, onNodeComplete);
                if (isBreakpoint(script, breakpointNodeId)) return ExecutionSignal.STOP;
                return recurseChildren(script, binding, logMap, level, onNodeComplete, breakpointNodeId);

            case End:
                logTerminalNode(script, binding, logList, ScriptRunStatus.End, onNodeComplete);
                return ExecutionSignal.CONTINUE;

            case Script:
                return executeAndRecurse(script, binding, logList,
                        this::executeScript, logMap, level, result -> true, onNodeComplete, breakpointNodeId);

            case Condition: {
                WorkflowTaskLog condLog = logScriptExecution(script, binding, logList, this::executeScript);
                if (condLog.getScriptRunStatus() == ScriptRunStatus.Error) {
                    if (onNodeComplete != null) onNodeComplete.accept(condLog);
                    return ExecutionSignal.STOP;
                }
                Object result = condLog.getScriptRunResult();
                if (!(result instanceof Boolean)) {
                    condLog.setScriptRunStatus(ScriptRunStatus.Error);
                    condLog.setScriptRunError("Condition node must return a Boolean (true/false), but got: "
                            + (result == null ? "null" : result.getClass().getSimpleName() + "(" + result + ")"));
                    if (onNodeComplete != null) onNodeComplete.accept(condLog);
                    return ExecutionSignal.STOP;
                }
                if (onNodeComplete != null) onNodeComplete.accept(condLog);
                if (isBreakpoint(script, breakpointNodeId)) return ExecutionSignal.STOP;
                if ((Boolean) result) {
                    ExecutionSignal childSignal = recurseChildren(script, binding, logMap, level, onNodeComplete, breakpointNodeId);
                    if (childSignal == ExecutionSignal.STOP) return ExecutionSignal.STOP;
                    return ExecutionSignal.CONDITION_HIT;
                }
                return ExecutionSignal.CONTINUE;
            }

            case Rule:
                return executeAndRecurse(script, binding, logList,
                        (s, b) -> {
                            List<Rule> rules = RuleEngine.parser(s);
                            return RuleEngine.execute(rules, b);
                        }, logMap, level, result -> true, onNodeComplete, breakpointNodeId);

            default:
                return ExecutionSignal.CONTINUE;
        }
    }

    private boolean isBreakpoint(ScriptMetadata script, String breakpointNodeId) {
        return breakpointNodeId != null && breakpointNodeId.equals(script.getScriptId());
    }

    private ExecutionSignal executeAndRecurse(ScriptMetadata script, Binding binding,
                                      List<WorkflowTaskLog> logList,
                                      BiFunction<ScriptMetadata, Binding, Object> executor,
                                      Map<Integer, List<WorkflowTaskLog>> logMap,
                                      int level, Predicate<Object> shouldRecurse,
                                      Consumer<WorkflowTaskLog> onNodeComplete,
                                      String breakpointNodeId) {
        WorkflowTaskLog log = logScriptExecution(script, binding, logList, executor);
        if (onNodeComplete != null) onNodeComplete.accept(log);
        if (log.getScriptRunStatus() == ScriptRunStatus.Error) {
            return ExecutionSignal.STOP;
        }
        if (isBreakpoint(script, breakpointNodeId)) return ExecutionSignal.STOP;
        if (shouldRecurse.test(log.getScriptRunResult())) {
            return recurseChildren(script, binding, logMap, level, onNodeComplete, breakpointNodeId);
        }
        return ExecutionSignal.CONTINUE;
    }

    private ExecutionSignal recurseChildren(ScriptMetadata script, Binding binding,
                                    Map<Integer, List<WorkflowTaskLog>> logMap, int level,
                                    Consumer<WorkflowTaskLog> onNodeComplete,
                                    String breakpointNodeId) {
        if (!CollectionUtils.isEmpty(script.getChildren())) {
            for (ScriptMetadata child : script.getChildren()) {
                ExecutionSignal signal = recursiveAndExecute(child, binding, logMap, level + 1, onNodeComplete, breakpointNodeId);
                if (signal == ExecutionSignal.STOP) return ExecutionSignal.STOP;
                if (signal == ExecutionSignal.CONDITION_HIT) break;
            }
        }
        return ExecutionSignal.CONTINUE;
    }

    private void logTerminalNode(ScriptMetadata script, Binding binding,
                                 List<WorkflowTaskLog> logList, ScriptRunStatus status,
                                 Consumer<WorkflowTaskLog> onNodeComplete) {
        WorkflowTaskLog log = new WorkflowTaskLog();
        log.setScriptId(script.getScriptId());
        log.setScriptName(script.getScriptName());
        log.setScriptType(script.getScriptType());
        Object snapshot = snapshotBinding(binding);
        log.setBeforeRunBinding(snapshot);
        log.setAfterRunBinding(snapshot);
        log.setScriptRunStatus(status);
        log.setScriptRunTime(LocalDateTime.now());
        log.setDurationMs(0L);
        logList.add(log);
        if (onNodeComplete != null) onNodeComplete.accept(log);
    }

    private WorkflowTaskLog logScriptExecution(ScriptMetadata script, Binding binding,
                                               List<WorkflowTaskLog> logList,
                                               BiFunction<ScriptMetadata, Binding, Object> executor) {
        WorkflowTaskLog log = new WorkflowTaskLog();
        log.setScriptId(script.getScriptId());
        log.setScriptName(script.getScriptName());
        log.setScriptType(script.getScriptType());
        log.setBeforeRunBinding(snapshotBinding(binding));
        log.setScriptRunTime(LocalDateTime.now());
        long startNanos = System.nanoTime();
        try {
            Object result = executor.apply(script, binding);
            log.setScriptRunStatus(ScriptRunStatus.Success);
            log.setScriptRunResult(result);
        } catch (Throwable e) {
            log.setScriptRunStatus(ScriptRunStatus.Error);
            log.setScriptRunError(extractErrorMessage(e));
            logger.error("Script execution failed: {}", script.getScriptName(), e);
        } finally {
            log.setDurationMs((System.nanoTime() - startNanos) / 1_000_000);
            log.setAfterRunBinding(snapshotBinding(binding));
            logList.add(log);
        }
        return log;
    }

    /**
     * Execute a single script node. Package-private, used by RuleEngine.
     */
    Object executeScript(ScriptMetadata scriptMetadata, Binding binding) {
        String filename;
        try {
            filename = scriptMetadata.getScriptType().name() + "_Id_" + scriptMetadata.getScriptId()
                    + "_MD5_" + EncodingGroovyMethods.md5(scriptMetadata.getScriptText()) + ".groovy";
        } catch (NoSuchAlgorithmException e) {
            throw new GroovyBugError("Failed to generate md5", e);
        }
        GroovyCodeSource codeSource = new GroovyCodeSource(
                scriptMetadata.getScriptText(), filename, "/groovy/script");
        Class<?> parseClass = groovyClassLoader.parseClass(
                codeSource, properties.isEnableCacheSource());
        Script script = InvokerHelper.createScript(parseClass, binding);

        int timeout = properties.getScriptTimeoutSeconds();
        if (timeout <= 0) {
            return script.run();
        }

        Thread currentThread = Thread.currentThread();
        ScheduledFuture<?> interruptor = timeoutScheduler.schedule(
                currentThread::interrupt, timeout, TimeUnit.SECONDS);
        try {
            return script.run();
        } catch (Exception e) {
            if (Thread.interrupted() && !interruptor.isDone()) {
                throw new RuntimeException("Script execution timed out (" + timeout + "s), node: " + scriptMetadata.getScriptName());
            }
            throw e;
        } finally {
            interruptor.cancel(false);
            Thread.interrupted(); // Clear interrupt flag to avoid affecting subsequent logic
        }
    }

    @SuppressWarnings("unchecked")
    private String snapshotBinding(Binding binding) {
        try {
            return OBJECT_MAPPER.writeValueAsString(binding.getVariables());
        } catch (Exception e) {
            // Jackson serialization failed — build a safe JSON map with toString() for non-serializable values
            Map<String, Object> safeMap = new LinkedHashMap<>();
            binding.getVariables().forEach((k, v) -> {
                try {
                    OBJECT_MAPPER.writeValueAsString(v);
                    safeMap.put(String.valueOf(k), v);
                } catch (Exception ex) {
                    safeMap.put(String.valueOf(k), String.valueOf(v));
                }
            });
            try {
                return OBJECT_MAPPER.writeValueAsString(safeMap);
            } catch (Exception ex) {
                logger.warn("Failed to snapshot binding", ex);
                return "{}";
            }
        }
    }

    private String extractErrorMessage(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }

    private void notifyListeners(Integer workflowId, Integer revision,
                                 Map<Integer, List<WorkflowTaskLog>> logs) {
        for (WorkflowExecutionListener listener : listeners) {
            try {
                listener.onExecutionComplete(workflowId, revision, logs);
            } catch (Exception e) {
                logger.error("Execution listener failed", e);
            }
        }
    }
}
