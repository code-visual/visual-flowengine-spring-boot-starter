package com.github.managetech.web;

import com.github.managetech.config.VisualFlowProperties;
import com.github.managetech.model.DebugRequest;
import com.github.managetech.model.ScriptRequest;
import com.github.managetech.model.WorkflowMetadata;
import com.github.managetech.model.WorkflowTaskLog;
import com.github.managetech.workflow.WorkflowManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping(VisualFlowProperties.DEFAULT_EXECUTE_WORKFLOW)
    public Map<Integer, List<WorkflowTaskLog>> executeWorkflow(@RequestParam Integer workflowId, @RequestBody Map inputVariables) {
        return workflowManager.startWorkflow(workflowId, inputVariables);
    }

    @PostMapping(VisualFlowProperties.DEFAULT_DEBUG_WORKFLOW)
    public Map<Integer, List<WorkflowTaskLog>> debugWorkflow(@RequestBody DebugRequest debugRequest) {
        return workflowManager.debug(debugRequest);
    }

    @PostMapping(VisualFlowProperties.DEFAULT_CREATE_WORKFLOW)
    public Object createWorkflow(@RequestBody WorkflowMetadata workflowMetadata) {
        return workflowManager.createWorkflow(workflowMetadata);
    }

    @DeleteMapping(VisualFlowProperties.DEFAULT_DELETE_WORKFLOW)
    public Object deleteWorkflowMetadata(@RequestParam Integer workflowId) {
        return workflowManager.deleteWorkflowMetadata(workflowId);
    }

    @PutMapping(VisualFlowProperties.DEFAULT_UPDATE_WORKFLOW)
    public Object updateWorkflowMetadata(@RequestBody WorkflowMetadata workflowMetadata) {
        return workflowManager.updateWorkflowMetadata(workflowMetadata);
    }

    @GetMapping(VisualFlowProperties.DEFAULT_LIST_WORKFLOWS)
    public List<WorkflowMetadata> listMenuWorkflow() {
        return workflowManager.getMenuWorkflowNameList();
    }

    @PostMapping(VisualFlowProperties.DEFAULT_COMPILE_GROOVY_SCRIPT)
    public Object compileGroovyScript(@RequestBody ScriptRequest runRequest) {
        return workflowManager.compileGroovyScript(runRequest.getCode());
    }

    @GetMapping(VisualFlowProperties.DEFAULT_GET_WORKFLOW_METADATA)
    public WorkflowMetadata getWorkflowMetadata(@RequestParam Integer workflowId) {
        return workflowManager.getWorkflowMetadataById(workflowId);
    }
}

