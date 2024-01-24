package com.github.managetech.workflow;

import com.github.managetech.model.Diagnostic;
import com.github.managetech.model.ScriptRequest;
import com.github.managetech.model.WorkflowMetadata;
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
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
@Service
@SuppressWarnings("all")
public class WorkflowManagerImpl implements WorkflowManager {

    private final CompilerConfiguration config;
    private final WorkflowMetadataRepository workflowMetadataRepository;
    private final Map<String, Class<? extends Script>> parseScriptCache = new ConcurrentHashMap<>();

    @Autowired
    public WorkflowManagerImpl(CompilerConfiguration config, WorkflowMetadataRepository workflowMetadataRepository) {
        this.config = config;
        this.workflowMetadataRepository = workflowMetadataRepository;
    }


    @Override
    public Script parseGroovyScript(String scriptText, Binding binding) throws IOException {

        Class<? extends Script> script = parseScriptCache.get(DigestUtils.md5DigestAsHex(scriptText.getBytes()));
        GroovyClassLoader groovyClassLoader = null;

        if (script == null) {

            try {
                groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
                Class<? extends Script> aClass = groovyClassLoader.parseClass(scriptText);
                parseScriptCache.put(DigestUtils.md5DigestAsHex(scriptText.getBytes()), aClass);
                script = aClass;
            } catch (Exception e) {
                if (e instanceof MultipleCompilationErrorsException) {
                    List<? extends Message> errors = ((MultipleCompilationErrorsException) e).getErrorCollector().getErrors();
                    List<Diagnostic> diagnostics = getDiagnostics(errors);
                    throw new RuntimeException(diagnostics.toString());
                }
                throw new RuntimeException(e);
            } finally {
                if (groovyClassLoader != null) {
                    groovyClassLoader.close();
                }
            }
        }

        return InvokerHelper.createScript(script, binding);
    }

    @Override
    public List<Diagnostic> compileGroovyScript(String code) throws IOException {

        GroovyClassLoader groovyClassLoader = null;
        try {
            groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
            groovyClassLoader.parseClass(code);
            return new ArrayList<>();
        } catch (Exception e) {
            if (!(e instanceof MultipleCompilationErrorsException)) {
                throw new RuntimeException(e);
            }
            List<? extends Message> errors = ((MultipleCompilationErrorsException) e).getErrorCollector().getErrors();
            return getDiagnostics(errors);
        } finally {
            if (groovyClassLoader != null) {
                groovyClassLoader.close();
            }
        }
    }

    @Override
    public Object runGroovyScript(ScriptRequest scriptRequest) throws IOException {

        GroovyClassLoader groovyClassLoader = null;
        try {
            groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
            Class aClass = groovyClassLoader.parseClass(scriptRequest.getCode());
            return InvokerHelper.createScript(aClass, new Binding(scriptRequest.getInputValues())).run();
        } catch (Exception e) {
            if (!(e instanceof MultipleCompilationErrorsException)) {
                throw new RuntimeException(e);
            }
            List<? extends Message> errors = ((MultipleCompilationErrorsException) e).getErrorCollector().getErrors();
            return getDiagnostics(errors);

        } finally {
            if (groovyClassLoader != null) {
                groovyClassLoader.close();
            }
        }

    }

    @Override
    public WorkflowMetadata getWorkflowMetadataByName(String workflowName) {
        return workflowMetadataRepository.findByWorkflowName(workflowName);
    }

    @Override
    public Object createWorkflow(WorkflowMetadata workflowMetadata) {
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

    private static List<Diagnostic> getDiagnostics(List<? extends Message> errors) {
        List<Diagnostic> diagnostics = new java.util.ArrayList<>();
        for (Message error : errors) {
            if (error instanceof SyntaxErrorMessage) {
                SyntaxException cause = ((SyntaxErrorMessage) error).getCause();
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setMessage(cause.getMessage());
                diagnostic.setStartLineNumber(cause.getStartLine());
                diagnostic.setStartColumn(cause.getStartColumn());
                diagnostic.setEndLineNumber(cause.getEndLine());
                diagnostic.setEndColumn(cause.getEndColumn());
                diagnostics.add(diagnostic);
                continue;
            }
            if (error instanceof ExceptionMessage) {
                Exception cause = ((ExceptionMessage) error).getCause();
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setMessage(cause.getMessage());
                diagnostic.setStartLineNumber(cause.getStackTrace()[0].getLineNumber());
                diagnostic.setEndLineNumber(cause.getStackTrace()[0].getLineNumber());
                diagnostics.add(diagnostic);
                continue;
            } else {
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setMessage("unkonw error");
                diagnostics.add(diagnostic);
            }
        }
        return diagnostics;
    }

}