package com.github.managetech.web;

import com.github.managetech.config.VisualFlowProperties;
import com.github.managetech.model.ScriptRequest;
import com.github.managetech.model.WorkflowMetadata;
import com.github.managetech.scriptcache.WorkflowEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000","http://10.17.80.189:3000"})
@RestController
@ConditionalOnProperty(name = "visual.flow.enableDefaultApi", havingValue = "true", matchIfMissing = true)
public class EngineController {

    private final WorkflowEngine workflowEngine;

    @Autowired
    public EngineController(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    @PostMapping(VisualFlowProperties.DEFAULT_CREATE_WORKFLOW)
    public Object createWorkflow(@RequestBody WorkflowMetadata workflowMetadata) {
        return workflowEngine.createWorkflow(workflowMetadata);
    }

    @DeleteMapping(VisualFlowProperties.DEFAULT_DELETE_WORKFLOW)
    public Object deleteWorkflowMetadata(@RequestParam String workflowName) {
        return workflowEngine.deleteWorkflowMetadata(workflowName);
    }

    @GetMapping(VisualFlowProperties.DEFAULT_LIST_WORKFLOWS)
    public List<String> listMenuWorkflow() {
        return workflowEngine.getMenuWorkflowNameList();
    }

    @PostMapping(VisualFlowProperties.DEFAULT_COMPILE_GROOVY_SCRIPT)
    public Object compileGroovyScript(@RequestBody ScriptRequest runRequest) throws IOException {
        return workflowEngine.compileGroovyScript(runRequest.getCode());
    }

    @PostMapping(VisualFlowProperties.DEFAULT_RUN_GROOVY_SCRIPT)
    public Object runGroovyScript(@RequestBody ScriptRequest runRequest) throws IOException {
        return workflowEngine.runGroovyScript(runRequest);
    }

    @GetMapping(VisualFlowProperties.DEFAULT_GET_WORKFLOW_METADATA)
    public WorkflowMetadata getWorkflowMetadata(@RequestParam String workflowName) {
        return workflowEngine.getWorkflowMetadataByName(workflowName);
    }
}

