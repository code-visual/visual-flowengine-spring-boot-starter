package com.github.managetech.workflow;


import com.github.managetech.model.Diagnostic;
import com.github.managetech.model.ScriptRequest;
import com.github.managetech.model.WorkflowMetadata;
import groovy.lang.Binding;
import groovy.lang.Script;

import java.io.IOException;
import java.util.List;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
public interface WorkflowManager {

    Script parseGroovyScript(String scriptText, Binding binding) throws IOException;

    List<Diagnostic> compileGroovyScript(String code) throws IOException;

    Object testGroovyScript(ScriptRequest scriptRequest) throws IOException;

    WorkflowMetadata getWorkflowMetadataByName(String workflowName);

    Object createWorkflow(WorkflowMetadata workflowMetadata);

    Object deleteWorkflowMetadata(String workflowName);

    List<String> getMenuWorkflowNameList();

    void resetGroovyClassLoader();
}
