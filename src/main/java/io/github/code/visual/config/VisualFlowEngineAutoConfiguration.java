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

import io.github.code.visual.workflow.InMemoryWorkflowRepository;
import io.github.code.visual.workflow.WorkflowEngine;
import io.github.code.visual.workflow.WorkflowExecutionListener;
import io.github.code.visual.workflow.WorkflowRepository;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.codehaus.groovy.syntax.Types;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Auto-configuration for Visual Flow Engine.
 *
 * @author Levi Li
 * @since 12/19/2023
 */
@Configuration
@ComponentScan(basePackages = "io.github.code.visual")
@ConditionalOnWebApplication
@EnableConfigurationProperties(VisualFlowProperties.class)
public class VisualFlowEngineAutoConfiguration {

    // ── UI: Static resource serving ──

    @Bean
    @ConditionalOnProperty(name = "visual.flow.enable-ui", havingValue = "true", matchIfMissing = true)
    public WebMvcConfigurer visualFlowUiConfigurer(VisualFlowProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String basePath = properties.getBasePath();
                registry.addResourceHandler(basePath + "/**")
                        .addResourceLocations("classpath:/META-INF/resources/visualflow/");
            }
        };
    }

    // ── Groovy security ──

    @Configuration
    static class GroovySecureConfig {

        @Bean
        @ConditionalOnMissingBean(SecureASTCustomizer.class)
        public SecureASTCustomizer secureASTCustomizer() {
            final SecureASTCustomizer secure = new SecureASTCustomizer();
            secure.setClosuresAllowed(true);

            List<Integer> tokensBlacklist = new ArrayList<>();
            tokensBlacklist.add(Types.KEYWORD_WHILE);
            tokensBlacklist.add(Types.KEYWORD_GOTO);
            secure.setDisallowedTokens(tokensBlacklist);

            List<Class<? extends Statement>> statementBlacklist = new ArrayList<>();
            statementBlacklist.add(WhileStatement.class);
            secure.setDisallowedStatements(statementBlacklist);

            secure.setIndirectImportCheckEnabled(false);
            secure.setDisallowedStaticImports(Arrays.asList("System", "Runtime", "Class"));
            secure.setDisallowedImports(Arrays.asList("org.codehaus.groovy.runtime.*", "groovy.json.*"));
            return secure;
        }

        @Bean
        @ConditionalOnMissingBean(CompilerConfiguration.class)
        public CompilerConfiguration compilerConfiguration(SecureASTCustomizer secure) {
            final CompilerConfiguration config = new CompilerConfiguration();
            config.addCompilationCustomizers(secure);
            config.setSourceEncoding("UTF-8");
            return config;
        }
    }

    // ── Core beans ──

    @Bean
    @ConditionalOnMissingBean(WorkflowRepository.class)
    public WorkflowRepository workflowRepository() {
        return new InMemoryWorkflowRepository();
    }

    @Bean
    @ConditionalOnMissingBean(WorkflowEngine.class)
    public WorkflowEngine workflowEngine(CompilerConfiguration compilerConfiguration,
                                         WorkflowRepository workflowRepository,
                                         VisualFlowProperties properties,
                                         @Autowired(required = false) List<WorkflowExecutionListener> listeners) {
        return new WorkflowEngine(compilerConfiguration, workflowRepository, properties, listeners);
    }
}
