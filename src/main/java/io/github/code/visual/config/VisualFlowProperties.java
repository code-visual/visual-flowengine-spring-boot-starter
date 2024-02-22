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
package io.github.code.visual.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Levi Li
 * @since 01/18/2024
 */
@ConfigurationProperties(prefix = "visual.flow")
public class VisualFlowProperties {

    @Value("${visual.flow.webUIPath:/visualFlow-ui.html}")
    private String webUIPath;
    private boolean enableDefaultApi;
    private boolean enableWebUIPath;

    private String executeWorkflowApiPath = "/api/engine/workflow/execute";
    private String debugWorkflowApiPath = "/api/engine/workflow/debug";
    private String createWorkflowApiPath = "/api/engine/workflow";
    private String deleteWorkflowApiPath = "/api/engine/workflow";
    private String updateWorkflowApiPath = "/api/engine/workflow";

    private String listWorkflowsApiPath = "/api/engine/workflowList";
    private String compileScriptApiPath = "/api/engine/groovyScript/compile";
    private String getWorkflowMetadataApiPath = "/api/engine/workflow";


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

    public String getWebUIPath() {
        return webUIPath;
    }


    public void setWebUIPath(String webUIPath) {
        this.webUIPath = webUIPath;
    }


}
