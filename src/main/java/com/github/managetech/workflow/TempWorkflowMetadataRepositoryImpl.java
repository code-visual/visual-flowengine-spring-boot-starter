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
        workflowMetadata.setWorkflowId(atomicInteger.incrementAndGet() + 10000);
        workflowMetadataMap.put(workflowMetadata.getWorkflowId(), workflowMetadata);
        return workflowMetadata;
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
    public WorkflowMetadata updateWorkflowMetadata(WorkflowMetadata updatedWorkflowMetadata) {
        WorkflowMetadata existingWorkflowMetadata = workflowMetadataMap.get(updatedWorkflowMetadata.getWorkflowId());

        if (existingWorkflowMetadata == null) {

            throw new IllegalArgumentException("WorkflowMetadata with id " + updatedWorkflowMetadata.getWorkflowId() + " does not exist");
        }

        if (updatedWorkflowMetadata.getWorkflowName() != null) {
            existingWorkflowMetadata.setWorkflowName(updatedWorkflowMetadata.getWorkflowName());
        }
        if (updatedWorkflowMetadata.getWorkflowParameters() != null) {
            existingWorkflowMetadata.setWorkflowParameters(updatedWorkflowMetadata.getWorkflowParameters());
        }
        if (updatedWorkflowMetadata.getWorkflowPurpose() != null) {
            existingWorkflowMetadata.setWorkflowPurpose(updatedWorkflowMetadata.getWorkflowPurpose());
        }
        if (updatedWorkflowMetadata.getRemark() != null) {
            existingWorkflowMetadata.setRemark(updatedWorkflowMetadata.getRemark());
        }
        if (updatedWorkflowMetadata.getScriptMetadata() != null) {
            existingWorkflowMetadata.setScriptMetadata(updatedWorkflowMetadata.getScriptMetadata());
        }

        workflowMetadataMap.put(updatedWorkflowMetadata.getWorkflowId(), existingWorkflowMetadata);

        return existingWorkflowMetadata;
    }

    @Override
    public WorkflowMetadata findByWorkflowName(String workflowName) {
        return workflowMetadataMap.values().stream().filter(workflowMetadata -> workflowMetadata.getWorkflowName().equals(workflowName)).findFirst().orElseThrow(() -> new RuntimeException("WorkflowMetadata with name " + workflowName + " does not exist"));
    }

}
