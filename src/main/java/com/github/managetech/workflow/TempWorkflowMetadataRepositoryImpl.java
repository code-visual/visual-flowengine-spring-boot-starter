package com.github.managetech.workflow;

import com.github.managetech.model.WorkflowMetadata;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Levi Li
 * @since 01/24/2024
 */
@Repository
public class TempWorkflowMetadataRepositoryImpl implements WorkflowMetadataRepository {

    private final Map<String, WorkflowMetadata> workflowMetadataMap = new ConcurrentHashMap<>();

    @Override
    public WorkflowMetadata create(WorkflowMetadata workflowMetadata) {

        return workflowMetadataMap.put(workflowMetadata.getWorkflowName(), workflowMetadata);
    }

    @Override
    public Object deleteByWorkflowName(String workflowName) {
        return workflowMetadataMap.remove(workflowName);
    }

    @Override
    public WorkflowMetadata findByWorkflowName(String workflowName) {
        return workflowMetadataMap.get(workflowName);
    }

    @Override
    public List<WorkflowMetadata> getMenuWorkflowList() {
        return new ArrayList<>(workflowMetadataMap.values());
    }

    @Override
    public WorkflowMetadata updateWorkflowMetadata(WorkflowMetadata workflowMetadata) {
        return workflowMetadataMap.put(workflowMetadata.getWorkflowName(), workflowMetadata);
    }
}
