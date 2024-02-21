package io.github.code.visual.workflow;

import io.github.code.visual.model.WorkflowMetadata;

import java.util.List;

public interface WorkflowMetadataRepository {

    WorkflowMetadata create(WorkflowMetadata workflowMetadata);

    Object deleteByWorkflowId(Integer workflowName);

    WorkflowMetadata findByWorkflowId(Integer workflowId);

    List<WorkflowMetadata> getMenuWorkflowList();

    WorkflowMetadata updateWorkflowMetadata(WorkflowMetadata workflowMetadata);

    WorkflowMetadata findByWorkflowName(String workflowName);
}
