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

import io.github.code.visual.config.VisualFlowProperties;
import io.github.code.visual.model.DebugRequest;
import io.github.code.visual.model.Diagnostic;
import io.github.code.visual.model.ScriptRequest;
import io.github.code.visual.model.WorkflowIdAndName;
import io.github.code.visual.model.WorkflowMetadata;
import io.github.code.visual.model.WorkflowTaskLog;
import io.github.code.visual.workflow.WorkflowEngine;
import io.github.code.visual.workflow.WorkflowRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Built-in REST controller for workflow management and execution.
 *
 * @author Levi Li
 * @since 09/18/2023
 */
@RestController
public class WorkflowController {

    private final WorkflowEngine engine;
    private final WorkflowRepository repository;
    private final VisualFlowProperties properties;

    public WorkflowController(WorkflowEngine engine,
                              WorkflowRepository repository,
                              VisualFlowProperties properties) {
        this.engine = engine;
        this.repository = repository;
        this.properties = properties;
    }

    // ── Config endpoint (for frontend) ──

    @GetMapping("${visual.flow.basePath:/visualflow}/api/config")
    public Map<String, Object> config() {
        Map<String, Object> config = new HashMap<>();
        config.put("basePath", properties.getBasePath());
        config.put("workflowsApiPath", properties.getWorkflowsApiPath());
        config.put("executeApiPath", properties.getExecuteApiPath());
        config.put("debugApiPath", properties.getDebugApiPath());
        config.put("compileApiPath", properties.getCompileApiPath());
        return config;
    }

    // ── Workflow CRUD ──

    @GetMapping("${visual.flow.basePath:/visualflow}/api/workflows")
    public List<WorkflowIdAndName> listWorkflows() {
        return repository.findAll();
    }

    @PostMapping("${visual.flow.basePath:/visualflow}/api/workflows")
    public WorkflowMetadata createWorkflow(@RequestBody WorkflowMetadata metadata) {
        return engine.createWorkflow(metadata);
    }

    @GetMapping("${visual.flow.basePath:/visualflow}/api/workflows/{id}")
    public WorkflowMetadata getWorkflow(@PathVariable("id") Integer id) {
        return engine.getWorkflow(id);
    }

    @PutMapping("${visual.flow.basePath:/visualflow}/api/workflows/{id}")
    public WorkflowMetadata updateWorkflow(@PathVariable("id") Integer id,
                                           @RequestBody WorkflowMetadata metadata) {
        metadata.setWorkflowId(id);
        return engine.updateWorkflow(metadata);
    }

    @DeleteMapping("${visual.flow.basePath:/visualflow}/api/workflows/{id}")
    public void deleteWorkflow(@PathVariable("id") Integer id) {
        engine.deleteWorkflow(id);
    }

    // ── Execution ──

    @PostMapping("${visual.flow.basePath:/visualflow}/api/workflows/execute")
    public Map<Integer, List<WorkflowTaskLog>> executeWorkflow(
            @RequestBody Map<String, Object> request) {
        Integer workflowId = (Integer) request.get("workflowId");
        @SuppressWarnings("unchecked")
        Map<String, Object> inputVariables = (Map<String, Object>) request.getOrDefault("inputVariables", new HashMap<>());
        return engine.execute(workflowId, inputVariables);
    }

    @PostMapping("${visual.flow.basePath:/visualflow}/api/workflows/debug")
    public Map<Integer, List<WorkflowTaskLog>> debugWorkflow(@RequestBody DebugRequest debugRequest) {
        return engine.debug(debugRequest);
    }

    // ── Script compilation ──

    @PostMapping("${visual.flow.basePath:/visualflow}/api/script/compile")
    public List<Diagnostic> compileScript(@RequestBody ScriptRequest request) {
        return engine.compileScript(request.getCode());
    }
}
