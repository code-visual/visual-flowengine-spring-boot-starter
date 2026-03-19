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

import io.github.code.visual.model.WorkflowIdAndName;
import io.github.code.visual.model.WorkflowMetadata;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Default in-memory implementation of {@link WorkflowRepository}.
 * <p>
 * Data is lost on application restart. Suitable for development and testing only.
 *
 * @author Levi Li
 * @since 01/24/2024
 */
public class InMemoryWorkflowRepository implements WorkflowRepository {

    private final AtomicInteger idGenerator = new AtomicInteger(10000);
    private final Map<Integer, WorkflowMetadata> store = new ConcurrentHashMap<>();

    @Override
    public void save(WorkflowMetadata metadata) {
        if (metadata.getWorkflowId() == null) {
            // New workflow
            metadata.setWorkflowId(idGenerator.incrementAndGet());
            metadata.setRevision(1);
            metadata.setCreatedAt(new Date());
            metadata.setUpdatedAt(new Date());
        } else {
            // Update existing
            WorkflowMetadata existing = store.get(metadata.getWorkflowId());
            if (existing == null) {
                throw new IllegalArgumentException(
                        "Workflow with id " + metadata.getWorkflowId() + " does not exist");
            }
            metadata.setRevision(existing.getRevision() + 1);
            metadata.setCreatedAt(existing.getCreatedAt());
            metadata.setUpdatedAt(new Date());
        }
        store.put(metadata.getWorkflowId(), metadata);
    }

    @Override
    public void deleteById(Integer workflowId) {
        store.remove(workflowId);
    }

    @Override
    public WorkflowMetadata findById(Integer workflowId) {
        return store.get(workflowId);
    }

    @Override
    public WorkflowMetadata findByName(String workflowName) {
        return store.values().stream()
                .filter(m -> m.getWorkflowName().equals(workflowName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<WorkflowIdAndName> findAll() {
        return store.values().stream()
                .map(m -> new WorkflowIdAndName(m.getWorkflowId(), m.getWorkflowName()))
                .collect(Collectors.toList());
    }
}
