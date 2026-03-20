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
package io.github.code.visual.web;

import io.github.code.visual.config.VisualFlowProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Serves index.html with paths dynamically adjusted to respect the servlet context-path.
 * Compatible with both Spring Boot 2.x (javax.servlet) and 3.x (jakarta.servlet).
 *
 * @author Levi Li
 * @since 03/20/2026
 */
@RestController
@ConditionalOnProperty(name = "visual.flow.enable-ui", havingValue = "true", matchIfMissing = true)
public class VisualFlowIndexController {

    private final VisualFlowProperties properties;

    public VisualFlowIndexController(VisualFlowProperties properties) {
        this.properties = properties;
    }

    @GetMapping(value = "${visual.flow.basePath:/visualflow}/index.html", produces = MediaType.TEXT_HTML_VALUE)
    public String index() throws IOException {
        ClassPathResource resource = new ClassPathResource("META-INF/resources/visualflow/index.html");
        String html;
        try (InputStream is = resource.getInputStream()) {
            html = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }

        // Detect context-path from current request using Spring API (no javax/jakarta import needed)
        String contextPath = ServletUriComponentsBuilder.fromCurrentContextPath().build().getPath();
        if (contextPath == null) {
            contextPath = "";
        }

        String basePath = properties.getBasePath();
        String fullBasePath = contextPath + basePath;

        // Replace hardcoded asset paths in index.html
        html = html.replace("\"/visualflow/assets/", "\"" + fullBasePath + "/assets/");
        html = html.replace("'/visualflow/assets/", "'" + fullBasePath + "/assets/");

        // Inject global config before </head> so frontend JS can read the correct basePath
        String configScript = "<script>"
                + "window.__VISUAL_FLOW_CONFIG__={"
                + "\"basePath\":\"" + fullBasePath + "\","
                + "\"workflowsApiPath\":\"" + fullBasePath + "/api/workflows\","
                + "\"executeApiPath\":\"" + fullBasePath + "/api/workflows/execute\","
                + "\"debugApiPath\":\"" + fullBasePath + "/api/workflows/debug\","
                + "\"compileApiPath\":\"" + fullBasePath + "/api/script/compile\""
                + "};</script>";
        html = html.replace("</head>", configScript + "</head>");

        return html;
    }
}
