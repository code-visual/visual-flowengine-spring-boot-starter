package com.github.managetech.workflow;

import com.github.managetech.model.WorkflowMetadata;

import java.util.List;

public interface WorkflowMetadataRepository {

    WorkflowMetadata create(WorkflowMetadata workflowMetadata);

    Object deleteByWorkflowId(Integer workflowName);

    WorkflowMetadata findByWorkflowId(Integer workflowId);

    List<WorkflowMetadata> getMenuWorkflowList();
    WorkflowMetadata updateWorkflowMetadata(WorkflowMetadata workflowMetadata);

}
