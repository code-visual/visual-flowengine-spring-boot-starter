/*
 * Copyright (c) 2023-2024, levi li (levi.lideng@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.code.visual.workflow;

import io.github.code.visual.model.WorkflowMetadata;
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
