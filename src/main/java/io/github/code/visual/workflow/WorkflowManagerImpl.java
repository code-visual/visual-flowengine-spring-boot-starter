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

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
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
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
@Service
@ConditionalOnMissingBean(WorkflowManager.class)
public class WorkflowManagerImpl implements WorkflowManager {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private final CompilerConfiguration config;
    private final WorkflowMetadataRepository workflowMetadataRepository;
    private GroovyClassLoader groovyClassLoader;

    private Map<String, Class> parseClassCache = new ConcurrentHashMap<>();

    @Autowired
    public WorkflowManagerImpl(CompilerConfiguration config, WorkflowMetadataRepository workflowMetadataRepository) {
        this.config = config;
        this.workflowMetadataRepository = workflowMetadataRepository;
        groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
//        groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader(),config);
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

    public boolean recursiveAndExecute(ScriptMetadata script, Binding binding, Map<Integer, List<WorkflowTaskLog>> workflowTaskLogMap, int currentLevel) {

        List<WorkflowTaskLog> workflowTaskLogList = workflowTaskLogMap.getOrDefault(currentLevel, new ArrayList<>());

        if (script.getScriptType() == ScriptType.Start) {

            String string = binding.getVariables().toString();
            WorkflowTaskLog workflowTaskLog = new WorkflowTaskLog();
            workflowTaskLog.setScriptId(script.getScriptId());
            workflowTaskLog.setScriptName(script.getScriptName());
            workflowTaskLog.setBeforeRunBinding(string);
            workflowTaskLog.setAfterRunBinding(string);
            workflowTaskLog.setScriptType(ScriptType.Start);
            workflowTaskLog.setScriptRunStatus(ScriptRunStatus.Start);
            workflowTaskLog.setScriptRunResult(null);
            workflowTaskLog.setScriptRunTime(new Date());
            workflowTaskLog.setScriptRunError(null);
            workflowTaskLogList.add(workflowTaskLog);
            workflowTaskLogMap.put(currentLevel, workflowTaskLogList);
            if (!CollectionUtils.isEmpty(script.getChildren())) {
                for (ScriptMetadata child : script.getChildren()) {
                    boolean childSuccess = recursiveAndExecute(child, binding, workflowTaskLogMap, currentLevel + 1);
                    if (!childSuccess) {
                        return false;
                    }
                }
            }


        } else if (script.getScriptType() == ScriptType.End) {

            String string = binding.getVariables().toString();
            WorkflowTaskLog workflowTaskLog = new WorkflowTaskLog();
            workflowTaskLog.setScriptId(script.getScriptId());
            workflowTaskLog.setScriptName(script.getScriptName());
            workflowTaskLog.setBeforeRunBinding(string);
            workflowTaskLog.setAfterRunBinding(string);
            workflowTaskLog.setScriptType(ScriptType.End);
            workflowTaskLog.setScriptRunStatus(ScriptRunStatus.End);
            workflowTaskLog.setScriptRunResult(null);
            workflowTaskLog.setScriptRunTime(new Date());
            workflowTaskLog.setScriptRunError(null);
            workflowTaskLogList.add(workflowTaskLog);
            workflowTaskLogMap.put(currentLevel, workflowTaskLogList);

        } else if (script.getScriptType() == ScriptType.Condition) {


            //todo 一样有深浅拷贝的问题
            Binding replaceBinding = new Binding( new HashMap<>(binding.getVariables()));

            WorkflowTaskLog runResult = logScriptExecution(script, replaceBinding, workflowTaskLogList,
                    this::executeScript,
                    workflowTaskLogMap, currentLevel);
            if (runResult.getScriptRunStatus() == ScriptRunStatus.Error) {
                return false;
            }
            boolean trueCondition = runResult.getScriptRunResult() instanceof Boolean && (Boolean) runResult.getScriptRunResult();

            if (trueCondition && !CollectionUtils.isEmpty(script.getChildren())) {

                for (ScriptMetadata child : script.getChildren()) {
                    boolean childSuccess = recursiveAndExecute(child, replaceBinding, workflowTaskLogMap, currentLevel + 1);
                    if (!childSuccess) {
                        return false;
                    }
                }
            }

        } else if (script.getScriptType() == ScriptType.Script) {
            WorkflowTaskLog runResult = logScriptExecution(script, binding, workflowTaskLogList, this::executeScript, workflowTaskLogMap, currentLevel);
            if (runResult.getScriptRunStatus() == ScriptRunStatus.Error) {
                return false;
            }

            if (!CollectionUtils.isEmpty(script.getChildren())) {
                for (ScriptMetadata child : script.getChildren()) {
                    boolean childSuccess = recursiveAndExecute(child, binding, workflowTaskLogMap, currentLevel + 1);
                    if (!childSuccess) {
                        return false;
                    }
                }
            }
        } else if (script.getScriptType() == ScriptType.Rule) {
            WorkflowTaskLog runResult = logScriptExecution(script, binding, workflowTaskLogList, (scriptText, binding1) -> {
                List<Rule> rules = RuleEngine.parser(scriptText);
                return RuleEngine.execute(rules, binding1);
            }, workflowTaskLogMap, currentLevel);

            if (runResult.getScriptRunStatus() == ScriptRunStatus.Error) {
                return false;
            }

            if (!CollectionUtils.isEmpty(script.getChildren())) {
                for (ScriptMetadata child : script.getChildren()) {
                    boolean childSuccess = recursiveAndExecute(child, binding, workflowTaskLogMap, currentLevel + 1);
                    if (!childSuccess) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    private WorkflowTaskLog logScriptExecution(ScriptMetadata script,
                                               Binding binding,
                                               List<WorkflowTaskLog> workflowTaskLogList,
                                               BiFunction<String, Binding, Object> scriptExecutor,
                                               Map<Integer, List<WorkflowTaskLog>> workflowTaskLogMap, int currentLevel) {
        WorkflowTaskLog workflowTaskLog = new WorkflowTaskLog();
        workflowTaskLog.setScriptId(script.getScriptId());
        workflowTaskLog.setScriptName(script.getScriptName());
        workflowTaskLog.setScriptType(script.getScriptType());
        workflowTaskLog.setBeforeRunBinding(binding.getVariables().toString());
        workflowTaskLog.setScriptRunTime(new Date());

        try {
            Object result = scriptExecutor.apply(script.getScriptText(), binding);

            workflowTaskLog.setScriptRunStatus(ScriptRunStatus.Success);
            if (result instanceof java.io.Serializable) {
                workflowTaskLog.setScriptRunResult(result);
            }

        } catch (Exception e) {
            workflowTaskLog.setScriptRunStatus(ScriptRunStatus.Error);
            workflowTaskLog.setScriptRunError(e.getMessage());
            logger.error("scriptExecutor Exception trace", e);
        } finally {
            workflowTaskLog.setAfterRunBinding(binding.getVariables().toString());
            workflowTaskLogList.add(workflowTaskLog);
            workflowTaskLogMap.put(currentLevel, workflowTaskLogList);
        }
        return workflowTaskLog;
    }


    @Override
    public Object executeScript(String scriptText, Binding binding) {
        Class<?> aClass = workflowMetadataRepository.getClassFromCache(groovyClassLoader, scriptText);
        Script script = InvokerHelper.createScript(aClass, binding);
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
        parseClassCache.clear();
    }

    @Override
    public WorkflowMetadata updateWorkflowMetadata(WorkflowMetadata workflowMetadata) {
        return workflowMetadataRepository.updateWorkflowMetadata(workflowMetadata);
    }

}
