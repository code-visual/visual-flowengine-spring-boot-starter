package com.github.managetech.scriptcache.repository;

import com.github.managetech.model.WorkflowMetadata;

import java.util.List;

public interface WorkflowMetadataRepository {

    WorkflowMetadata save(WorkflowMetadata workflowMetadata);

    Object deleteByScriptId(String scriptId);

    WorkflowMetadata findById(String scriptId);

    List<WorkflowMetadata> findAll();
}
