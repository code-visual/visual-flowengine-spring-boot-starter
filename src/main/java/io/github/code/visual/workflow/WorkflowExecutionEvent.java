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

import io.github.code.visual.model.WorkflowTaskLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Rich execution event containing complete context for a workflow execution.
 * <p>
 * Used by {@link WorkflowExecutionEventListener} to receive full execution details
 * without needing to infer status, duration, or workflow name from raw logs.
 *
 * @author Levi Li
 * @since 1.3.0
 */
public class WorkflowExecutionEvent {

    private final Integer workflowId;
    private final String workflowName;
    private final Integer revision;
    private final boolean success;
    private final long durationMs;
    private final LocalDateTime executedAt;
    private final String errorMessage;
    private final Map<Integer, List<WorkflowTaskLog>> logs;

    private WorkflowExecutionEvent(Builder builder) {
        this.workflowId = builder.workflowId;
        this.workflowName = builder.workflowName;
        this.revision = builder.revision;
        this.success = builder.success;
        this.durationMs = builder.durationMs;
        this.executedAt = builder.executedAt;
        this.errorMessage = builder.errorMessage;
        this.logs = builder.logs;
    }

    public Integer getWorkflowId() { return workflowId; }
    public String getWorkflowName() { return workflowName; }
    public Integer getRevision() { return revision; }
    public boolean isSuccess() { return success; }
    public long getDurationMs() { return durationMs; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public String getErrorMessage() { return errorMessage; }
    public Map<Integer, List<WorkflowTaskLog>> getLogs() { return logs; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer workflowId;
        private String workflowName;
        private Integer revision;
        private boolean success;
        private long durationMs;
        private LocalDateTime executedAt;
        private String errorMessage;
        private Map<Integer, List<WorkflowTaskLog>> logs;

        public Builder workflowId(Integer workflowId) { this.workflowId = workflowId; return this; }
        public Builder workflowName(String workflowName) { this.workflowName = workflowName; return this; }
        public Builder revision(Integer revision) { this.revision = revision; return this; }
        public Builder success(boolean success) { this.success = success; return this; }
        public Builder durationMs(long durationMs) { this.durationMs = durationMs; return this; }
        public Builder executedAt(LocalDateTime executedAt) { this.executedAt = executedAt; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder logs(Map<Integer, List<WorkflowTaskLog>> logs) { this.logs = logs; return this; }

        public WorkflowExecutionEvent build() { return new WorkflowExecutionEvent(this); }
    }
}
