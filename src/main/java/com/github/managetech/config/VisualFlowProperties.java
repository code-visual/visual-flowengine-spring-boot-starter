package com.github.managetech.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Levi Li
 * @since 01/18/2024
 */
@ConfigurationProperties(prefix = "visual.flow")
public class VisualFlowProperties {

    public static final String DEFAULT_EXECUTE_WORKFLOW = "/api/engine/workflow/execute";
    public static final String DEFAULT_DEBUG_WORKFLOW = "/api/engine/workflow/debug";
    public static final String DEFAULT_CREATE_WORKFLOW = "/api/engine/workflow";
    public static final String DEFAULT_DELETE_WORKFLOW = "/api/engine/workflow";
    public static final String DEFAULT_LIST_WORKFLOWS = "/api/engine/workflowList";
    public static final String DEFAULT_COMPILE_GROOVY_SCRIPT = "/api/engine/groovyScript/compile";
    public static final String DEFAULT_GET_WORKFLOW_METADATA = "/api/engine/workflow";
    public static final String DEFAULT_UPDATE_WORKFLOW = "/api/engine/workflow";


    @Value("${visual.flow.webUIPath:/visualFlow-ui.html}")
    private String webUIPath;
    private String resourcePrefix = "";
    private boolean enableDefaultApi;
    private boolean enableWebUIPath;

    private String executeWorkflowApiPath = DEFAULT_EXECUTE_WORKFLOW;
    private String debugWorkflowApiPath = DEFAULT_DEBUG_WORKFLOW;
    private String createWorkflowApiPath = DEFAULT_CREATE_WORKFLOW;
    private String deleteWorkflowApiPath = DEFAULT_DELETE_WORKFLOW;
    private String updateWorkflowApiPath = DEFAULT_UPDATE_WORKFLOW;

    private String listWorkflowsApiPath = DEFAULT_LIST_WORKFLOWS;
    private String compileScriptApiPath = DEFAULT_COMPILE_GROOVY_SCRIPT;
    private String getWorkflowMetadataApiPath = DEFAULT_GET_WORKFLOW_METADATA;


    public String getDebugWorkflowApiPath() {
        return debugWorkflowApiPath;
    }

    public void setDebugWorkflowApiPath(String debugWorkflowApiPath) {
        this.debugWorkflowApiPath = debugWorkflowApiPath;
    }

    public String getExecuteWorkflowApiPath() {
        return executeWorkflowApiPath;
    }

    public void setExecuteWorkflowApiPath(String executeWorkflowApiPath) {
        this.executeWorkflowApiPath = executeWorkflowApiPath;
    }

    public String getUpdateWorkflowApiPath() {
        return updateWorkflowApiPath;
    }

    public void setUpdateWorkflowApiPath(String updateWorkflowApiPath) {
        this.updateWorkflowApiPath = updateWorkflowApiPath;
    }

    public boolean isEnableWebUIPath() {
        return enableWebUIPath;
    }

    public void setEnableWebUIPath(boolean enableWebUIPath) {
        this.enableWebUIPath = enableWebUIPath;
    }

    public String getCreateWorkflowApiPath() {
        return createWorkflowApiPath;
    }

    public void setCreateWorkflowApiPath(String createWorkflowApiPath) {
        this.createWorkflowApiPath = createWorkflowApiPath;
    }

    public String getDeleteWorkflowApiPath() {
        return deleteWorkflowApiPath;
    }

    public void setDeleteWorkflowApiPath(String deleteWorkflowApiPath) {
        this.deleteWorkflowApiPath = deleteWorkflowApiPath;
    }

    public String getListWorkflowsApiPath() {
        return listWorkflowsApiPath;
    }

    public void setListWorkflowsApiPath(String listWorkflowsApiPath) {
        this.listWorkflowsApiPath = listWorkflowsApiPath;
    }

    public String getGetWorkflowMetadataApiPath() {
        return getWorkflowMetadataApiPath;
    }

    public void setGetWorkflowMetadataApiPath(String getWorkflowMetadataApiPath) {
        this.getWorkflowMetadataApiPath = getWorkflowMetadataApiPath;
    }

    public String getCompileScriptApiPath() {
        return compileScriptApiPath;
    }

    public void setCompileScriptApiPath(String compileScriptApiPath) {
        this.compileScriptApiPath = compileScriptApiPath;
    }

    public boolean isEnableDefaultApi() {
        return enableDefaultApi;
    }

    public void setEnableDefaultApi(boolean enableDefaultApi) {
        this.enableDefaultApi = enableDefaultApi;
    }

    // Getter
    public String getResourcePath(String path) {
        return resourcePrefix + path;
    }

    public String getWebUIPath() {
        return webUIPath;
    }


    public void setWebUIPath(String webUIPath) {
        this.webUIPath = webUIPath;
    }

    public String getResourcePrefix() {
        return resourcePrefix;
    }

    public void setResourcePrefix(String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
    }

}
