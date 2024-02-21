package io.github.code.visual.workflow;

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
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.function.Supplier;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
@Service
public class WorkflowManagerImpl implements WorkflowManager {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private final CompilerConfiguration config;
    private final WorkflowMetadataRepository workflowMetadataRepository;
    private GroovyClassLoader groovyClassLoader;

    @Autowired
    public WorkflowManagerImpl(CompilerConfiguration config, WorkflowMetadataRepository workflowMetadataRepository) {
        this.config = config;
        this.workflowMetadataRepository = workflowMetadataRepository;
        groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
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
            workflowTaskLog.setBeforeRunBinding(debugRequest.getInputValues());
            workflowTaskLog.setAfterRunBinding(null);
            workflowTaskLog.setScriptRunStatus(ScriptRunStatus.Error);
            workflowTaskLog.setScriptRunResult(null);
            workflowTaskLog.setScriptRunTime(null);
            workflowTaskLog.setScriptRunError("脚本为空");

            return Collections.singletonMap(1, Collections.singletonList(workflowTaskLog));
        }
        Map<Integer, List<WorkflowTaskLog>> workflowTaskLogMap = new HashMap<>();
        this.recursiveAndExecute(debugRequest.getScriptMetadata(), new Binding(debugRequest.getInputValues()), workflowTaskLogMap, 1);
        try {
            resetGroovyClassLoader();
        } catch (IOException e) {
            logger.error("重置GroovyClassLoader失败", e);
        }
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

            Map StartRunBinding = new HashMap<>(binding.getVariables());
            WorkflowTaskLog workflowTaskLog = new WorkflowTaskLog();
            workflowTaskLog.setScriptId(script.getScriptId());
            workflowTaskLog.setScriptName(script.getScriptName());
            workflowTaskLog.setBeforeRunBinding(StartRunBinding);
            workflowTaskLog.setAfterRunBinding(StartRunBinding);
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

            Map endBinding = new HashMap<>(binding.getVariables());
            WorkflowTaskLog workflowTaskLog = new WorkflowTaskLog();
            workflowTaskLog.setScriptId(script.getScriptId());
            workflowTaskLog.setScriptName(script.getScriptName());
            workflowTaskLog.setBeforeRunBinding(endBinding);
            workflowTaskLog.setAfterRunBinding(endBinding);
            workflowTaskLog.setScriptRunStatus(ScriptRunStatus.End);
            workflowTaskLog.setScriptRunResult(null);
            workflowTaskLog.setScriptRunTime(new Date());
            workflowTaskLog.setScriptRunError(null);
            workflowTaskLogList.add(workflowTaskLog);
            workflowTaskLogMap.put(currentLevel, workflowTaskLogList);

        } else if (script.getScriptType() == ScriptType.Condition) {
            return logScriptExecution(script, binding, workflowTaskLogList, () -> {
                Object executeScript = this.executeScript(script, binding);
                if (executeScript instanceof Boolean && (Boolean) executeScript) {

                    if (!CollectionUtils.isEmpty(script.getChildren())) {
                        for (ScriptMetadata child : script.getChildren()) {
                            boolean childSuccess = recursiveAndExecute(child, binding, workflowTaskLogMap, currentLevel + 1);
                            if (!childSuccess) {
                                return false;
                            }
                        }
                    }
                }
                return executeScript;
            }, workflowTaskLogMap, currentLevel);


        } else if (script.getScriptType() == ScriptType.Script) {
            boolean success = logScriptExecution(script, binding, workflowTaskLogList, () -> this.executeScript(script, binding), workflowTaskLogMap, currentLevel);
            if (!success) {
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
            return logScriptExecution(script, binding, workflowTaskLogList, () -> {

                List<Rule> rules = RuleEngine.parser(script.getScriptText());
                //闭包导致不能序列化 要移除
                String executeScript = RuleEngine.execute(rules, binding);


                if (!CollectionUtils.isEmpty(script.getChildren())) {
                    for (ScriptMetadata child : script.getChildren()) {
                        boolean childSuccess = recursiveAndExecute(child, binding, workflowTaskLogMap, currentLevel + 1);
                        if (!childSuccess) {
                            return false;
                        }

                    }
                }
                return executeScript;
            }, workflowTaskLogMap, currentLevel);
        }
        return true;
    }

    private boolean logScriptExecution(ScriptMetadata script, Binding binding, List<WorkflowTaskLog> workflowTaskLogList, Supplier<Object> scriptExecutor, Map<Integer, List<WorkflowTaskLog>> workflowTaskLogMap, int currentLevel) {
        HashMap beforeRunBinding = new HashMap<>(binding.getVariables());
        WorkflowTaskLog workflowTaskLog = new WorkflowTaskLog();
        workflowTaskLog.setScriptId(script.getScriptId());
        workflowTaskLog.setScriptName(script.getScriptName());
        workflowTaskLog.setBeforeRunBinding(beforeRunBinding);
        workflowTaskLog.setScriptRunTime(new Date());

        try {
            Object result = scriptExecutor.get(); // 执行脚本

            workflowTaskLog.setScriptRunStatus(ScriptRunStatus.Success);
            workflowTaskLog.setScriptRunResult(result);
        } catch (Exception e) {
            workflowTaskLog.setScriptRunStatus(ScriptRunStatus.Error);
            workflowTaskLog.setScriptRunError(e.getMessage());
        } finally {
            workflowTaskLog.setAfterRunBinding(new HashMap<>(binding.getVariables()));
            workflowTaskLogList.add(workflowTaskLog);
            // 立即更新workflowTaskLogMap以确保日志不会丢失
            workflowTaskLogMap.put(currentLevel, workflowTaskLogList);
        }

        return workflowTaskLog.getScriptRunStatus() != ScriptRunStatus.Error;
    }


    @SuppressWarnings("rawtypes,unchecked")
    private Object executeScript(ScriptMetadata metadata, Binding binding) {
        Class<?> aClass = groovyClassLoader.parseClass(metadata.getScriptText());
        Map variables = binding.getVariables();
        Script script = InvokerHelper.createScript(aClass, binding);
        Object run = script.run();
        if (run instanceof Map) {
            variables.putAll((Map) run);
        }
        return run;
    }


    @SuppressWarnings("unchecked")
    @Override
    public Script parseGroovyScript(String scriptText, Binding binding) {
        Class<? extends Script> aClass = this.groovyClassLoader.parseClass(scriptText);
        return InvokerHelper.createScript(aClass, binding);
    }

    /**
     * 编译的时候肯定有很多大量的,细碎的脚本。应该用临时的groovyClassLoader来编译
     *
     * @param code
     * @return
     */
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
        return workflowMetadataRepository.create(workflowMetadata);
    }

    @Override
    public Object deleteWorkflowMetadata(Integer workflowId) {
        return workflowMetadataRepository.deleteByWorkflowId(workflowId);
    }

    @Override
    public List<WorkflowMetadata> getMenuWorkflowNameList() {
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