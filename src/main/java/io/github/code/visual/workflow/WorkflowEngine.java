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
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.Message;
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
import java.util.List;
import java.util.Map;
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
    private GroovyClassLoader groovyClassLoader;

    public WorkflowEngine(CompilerConfiguration compilerConfig,
                          WorkflowRepository repository,
                          VisualFlowProperties properties,
                          List<WorkflowExecutionListener> listeners) {
        this.compilerConfig = compilerConfig;
        this.repository = repository;
        this.properties = properties;
        this.listeners = listeners != null ? listeners : Collections.emptyList();
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
        return doExecute(debugRequest.getScriptMetadata(), debugRequest.getInputValues(), onNodeComplete);
    }

    /**
     * Compile and validate Groovy code without executing it.
     */
    public List<Diagnostic> compileScript(String code) {
        try (GroovyClassLoader tempLoader = new GroovyClassLoader(
                Thread.currentThread().getContextClassLoader(), compilerConfig)) {
            tempLoader.parseClass(code);
            return new ArrayList<>();
        } catch (Exception e) {
            if (!(e instanceof MultipleCompilationErrorsException)) {
                throw new RuntimeException(e);
            }
            List<? extends Message> errors =
                    ((MultipleCompilationErrorsException) e).getErrorCollector().getErrors();
            return CommonUtils.getDiagnostics(errors);
        }
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

    // ── Internal execution logic ──

    @SuppressWarnings("rawtypes")
    private Map<Integer, List<WorkflowTaskLog>> doExecute(ScriptMetadata script, Map inputVariables) {
        return doExecute(script, inputVariables, null);
    }

    @SuppressWarnings("rawtypes")
    private Map<Integer, List<WorkflowTaskLog>> doExecute(ScriptMetadata script, Map inputVariables,
                                                           Consumer<WorkflowTaskLog> onNodeComplete) {
        Map<Integer, List<WorkflowTaskLog>> logs = new HashMap<>();
        recursiveAndExecute(script, new Binding(inputVariables), logs, 1, onNodeComplete);
        return logs;
    }

    @SuppressWarnings("unchecked")
    private boolean recursiveAndExecute(ScriptMetadata script, Binding binding,
                                        Map<Integer, List<WorkflowTaskLog>> logMap, int level,
                                        Consumer<WorkflowTaskLog> onNodeComplete) {
        List<WorkflowTaskLog> logList = logMap.computeIfAbsent(level, k -> new ArrayList<>());

        switch (script.getScriptType()) {
            case Start:
                logTerminalNode(script, binding, logList, ScriptRunStatus.Start, onNodeComplete);
                return recurseChildren(script, binding, logMap, level, onNodeComplete);

            case End:
                logTerminalNode(script, binding, logList, ScriptRunStatus.End, onNodeComplete);
                return true;

            case Script:
                return executeAndRecurse(script, binding, logList,
                        this::executeScript, logMap, level, result -> true, onNodeComplete);

            case Condition:
                return executeAndRecurse(script, binding, logList,
                        this::executeScript, logMap, level,
                        result -> result instanceof Boolean && (Boolean) result, onNodeComplete);

            case Rule:
                return executeAndRecurse(script, binding, logList,
                        (s, b) -> {
                            List<Rule> rules = RuleEngine.parser(s);
                            return RuleEngine.execute(rules, b);
                        }, logMap, level, result -> true, onNodeComplete);

            default:
                return true;
        }
    }

    private boolean executeAndRecurse(ScriptMetadata script, Binding binding,
                                      List<WorkflowTaskLog> logList,
                                      BiFunction<ScriptMetadata, Binding, Object> executor,
                                      Map<Integer, List<WorkflowTaskLog>> logMap,
                                      int level, Predicate<Object> shouldRecurse,
                                      Consumer<WorkflowTaskLog> onNodeComplete) {
        WorkflowTaskLog log = logScriptExecution(script, binding, logList, executor);
        if (onNodeComplete != null) onNodeComplete.accept(log);
        if (log.getScriptRunStatus() == ScriptRunStatus.Error) {
            return false;
        }
        if (shouldRecurse.test(log.getScriptRunResult())) {
            return recurseChildren(script, binding, logMap, level, onNodeComplete);
        }
        return true;
    }

    private boolean recurseChildren(ScriptMetadata script, Binding binding,
                                    Map<Integer, List<WorkflowTaskLog>> logMap, int level,
                                    Consumer<WorkflowTaskLog> onNodeComplete) {
        if (!CollectionUtils.isEmpty(script.getChildren())) {
            for (ScriptMetadata child : script.getChildren()) {
                if (!recursiveAndExecute(child, binding, logMap, level + 1, onNodeComplete)) {
                    return false;
                }
            }
        }
        return true;
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
        try {
            Object result = executor.apply(script, binding);
            log.setScriptRunStatus(ScriptRunStatus.Success);
            log.setScriptRunResult(result);
        } catch (Throwable e) {
            log.setScriptRunStatus(ScriptRunStatus.Error);
            log.setScriptRunError(extractErrorMessage(e));
            logger.error("Script execution failed: {}", script.getScriptName(), e);
        } finally {
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
        return script.run();
    }

    @SuppressWarnings("unchecked")
    private String snapshotBinding(Binding binding) {
        try {
            return OBJECT_MAPPER.writeValueAsString(binding.getVariables());
        } catch (Exception e) {
            return binding.getVariables().toString();
        }
    }

    private String extractErrorMessage(Throwable e) {
        if (e.getCause() != null && e.getCause().getCause() != null) {
            return e.getCause().getCause().getMessage();
        }
        if (e.getCause() != null) {
            return e.getCause().getMessage();
        }
        return e.getMessage();
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
