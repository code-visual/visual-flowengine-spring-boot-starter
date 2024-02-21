package io.github.code.visual.workflow;


import io.github.code.visual.model.DebugRequest;
import io.github.code.visual.model.Diagnostic;
import io.github.code.visual.model.WorkflowMetadata;
import io.github.code.visual.model.WorkflowTaskLog;
import groovy.lang.Binding;
import groovy.lang.Script;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
public interface WorkflowManager {
    Map<Integer, List<WorkflowTaskLog>> startWorkflow(Integer workflowId, Map inputVariables);

    Map<Integer, List<WorkflowTaskLog>> startWorkflow(String workflowName, Map inputVariables);

    Map<Integer, List<WorkflowTaskLog>> debug(DebugRequest debugRequest);

    public void localTestScript(List<File> files, Binding binding) throws IOException;

    Script parseGroovyScript(String scriptText, Binding binding);

    List<Diagnostic> compileGroovyScript(String code);

    WorkflowMetadata getWorkflowMetadataById(Integer workflowName);

    Object createWorkflow(WorkflowMetadata workflowMetadata);

    Object deleteWorkflowMetadata(Integer workflowId);

    List<WorkflowMetadata> getMenuWorkflowNameList();

    void resetGroovyClassLoader() throws IOException;

    WorkflowMetadata updateWorkflowMetadata(WorkflowMetadata workflowMetadata);
}
