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


import io.github.code.visual.groovy.CustomBinding;
import io.github.code.visual.model.*;
import groovy.lang.Binding;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
public interface WorkflowManager {
    Map<Integer, List<WorkflowTaskLog>> startWorkflow(Integer workflowId, Map inputVariables);

    Map<Integer, List<WorkflowTaskLog>> startWorkflow(String workflowName, Map inputVariables);

    Map<Integer, List<WorkflowTaskLog>> debug(DebugRequest debugRequest);

    void localTestScript(List<File> files, Binding binding) throws IOException;

    Object executeScript(ScriptMetadata scriptText, CustomBinding binding);

    List<Diagnostic> compileGroovyScript(String code);

    WorkflowMetadata getWorkflowMetadataById(Integer workflowName);

    Object createWorkflow(WorkflowMetadata workflowMetadata);

    Object deleteWorkflowMetadata(Integer workflowId);

    List<WorkflowIdAndName> getMenuWorkflowNameList();

    void resetGroovyClassLoader() throws IOException;

    WorkflowMetadata updateWorkflowMetadata(WorkflowMetadata workflowMetadata);
}
