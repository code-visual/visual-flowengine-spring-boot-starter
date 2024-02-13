package com.github.managetech.workflow;

import com.github.managetech.model.WorkflowMetadata;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Levi Li
 * @since 01/24/2024
 */
@Repository
public class TempWorkflowMetadataRepositoryImpl implements WorkflowMetadataRepository {

    AtomicInteger atomicInteger = new AtomicInteger(0);
    private final Map<Integer, WorkflowMetadata> workflowMetadataMap = new ConcurrentHashMap<>();

    @Override
    public WorkflowMetadata create(WorkflowMetadata workflowMetadata) {
        workflowMetadata.setWorkflowId(atomicInteger.incrementAndGet());
        return workflowMetadataMap.put(workflowMetadata.getWorkflowId(), workflowMetadata);
    }

    @Override
    public Object deleteByWorkflowId(Integer workflowId) {
        return workflowMetadataMap.remove(workflowId);
    }

    @Override
    public WorkflowMetadata findByWorkflowId(Integer workflowId) {
        return workflowMetadataMap.get(workflowId);
    }

    @Override
    public List<WorkflowMetadata> getMenuWorkflowList() {
        return new ArrayList<>(workflowMetadataMap.values());
    }

    @Override
    public WorkflowMetadata updateWorkflowMetadata(WorkflowMetadata workflowMetadata) {
        return workflowMetadataMap.put(workflowMetadata.getWorkflowId(), workflowMetadata);
    }

    @Override
    public WorkflowMetadata updateWorkflowName(Integer workflowId, String workflowName) {
        WorkflowMetadata workflowMetadata = workflowMetadataMap.get(workflowId);
        workflowMetadata.setWorkflowName(workflowName);
        return workflowMetadataMap.put(workflowId, workflowMetadata);
    }
}
