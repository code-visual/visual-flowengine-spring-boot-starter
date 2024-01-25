package com.github.managetech.workflow;

import com.github.managetech.model.Diagnostic;
import com.github.managetech.model.ScriptMetadata;
import com.github.managetech.model.ScriptRequest;
import com.github.managetech.model.ScriptRunStatus;
import com.github.managetech.model.ScriptType;
import com.github.managetech.model.WorkflowMetadata;
import com.github.managetech.model.WorkflowTaskLog;
import com.github.managetech.utils.CommonUtils;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
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
    private static final Logger logger = LoggerFactory.getLogger(WorkflowManagerImpl.class);

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
    public void execute(String workflowName, Map inputVariables) {
        WorkflowMetadata workflowMetadata = workflowMetadataRepository.findByWorkflowName(workflowName);
        ScriptMetadata scriptMetadata = workflowMetadata.getScriptMetadata();
        List<WorkflowTaskLog> workflowTaskLogList = new ArrayList<>();
        try {
            this.recursiveAndExecute(scriptMetadata, new Binding(inputVariables), workflowTaskLogList);
        } finally {
            logger.info("保存日志:{}", workflowTaskLogList);
        }
    }

    public void recursiveAndExecute(ScriptMetadata script, Binding binding, List<WorkflowTaskLog> workflowTaskLogList) {

        if (script.getScriptType() == ScriptType.Start) {

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
            for (ScriptMetadata child : script.getChildren()) {
                recursiveAndExecute(child, binding, workflowTaskLogList);
            }

        } else if (script.getScriptType() == ScriptType.End) {

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


    @SuppressWarnings("rawtypes,unchecked")
    private Object executeScript(ScriptMetadata metadata, Binding binding) {
        Class<?> aClass = groovyClassLoader.parseClass(metadata.getScriptContent());
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
    public WorkflowMetadata getWorkflowMetadataByName(String workflowName) {
        return workflowMetadataRepository.findByWorkflowName(workflowName);
    }

    @Override
    public Object createWorkflow(WorkflowMetadata workflowMetadata) {
        ScriptMetadata metadata = new ScriptMetadata();
        metadata.setScriptId("1");
        //这里应该可以看到workflow 有哪些传入参数的描述
        metadata.setScriptContent("");
        metadata.setScriptName("Start");
        metadata.setScriptType(ScriptType.Start);
        metadata.setChildren(null);

        workflowMetadata.setScriptMetadata(metadata);
        workflowMetadata.setCreateTime(new Date());
        return workflowMetadataRepository.create(workflowMetadata);
    }

    @Override
    public Object deleteWorkflowMetadata(String workflowName) {
        return workflowMetadataRepository.deleteByWorkflowName(workflowName);
    }

    @Override
    public List<String> getMenuWorkflowNameList() {
        return workflowMetadataRepository.getMenuWorkflowList();
    }


    @Override
    public void resetGroovyClassLoader() throws IOException {

        if (this.groovyClassLoader != null) {
            this.groovyClassLoader.close();
        }
        this.groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
    }
}
