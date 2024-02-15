package com.github.managetech.workflow;

import com.github.managetech.model.DebugRequest;
import com.github.managetech.model.Diagnostic;
import com.github.managetech.model.ScriptMetadata;
import com.github.managetech.model.ScriptRunStatus;
import com.github.managetech.model.ScriptType;
import com.github.managetech.model.WorkflowMetadata;
import com.github.managetech.model.WorkflowTaskLog;
import com.github.managetech.ruleengine.Rule;
import com.github.managetech.ruleengine.RuleEngine;
import com.github.managetech.utils.CommonUtils;
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
    public void execute(Integer workflowId, Map inputVariables) {
        WorkflowMetadata workflowMetadata = workflowMetadataRepository.findByWorkflowId(workflowId);
        ScriptMetadata scriptMetadata = workflowMetadata.getScriptMetadata();
        Map<Integer, List<WorkflowTaskLog>> workflowTaskLogMap = new HashMap<>();
        try {
            this.recursiveAndExecute(scriptMetadata, new Binding(inputVariables), workflowTaskLogMap, 1);
        } finally {
            logger.info("保存日志:{}", workflowTaskLogMap);
        }
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

    public void recursiveAndExecute(ScriptMetadata script, Binding binding, Map<Integer, List<WorkflowTaskLog>> workflowTaskLogMap, int currentLevel) {

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

            if (!CollectionUtils.isEmpty(script.getChildren())) {
                for (ScriptMetadata child : script.getChildren()) {
                    recursiveAndExecute(child, binding, workflowTaskLogMap, currentLevel + 1);
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


        } else if (script.getScriptType() == ScriptType.Condition) {
            logScriptExecution(script, binding, workflowTaskLogList, () -> {
                Object executeScript = this.executeScript(script, binding);
                if (executeScript instanceof Boolean && (Boolean) executeScript) {

                    if (!CollectionUtils.isEmpty(script.getChildren())) {
                        for (ScriptMetadata child : script.getChildren()) {
                            recursiveAndExecute(child, binding, workflowTaskLogMap, currentLevel + 1);
                        }
                    }
                }
                return executeScript;
            });


        } else if (script.getScriptType() == ScriptType.Script) {
            logScriptExecution(script, binding, workflowTaskLogList, () -> this.executeScript(script, binding));
            if (!CollectionUtils.isEmpty(script.getChildren())) {
                for (ScriptMetadata child : script.getChildren()) {
                    recursiveAndExecute(child, binding, workflowTaskLogMap, currentLevel + 1);
                }
            }
        } else if (script.getScriptType() == ScriptType.Rule) {
            logScriptExecution(script, binding, workflowTaskLogList, () -> {

                List<Rule> rules = RuleEngine.parser(script.getScriptText());
                //闭包导致不能序列化 要移除
                Object executeScript = RuleEngine.execute(rules, binding);
                if (executeScript==null){
                    binding.setVariable("decision_rule","miss");
                }

                if (!CollectionUtils.isEmpty(script.getChildren())) {
                    for (ScriptMetadata child : script.getChildren()) {
                        recursiveAndExecute(child, binding, workflowTaskLogMap, currentLevel + 1);
                    }
                }
                return executeScript;
            });
        }
        workflowTaskLogMap.put(currentLevel, workflowTaskLogList);
    }

    private void logScriptExecution(ScriptMetadata script, Binding binding, List<WorkflowTaskLog> workflowTaskLogList, Supplier<Object> scriptExecutor) {

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
//            throw e;
        } finally {
            workflowTaskLog.setAfterRunBinding(new HashMap<>(binding.getVariables()));
            workflowTaskLogList.add(workflowTaskLog);
        }
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

    @Override
    public WorkflowMetadata updateWorkflowName(Integer workflowId, String workflowName) {
        return workflowMetadataRepository.updateWorkflowName(workflowId, workflowName);
    }
}
