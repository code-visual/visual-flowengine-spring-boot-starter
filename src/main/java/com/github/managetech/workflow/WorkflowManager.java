package com.github.managetech.workflow;


import com.github.managetech.model.Diagnostic;
import com.github.managetech.model.ScriptRequest;
import com.github.managetech.model.WorkflowMetadata;
import groovy.lang.Binding;
import groovy.lang.Script;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
public interface WorkflowManager {
    void execute(Integer workflowId, Map inputVariables) ;

    Script parseGroovyScript(String scriptText, Binding binding);

    List<Diagnostic> compileGroovyScript(String code);

    Object testGroovyScript(ScriptRequest scriptRequest);

    WorkflowMetadata getWorkflowMetadataById(Integer workflowName);

    Object createWorkflow(WorkflowMetadata workflowMetadata);

    Object deleteWorkflowMetadata(Integer workflowId);

    List<WorkflowMetadata> getMenuWorkflowNameList();

    void resetGroovyClassLoader() throws IOException;

    WorkflowMetadata updateWorkflowMetadata(WorkflowMetadata workflowMetadata);
}
