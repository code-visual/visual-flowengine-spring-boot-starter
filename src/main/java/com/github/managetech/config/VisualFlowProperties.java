package com.github.managetech.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Levi Li
 * @since 01/18/2024
 */
@ConfigurationProperties(prefix = "visual.flow")
public class VisualFlowProperties {

    private String webUIPath = "/visualFlow-ui";
    private String compileScriptApiPath = "/groovyScript/compile";
    private String resourcePrefix = "";
    private boolean enableDefaultApi;


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
        return resourcePrefix+ path;
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
