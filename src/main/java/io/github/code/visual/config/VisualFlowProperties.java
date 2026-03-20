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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Visual Flow Engine.
 *
 * @author Levi Li
 * @since 01/18/2024
 */
@ConfigurationProperties(prefix = "visual.flow")
public class VisualFlowProperties {

    /**
     * Base path for all Visual Flow endpoints (UI + API).
     * Default: /visualflow
     */
    private String basePath = "/visualflow";

    /**
     * Whether to enable the built-in REST API.
     */
    private boolean enableApi = true;

    /**
     * Whether to enable the Web UI.
     */
    private boolean enableUi = true;

    /**
     * Whether to enable Groovy AST security checks.
     */
    private boolean enableAST = true;

    /**
     * Whether to cache compiled Groovy classes.
     */
    private boolean enableCacheSource = true;

    /**
     * Script execution timeout in seconds. 0 means no timeout.
     */
    private int scriptTimeoutSeconds = 30;

    // ── Per-endpoint path overrides (optional) ──

    private String workflowsApiPath;
    private String executeApiPath;
    private String debugApiPath;
    private String compileApiPath;

    // ── Derived paths (use override if set, otherwise derive from basePath) ──

    public String getWorkflowsApiPath() {
        return workflowsApiPath != null ? workflowsApiPath : basePath + "/api/workflows";
    }

    public String getExecuteApiPath() {
        return executeApiPath != null ? executeApiPath : basePath + "/api/workflows/execute";
    }

    public String getDebugApiPath() {
        return debugApiPath != null ? debugApiPath : basePath + "/api/workflows/debug";
    }

    public String getDebugStreamApiPath() {
        return basePath + "/api/workflows/debug/stream";
    }

    public String getCompileApiPath() {
        return compileApiPath != null ? compileApiPath : basePath + "/api/script/compile";
    }

    public String getConfigApiPath() {
        return basePath + "/api/config";
    }

    // ── Getters and Setters ──

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public boolean isEnableApi() {
        return enableApi;
    }

    public void setEnableApi(boolean enableApi) {
        this.enableApi = enableApi;
    }

    public boolean isEnableUi() {
        return enableUi;
    }

    public void setEnableUi(boolean enableUi) {
        this.enableUi = enableUi;
    }

    public boolean isEnableAST() {
        return enableAST;
    }

    public void setEnableAST(boolean enableAST) {
        this.enableAST = enableAST;
    }

    public boolean isEnableCacheSource() {
        return enableCacheSource;
    }

    public void setEnableCacheSource(boolean enableCacheSource) {
        this.enableCacheSource = enableCacheSource;
    }

    public void setWorkflowsApiPath(String workflowsApiPath) {
        this.workflowsApiPath = workflowsApiPath;
    }

    public void setExecuteApiPath(String executeApiPath) {
        this.executeApiPath = executeApiPath;
    }

    public void setDebugApiPath(String debugApiPath) {
        this.debugApiPath = debugApiPath;
    }

    public void setCompileApiPath(String compileApiPath) {
        this.compileApiPath = compileApiPath;
    }

    public int getScriptTimeoutSeconds() {
        return scriptTimeoutSeconds;
    }

    public void setScriptTimeoutSeconds(int scriptTimeoutSeconds) {
        this.scriptTimeoutSeconds = scriptTimeoutSeconds;
    }
}
