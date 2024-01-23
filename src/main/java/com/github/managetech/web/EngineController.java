package com.github.managetech.web;

import com.github.managetech.model.WorkflowMetadata;
import com.github.managetech.scriptcache.WorkflowEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/engine")
@ConditionalOnProperty(name = "visual.flow.enableDefaultApi", havingValue = "true", matchIfMissing = true)
public class EngineController {


    private final WorkflowEngine workflowEngine;

    @Autowired
    public EngineController(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    @PostMapping("/createWorkflow")
    public Object createWorkflow(@RequestBody WorkflowMetadata workflowMetadata) {
        return workflowEngine.createWorkflow(workflowMetadata);
    }

    @DeleteMapping("/workflow/{workflowName}")
    public Object deleteWorkflowMetadata(@PathVariable String workflowName) {
        return workflowEngine.deleteWorkflowMetadata(workflowName);
    }

    @GetMapping("/workflowList")
    public List<String> listMenuWorkflow() {
        return workflowEngine.getMenuWorkflowNameList();
    }


    @PostMapping("/groovyScript/compile")
    public Object compileGroovyScript(@RequestBody String code) throws IOException {
        return workflowEngine.compileGroovyScript(code);
    }

    @GetMapping("/groovyScript/{scriptName}")
    public Object getScriptMetadataByName(@PathVariable String scriptName) {
        return workflowEngine.getScriptMetadataByName(scriptName);
    }


}
