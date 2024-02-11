package com.github.managetech.workflow;

import com.github.managetech.model.*;
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

import javax.script.ScriptException;
import java.io.IOException;
import java.util.*;
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
        List<WorkflowTaskLog> workflowTaskLogList = new ArrayList<>();
        try {
            this.recursiveAndExecute(scriptMetadata, new Binding(inputVariables), workflowTaskLogList);
        } finally {
            logger.info("保存日志:{}", workflowTaskLogList);
        }
    }

    @Override
    public void debug(DebugRequest debugRequest) {

        if (debugRequest.getScriptMetadata() == null) {
           return;
        }
        this.recursiveAndExecute(debugRequest.getScriptMetadata(), new Binding(debugRequest.getInputValues()), null);
        try {
            resetGroovyClassLoader();
        } catch (IOException e) {
            logger.error("重置GroovyClassLoader失败", e);
        }

    }

    public void recursiveAndExecute(ScriptMetadata script, Binding binding, List<WorkflowTaskLog> workflowTaskLogList) {

        if (script.getScriptType() == ScriptType.Start) {

            if (!CollectionUtils.isEmpty(workflowTaskLogList)) {
                Map StartRunBinding = new HashMap<>(binding.getVariables());
                WorkflowTaskLog workflowTaskLog = new WorkflowTaskLog();
                workflowTaskLog.setScriptId(script.getScriptId());
                workflowTaskLog.setBeforeRunBinding(StartRunBinding);
                workflowTaskLog.setAfterRunBinding(StartRunBinding);
                workflowTaskLog.setScriptRunStatus(ScriptRunStatus.Start);
                workflowTaskLog.setScriptRunResult(null);
                workflowTaskLog.setScriptRunTime(new Date());
                workflowTaskLog.setScriptRunError(null);
                workflowTaskLogList.add(workflowTaskLog);
            }

            for (ScriptMetadata child : script.getChildren()) {
                recursiveAndExecute(child, binding, workflowTaskLogList);
            }

        } else if (script.getScriptType() == ScriptType.End) {

            if (!CollectionUtils.isEmpty(workflowTaskLogList)) {
                Map endBinding = new HashMap<>(binding.getVariables());
                WorkflowTaskLog workflowTaskLog = new WorkflowTaskLog();
                workflowTaskLog.setScriptId(script.getScriptId());
                workflowTaskLog.setBeforeRunBinding(endBinding);
                workflowTaskLog.setAfterRunBinding(endBinding);
                workflowTaskLog.setScriptRunStatus(ScriptRunStatus.End);
                workflowTaskLog.setScriptRunResult(null);
                workflowTaskLog.setScriptRunTime(new Date());
                workflowTaskLog.setScriptRunError(null);
                workflowTaskLogList.add(workflowTaskLog);
            }


        } else if (script.getScriptType() == ScriptType.Condition) {
            logScriptExecution(script, binding, workflowTaskLogList, () -> {
                Object executeScript = this.executeScript(script, binding);
                if (executeScript instanceof Boolean && (Boolean) executeScript) {
                    for (ScriptMetadata child : script.getChildren()) {
                        recursiveAndExecute(child, binding, workflowTaskLogList);
                    }
                }
                return executeScript;
            });


        } else if (script.getScriptType() == ScriptType.Script) {
            logScriptExecution(script, binding, workflowTaskLogList, () -> this.executeScript(script, binding));
            for (ScriptMetadata scriptChild : script.getChildren()) {
                recursiveAndExecute(scriptChild, binding, workflowTaskLogList);
            }
        }
    }

    private void logScriptExecution(ScriptMetadata script, Binding binding, List<WorkflowTaskLog> workflowTaskLogList, Supplier<Object> scriptExecutor) {

        if (CollectionUtils.isEmpty(workflowTaskLogList)) {
            scriptExecutor.get();
        } else {
            HashMap beforeRunBinding = new HashMap<>(binding.getVariables());
            WorkflowTaskLog workflowTaskLog = new WorkflowTaskLog();
            workflowTaskLog.setScriptId(script.getScriptId());
            workflowTaskLog.setBeforeRunBinding(beforeRunBinding);
            workflowTaskLog.setScriptRunTime(new Date());

            try {
                Object result = scriptExecutor.get(); // 执行脚本
                workflowTaskLog.setScriptRunStatus(ScriptRunStatus.Success);
                workflowTaskLog.setScriptRunResult(result);
            } catch (Exception e) {
                workflowTaskLog.setScriptRunStatus(ScriptRunStatus.Error);
                workflowTaskLog.setScriptRunError(e.getMessage());
                throw e;
            } finally {
                workflowTaskLog.setAfterRunBinding(new HashMap<>(binding.getVariables()));
                workflowTaskLogList.add(workflowTaskLog);
            }
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

    /**
     * 测试脚本的时候，应该用临时的groovyClassLoader来编译
     *
     * @param scriptRequest
     * @return
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object testGroovyScript(ScriptRequest scriptRequest) {
        try (GroovyClassLoader tempGroovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config)) {
            Class aClass = tempGroovyClassLoader.parseClass(scriptRequest.getCode());
            return InvokerHelper.createScript(aClass, new Binding(scriptRequest.getInputValues())).run();
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
