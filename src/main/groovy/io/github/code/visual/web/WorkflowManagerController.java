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
package io.github.code.visual.web;

import io.github.code.visual.model.DebugRequest;
import io.github.code.visual.model.ScriptRequest;
import io.github.code.visual.model.WorkflowIdAndName;
import io.github.code.visual.model.WorkflowMetadata;
import io.github.code.visual.model.WorkflowTaskLog;
import io.github.code.visual.workflow.WorkflowManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@ConditionalOnProperty(name = "visual.flow.enableDefaultApi", havingValue = "true", matchIfMissing = true)
public class WorkflowManagerController {

    private final WorkflowManager workflowManager;

    @Autowired
    public WorkflowManagerController(WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    @PostMapping("${visual.flow.executeWorkflowApiPath:/api/engine/workflow/execute}")
    public Map<Integer, List<WorkflowTaskLog>> executeWorkflow(@RequestParam("workflowId") Integer workflowId, @RequestBody Map inputVariables) {
        return workflowManager.startWorkflow(workflowId, inputVariables);
    }

    @PostMapping("${visual.flow.debugWorkflowApiPath:/api/engine/workflow/debug}")
    public Map<Integer, List<WorkflowTaskLog>> debugWorkflow(@RequestBody DebugRequest debugRequest) {
        return workflowManager.debug(debugRequest);
    }

    @PostMapping("${visual.flow.createWorkflowApiPath:/api/engine/workflow}")
    public Object createWorkflow(@RequestBody WorkflowMetadata workflowMetadata) {
        return workflowManager.createWorkflow(workflowMetadata);
    }

    @DeleteMapping("${visual.flow.deleteWorkflowApiPath:/api/engine/workflow}")
    public Object deleteWorkflowMetadata(@RequestParam("workflowId") Integer workflowId) {
        return workflowManager.deleteWorkflowMetadata(workflowId);
    }

    @PutMapping("${visual.flow.updateWorkflowApiPath:/api/engine/workflow}")
    public Object updateWorkflowMetadata(@RequestBody WorkflowMetadata workflowMetadata) {
        return workflowManager.updateWorkflowMetadata(workflowMetadata);
    }

    @GetMapping("${visual.flow.listWorkflowsApiPath:/api/engine/workflowList}")
    public List<WorkflowIdAndName> listMenuWorkflow() {
        return workflowManager.getMenuWorkflowNameList();
    }

    @PostMapping("${visual.flow.compileScriptApiPath:/api/engine/groovyScript/compile}")
    public Object compileGroovyScript(@RequestBody ScriptRequest runRequest) {
        return workflowManager.compileGroovyScript(runRequest.getCode());
    }

    @GetMapping("${visual.flow.getWorkflowMetadataApiPath:/api/engine/workflow}")
    public WorkflowMetadata getWorkflowMetadata(@RequestParam("workflowId") Integer workflowId) {
        return workflowManager.getWorkflowMetadataById(workflowId);
    }
}

