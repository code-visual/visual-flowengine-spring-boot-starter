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

import groovy.lang.GroovyClassLoader;
import io.github.code.visual.model.ScriptMetadata;
import io.github.code.visual.model.WorkflowIdAndName;
import io.github.code.visual.model.WorkflowMetadata;
import io.github.code.visual.model.WorkflowTaskLog;

import java.util.List;
import java.util.Map;

public interface WorkflowMetadataRepository {

    void create(WorkflowMetadata workflowMetadata);

    Object deleteByWorkflowId(Integer workflowName);

    WorkflowMetadata findByWorkflowId(Integer workflowId);

    List<WorkflowIdAndName> getMenuWorkflowList();

    WorkflowMetadata updateWorkflowMetadata(WorkflowMetadata workflowMetadata);

    WorkflowMetadata findByWorkflowName(String workflowName);

    default Class<?> getClassFromCache(GroovyClassLoader groovyClassLoader, ScriptMetadata scriptMetadata) {
        return null;
    }

    default void asyncSaveWorkflowTaskLog(Map<Integer, List<WorkflowTaskLog>> workflowTaskLogMap) {

    }

}
