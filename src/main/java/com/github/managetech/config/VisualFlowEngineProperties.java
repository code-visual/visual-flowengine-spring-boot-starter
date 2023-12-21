package com.github.managetech.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Levi Li
 * @since 12/21/2023
 */
@ConfigurationProperties(prefix = "visual.flow.engine")
public class VisualFlowEngineProperties {
    private String path = "visualflow";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "VisualFlowEngineProperties{" +
                "path='" + path + '\'' +
                '}';
    }
}
