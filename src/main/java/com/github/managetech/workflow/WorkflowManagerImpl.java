package com.github.managetech.workflow;

import com.github.managetech.model.Diagnostic;
import com.github.managetech.model.ScriptMetadata;
import com.github.managetech.model.ScriptRequest;
import com.github.managetech.model.ScriptType;
import com.github.managetech.model.WorkflowMetadata;
import com.github.managetech.utils.CommonUtils;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
@Service
@SuppressWarnings("all")
public class WorkflowManagerImpl implements WorkflowManager {

    private final CompilerConfiguration config;
    private final WorkflowMetadataRepository workflowMetadataRepository;
    private GroovyClassLoader groovyClassLoader;

    @Autowired
    public WorkflowManagerImpl(CompilerConfiguration config, WorkflowMetadataRepository workflowMetadataRepository) {
        this.config = config;
        this.workflowMetadataRepository = workflowMetadataRepository;
        groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);

    }


    @Override
    public Script parseGroovyScript(String scriptText, Binding binding) throws IOException {
        Class<? extends Script> aClass = this.groovyClassLoader.parseClass(scriptText);
        return InvokerHelper.createScript(aClass, binding);
    }

    /**
     * 编译的时候肯定有很多大量的,细碎的脚本。应该用临时的groovyClassLoader来编译
     * @param code
     * @return
     * @throws IOException
     */
    @Override
    public List<Diagnostic> compileGroovyScript(String code) throws IOException {

        GroovyClassLoader tempGroovyClassLoader = null;
        try {
            tempGroovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
            tempGroovyClassLoader.parseClass(code);
            return new ArrayList<>();
        } catch (Exception e) {
            if (!(e instanceof MultipleCompilationErrorsException)) {
                throw new RuntimeException(e);
            }
            List<? extends Message> errors = ((MultipleCompilationErrorsException) e).getErrorCollector().getErrors();
            return CommonUtils.getDiagnostics(errors);
        } finally {
            if (tempGroovyClassLoader != null) {
                tempGroovyClassLoader.close();
            }
        }
    }

    /**
     * 测试脚本的时候，应该用临时的groovyClassLoader来编译
     * @param scriptRequest
     * @return
     * @throws IOException
     */
    @Override
    public Object testGroovyScript(ScriptRequest scriptRequest) throws IOException {
        GroovyClassLoader tempGroovyClassLoader = null;
        try {
            tempGroovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
            Class aClass = tempGroovyClassLoader.parseClass(scriptRequest.getCode());
            return InvokerHelper.createScript(aClass, new Binding(scriptRequest.getInputValues())).run();
        } catch (Exception e) {
            if (!(e instanceof MultipleCompilationErrorsException)) {
                throw new RuntimeException(e);
            }
            List<? extends Message> errors = ((MultipleCompilationErrorsException) e).getErrorCollector().getErrors();
            return CommonUtils.getDiagnostics(errors);
        } finally {
            if (tempGroovyClassLoader != null) {
                tempGroovyClassLoader.close();
            }
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
    public void resetGroovyClassLoader() {

        if (this.groovyClassLoader != null) {
            try {
                this.groovyClassLoader.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        this.groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
    }
}
