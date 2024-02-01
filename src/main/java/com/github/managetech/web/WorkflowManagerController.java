package com.github.managetech.web;

import com.github.managetech.config.VisualFlowProperties;
import com.github.managetech.model.ScriptRequest;
import com.github.managetech.model.WorkflowMetadata;
import com.github.managetech.workflow.WorkflowManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000","http://10.17.80.189:3000"})
@RestController
@ConditionalOnProperty(name = "visual.flow.enableDefaultApi", havingValue = "true", matchIfMissing = true)
public class WorkflowManagerController {

    private final WorkflowManager workflowManager;

    @Autowired
    public WorkflowManagerController(WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    @PostMapping(VisualFlowProperties.DEFAULT_CREATE_WORKFLOW)
    public Object createWorkflow(@RequestBody WorkflowMetadata workflowMetadata) {
        return workflowManager.createWorkflow(workflowMetadata);
    }

    @DeleteMapping(VisualFlowProperties.DEFAULT_DELETE_WORKFLOW)
    public Object deleteWorkflowMetadata(@RequestParam String workflowName) {
        return workflowManager.deleteWorkflowMetadata(workflowName);
    }

    @PutMapping(VisualFlowProperties.DEFAULT_UPDATE_WORKFLOW)
    public Object updateWorkflowMetadata(@RequestBody WorkflowMetadata workflowMetadata) {
        return workflowManager.updateWorkflowMetadata(workflowMetadata);
    }

    @GetMapping(VisualFlowProperties.DEFAULT_LIST_WORKFLOWS)
    public List<String> listMenuWorkflow() {
        return workflowManager.getMenuWorkflowNameList();
    }

    @PostMapping(VisualFlowProperties.DEFAULT_COMPILE_GROOVY_SCRIPT)
    public Object compileGroovyScript(@RequestBody ScriptRequest runRequest) throws IOException {
        return workflowManager.compileGroovyScript(runRequest.getCode());
    }

    @PostMapping(VisualFlowProperties.DEFAULT_RUN_GROOVY_SCRIPT)
    public Object runGroovyScript(@RequestBody ScriptRequest runRequest) throws IOException {
        return workflowManager.testGroovyScript(runRequest);
    }

    @GetMapping(VisualFlowProperties.DEFAULT_GET_WORKFLOW_METADATA)
    public WorkflowMetadata getWorkflowMetadata(@RequestParam String workflowName) {
        return workflowManager.getWorkflowMetadataByName(workflowName);
    }
}

