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

import java.util.List;
import java.util.Map;

/**
 * Optional listener for workflow execution results.
 * <p>
 * Implement this interface and register as a Spring Bean to receive
 * execution logs after each workflow run (e.g., for persistence or monitoring).
 *
 * @author Levi Li
 * @since 2024
 */
public interface WorkflowExecutionListener {

    /**
     * Called after a workflow execution completes (success or failure).
     *
     * @param workflowId the workflow that was executed
     * @param revision   the revision of the workflow definition at execution time
     * @param logs       execution logs grouped by level
     */
    void onExecutionComplete(Integer workflowId, Integer revision,
                             Map<Integer, List<WorkflowTaskLog>> logs);
}
