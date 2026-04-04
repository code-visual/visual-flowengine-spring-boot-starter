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

/**
 * Enhanced listener for workflow execution events.
 * <p>
 * Provides a richer callback with complete execution context via
 * {@link WorkflowExecutionEvent}, including workflow name, execution status,
 * duration, timestamp, and error details.
 * <p>
 * Implement this interface and register as a Spring Bean. The engine calls
 * listeners <b>synchronously</b> after execution completes — implementations
 * should return quickly or delegate to an async executor.
 *
 * @author Levi Li
 * @since 1.3.0
 * @see WorkflowExecutionEvent
 * @see WorkflowExecutionListener
 */
public interface WorkflowExecutionEventListener {

    /**
     * Called after a workflow execution completes (success or failure).
     *
     * @param event the execution event with complete context
     */
    void onExecutionComplete(WorkflowExecutionEvent event);
}
