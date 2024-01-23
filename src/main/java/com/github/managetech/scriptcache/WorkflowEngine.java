package com.github.managetech.scriptcache;


import com.github.managetech.model.Diagnostic;
import com.github.managetech.model.RunScriptRequest;
import com.github.managetech.model.WorkflowMetadata;
import groovy.lang.Binding;
import groovy.lang.Script;

import java.io.IOException;
import java.util.List;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
public interface WorkflowEngine {

    Script parseGroovyScript(String scriptText, Binding binding) throws IOException;

    List<Diagnostic> compileGroovyScript(String code) throws IOException;

    Object runGroovyScript(RunScriptRequest runScriptRequest) throws IOException;

    WorkflowMetadata getWorkflowMetadataByName(String workflowName);

    Object createWorkflow(WorkflowMetadata workflowMetadata);

    Object deleteWorkflowMetadata(String workflowName);

    List<String> getMenuWorkflowNameList();
}
