package com.github.managetech.workflow;

import com.github.managetech.model.WorkflowMetadata;

import java.util.List;

public interface WorkflowMetadataRepository {

    WorkflowMetadata create(WorkflowMetadata workflowMetadata);

    Object deleteByWorkflowName(String workflowName);

    WorkflowMetadata findByWorkflowName(String workflowName);

    List<String> getMenuWorkflowList();
}
