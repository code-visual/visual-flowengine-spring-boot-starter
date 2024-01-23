package com.github.managetech.scriptcache.repository;

import com.github.managetech.model.WorkflowMetadata;

import java.util.List;

public interface WorkflowMetadataRepository {

    WorkflowMetadata create(WorkflowMetadata workflowMetadata);

    Object deleteByScriptId(String workflowName);

    WorkflowMetadata findByWorkflowName(String workflowName);

    List<String> getMenuWorkflowList();
}
