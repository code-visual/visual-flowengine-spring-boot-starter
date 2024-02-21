package io.github.code.visual.web;

import io.github.code.visual.config.VisualFlowProperties;
import io.github.code.visual.model.DebugRequest;
import io.github.code.visual.model.ScriptRequest;
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
