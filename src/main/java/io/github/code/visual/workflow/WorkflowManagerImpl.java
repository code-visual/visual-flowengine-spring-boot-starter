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
import io.github.code.visual.model.WorkflowIdAndName;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
@Service
@ConditionalOnMissingBean(WorkflowManager.class)
public class WorkflowManagerImpl implements WorkflowManager {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CompilerConfiguration config;
    private final WorkflowMetadataRepository workflowMetadataRepository;
    private final VisualFlowProperties visualFlowProperties;
    private GroovyClassLoader groovyClassLoader;


    @Autowired
    public WorkflowManagerImpl(CompilerConfiguration config,
                               WorkflowMetadataRepository workflowMetadataRepository,
                               VisualFlowProperties visualFlowProperties) {
        this.config = config;
        this.workflowMetadataRepository = workflowMetadataRepository;
        this.groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
        this.visualFlowProperties = visualFlowProperties;
    }


    @SuppressWarnings("rawtypes")
    @Override
    public Map<Integer, List<WorkflowTaskLog>> startWorkflow(Integer workflowId, Map inputVariables) {
        WorkflowMetadata workflowMetadata = workflowMetadataRepository.findByWorkflowId(workflowId);
        return executeWorkflow(inputVariables, workflowMetadata);
    }

    @Override
    public Map<Integer, List<WorkflowTaskLog>> startWorkflow(String workflowName, Map inputVariables) {
        WorkflowMetadata workflowMetadata = workflowMetadataRepository.findByWorkflowName(workflowName);
        return executeWorkflow(inputVariables, workflowMetadata);
    }


    private Map<Integer, List<WorkflowTaskLog>> executeWorkflow(Map inputVariables, WorkflowMetadata workflowMetadata) {
        ScriptMetadata scriptMetadata = workflowMetadata.getScriptMetadata();
        Map<Integer, List<WorkflowTaskLog>> workflowTaskLogMap = new HashMap<>();
        this.recursiveAndExecute(scriptMetadata, new Binding(inputVariables), workflowTaskLogMap, 1);
        workflowMetadataRepository.asyncSaveWorkflowTaskLog(workflowTaskLogMap);
        return workflowTaskLogMap;
    }

    @Override
    public Map<Integer, List<WorkflowTaskLog>> debug(DebugRequest debugRequest) {

        if (debugRequest.getScriptMetadata() == null) {
            WorkflowTaskLog workflowTaskLog = new WorkflowTaskLog();
            workflowTaskLog.setScriptName("");
            workflowTaskLog.setScriptId("");
            workflowTaskLog.setBeforeRunBinding(debugRequest.toString());
            workflowTaskLog.setAfterRunBinding(null);
            workflowTaskLog.setScriptRunStatus(ScriptRunStatus.Error);
            workflowTaskLog.setScriptRunResult(null);
            workflowTaskLog.setScriptRunTime(null);
            workflowTaskLog.setScriptRunError("ScriptMetadata is null");

            return Collections.singletonMap(1, Collections.singletonList(workflowTaskLog));
        }
        Map<Integer, List<WorkflowTaskLog>> workflowTaskLogMap = new HashMap<>();
        this.recursiveAndExecute(debugRequest.getScriptMetadata(), new Binding(debugRequest.getInputValues()), workflowTaskLogMap, 1);
        workflowMetadataRepository.asyncSaveWorkflowTaskLog(workflowTaskLogMap);
        return workflowTaskLogMap;

    }

    @SuppressWarnings("all")
    public void localTestScript(List<File> files, Binding binding) throws IOException {

        GroovyClassLoader tempGroovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);

        for (File file : files) {
            Class aClass = tempGroovyClassLoader.parseClass(file);
            InvokerHelper.createScript(aClass, binding).run();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean recursiveAndExecute(ScriptMetadata script, Binding binding,
                                       Map<Integer, List<WorkflowTaskLog>> workflowTaskLogMap, int currentLevel) {

        List<WorkflowTaskLog> logList = workflowTaskLogMap.computeIfAbsent(currentLevel, k -> new ArrayList<>());

        switch (script.getScriptType()) {
            case Start:
                logTerminalNode(script, binding, logList, ScriptRunStatus.Start);
                return recurseChildren(script, binding, workflowTaskLogMap, currentLevel);

            case End:
                logTerminalNode(script, binding, logList, ScriptRunStatus.End);
                return true;

            case Script:
                return executeAndRecurse(script, binding, logList,
                        this::executeScript, workflowTaskLogMap, currentLevel, result -> true);

            case Condition:
                return executeAndRecurse(script, binding, logList,
                        this::executeScript, workflowTaskLogMap, currentLevel,
                        result -> result instanceof Boolean && (Boolean) result);

            case Rule:
                return executeAndRecurse(script, binding, logList,
                        (s, b) -> {
                            List<Rule> rules = RuleEngine.parser(s);
                            return RuleEngine.execute(rules, b);
                        }, workflowTaskLogMap, currentLevel, result -> true);

            default:
                return true;
        }
    }

    private boolean executeAndRecurse(ScriptMetadata script, Binding binding,
                                      List<WorkflowTaskLog> logList,
                                      BiFunction<ScriptMetadata, Binding, Object> executor,
                                      Map<Integer, List<WorkflowTaskLog>> workflowTaskLogMap,
                                      int currentLevel, Predicate<Object> shouldRecurse) {

        WorkflowTaskLog log = logScriptExecution(script, binding, logList, executor);

        if (log.getScriptRunStatus() == ScriptRunStatus.Error) {
            return false;
        }
        if (shouldRecurse.test(log.getScriptRunResult())) {
            return recurseChildren(script, binding, workflowTaskLogMap, currentLevel);
        }
        return true;
    }

    private boolean recurseChildren(ScriptMetadata script, Binding binding,
                                    Map<Integer, List<WorkflowTaskLog>> workflowTaskLogMap, int currentLevel) {
        if (!CollectionUtils.isEmpty(script.getChildren())) {
            for (ScriptMetadata child : script.getChildren()) {
                if (!recursiveAndExecute(child, binding, workflowTaskLogMap, currentLevel + 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void logTerminalNode(ScriptMetadata script, Binding binding,
                                 List<WorkflowTaskLog> logList, ScriptRunStatus status) {
        WorkflowTaskLog workflowTaskLog = new WorkflowTaskLog();
        workflowTaskLog.setScriptId(script.getScriptId());
        workflowTaskLog.setScriptName(script.getScriptName());
        workflowTaskLog.setScriptType(script.getScriptType());
        Object snapshot = snapshotBinding(binding);
        workflowTaskLog.setBeforeRunBinding(snapshot);
        workflowTaskLog.setAfterRunBinding(snapshot);
        workflowTaskLog.setScriptRunStatus(status);
        workflowTaskLog.setScriptRunTime(LocalDateTime.now());
        logList.add(workflowTaskLog);
    }

    @SuppressWarnings("unchecked")
    private String snapshotBinding(Binding binding) {
        try {
            return OBJECT_MAPPER.writeValueAsString(binding.getVariables());
        } catch (Exception e) {
            return binding.getVariables().toString();
        }
    }

    private WorkflowTaskLog logScriptExecution(ScriptMetadata script,
                                               Binding binding,
                                               List<WorkflowTaskLog> workflowTaskLogList,
                                               BiFunction<ScriptMetadata, Binding, Object> scriptExecutor) {
        WorkflowTaskLog workflowTaskLog = new WorkflowTaskLog();
        workflowTaskLog.setScriptId(script.getScriptId());
        workflowTaskLog.setScriptName(script.getScriptName());
        workflowTaskLog.setScriptType(script.getScriptType());
        workflowTaskLog.setBeforeRunBinding(snapshotBinding(binding));
        workflowTaskLog.setScriptRunTime(LocalDateTime.now());

        try {
            Object result = scriptExecutor.apply(script, binding);
            workflowTaskLog.setScriptRunStatus(ScriptRunStatus.Success);
            workflowTaskLog.setScriptRunResult(result);

        } catch (Throwable e) {
            workflowTaskLog.setScriptRunStatus(ScriptRunStatus.Error);
            workflowTaskLog.setScriptRunError(extractErrorMessage(e));
            logger.error("Script execution failed: {}", script.getScriptName(), e);

        } finally {
            workflowTaskLog.setAfterRunBinding(snapshotBinding(binding));
            workflowTaskLogList.add(workflowTaskLog);
        }
        return workflowTaskLog;
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


    @Override
    public Object executeScript(ScriptMetadata scriptMetadata, Binding binding) {
        Class<?> aClass = workflowMetadataRepository.getClassFromCache(groovyClassLoader, scriptMetadata);
        if (aClass != null) {
            Script script = InvokerHelper.createScript(aClass, binding);
            return script.run();
        }

        String filename;
        try {
            filename = scriptMetadata.getScriptType().name() + "_Id_" + scriptMetadata.getScriptId() + "_MD5_" + EncodingGroovyMethods.md5(scriptMetadata.getScriptText()) + ".groovy";
        } catch (NoSuchAlgorithmException e) {
            throw new GroovyBugError("Failed to generate md5", e);
        }
        GroovyCodeSource codeSource = new GroovyCodeSource(scriptMetadata.getScriptText(), filename, "/groovy/script");
        Class parseClass = groovyClassLoader.parseClass(codeSource, visualFlowProperties.isEnableCacheSource());
        Script script = InvokerHelper.createScript(parseClass, binding);
        return script.run();
    }


    @Override
    public List<Diagnostic> compileGroovyScript(String code) {

        try (GroovyClassLoader tempGroovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config)) {
            tempGroovyClassLoader.parseClass(code);
            return new ArrayList<>();
        } catch (Exception e) {
            if (!(e instanceof MultipleCompilationErrorsException)) {
                throw new RuntimeException(e);
            }
            List<? extends Message> errors = ((MultipleCompilationErrorsException) e).getErrorCollector().getErrors();
            return CommonUtils.getDiagnostics(errors);
        }
    }

    @Override
    public WorkflowMetadata getWorkflowMetadataById(Integer workflowId) {
        return workflowMetadataRepository.findByWorkflowId(workflowId);
    }

    @Override
    public Object createWorkflow(WorkflowMetadata workflowMetadata) {
        ScriptMetadata metadata = new ScriptMetadata();
        metadata.setScriptId("1");

        metadata.setScriptText("");
        metadata.setScriptName("Start");
        metadata.setScriptType(ScriptType.Start);
        metadata.setChildren(null);

        workflowMetadata.setScriptMetadata(metadata);
        workflowMetadata.setCreateTime(new Date());
        workflowMetadataRepository.create(workflowMetadata);
        return "ok";
    }

    @Override
    public Object deleteWorkflowMetadata(Integer workflowId) {
        return workflowMetadataRepository.deleteByWorkflowId(workflowId);
    }

    @Override
    public List<WorkflowIdAndName> getMenuWorkflowNameList() {
        return workflowMetadataRepository.getMenuWorkflowList();
    }


    @Override
    public void resetGroovyClassLoader() throws IOException {

        if (this.groovyClassLoader != null) {
            this.groovyClassLoader.close();
        }
        this.groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
    }

    @Override
    public WorkflowMetadata updateWorkflowMetadata(WorkflowMetadata workflowMetadata) {
        return workflowMetadataRepository.updateWorkflowMetadata(workflowMetadata);
    }

}
