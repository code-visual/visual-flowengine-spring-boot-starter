package com.github.managetech.config;

import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.codehaus.groovy.syntax.Types;
import org.kohsuke.groovy.sandbox.SandboxTransformer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Levi Li
 * @since 12/19/2023
 */
@Configuration
@ComponentScan(basePackages = "com.github.managetech")
@ConditionalOnWebApplication
@EnableConfigurationProperties(VisualFlowProperties.class)
public class VisualFlowEngineAutoConfiguration {


    @Controller
    public static class VisualFlowUIController {

        private final VisualFlowProperties visualFlowProperties;

        public VisualFlowUIController(VisualFlowProperties visualFlowProperties) {
            this.visualFlowProperties = visualFlowProperties;
        }

        @GetMapping("${visual.flow.webUIPath}")
        public ModelAndView visualFlow(Model model) {
            model.addAttribute("visualFlowProperties", visualFlowProperties);
            return new ModelAndView("index");
        }
    }

    @Bean(name = "customLibraryWebConfigurer")
    public WebMvcConfigurer customLibraryWebConfigurer() {
        return new WebMvcConfigurer() {

            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/**")
                        .addResourceLocations("classpath:/META-INF/resources/visualflow/");
            }
        };
    }

    @Configuration
    static class GroovySecureConfig {


        @Bean
        @ConditionalOnMissingBean(SecureASTCustomizer.class)
        public SecureASTCustomizer secureASTCustomizer() {
            final SecureASTCustomizer secure = new SecureASTCustomizer();
            secure.setClosuresAllowed(true); // 允许使用闭包

            List<Integer> tokensBlacklist = new ArrayList<>();
            tokensBlacklist.add(Types.KEYWORD_WHILE); // 添加关键字黑名单 while和goto
            tokensBlacklist.add(Types.KEYWORD_GOTO);
            secure.setDisallowedTokens(tokensBlacklist);
            List<Class<? extends Statement>> statementBlacklist = new ArrayList<>();
            statementBlacklist.add(WhileStatement.class);
            secure.setDisallowedStatements(statementBlacklist);
            secure.setIndirectImportCheckEnabled(false); // 设置为false, 可以在代码中定义并直接使用class, 否则需要在白名单中指定
            secure.setDisallowedImports(Arrays.asList("org.codehaus.groovy.runtime.*", "groovy.json.*"));
            return secure;
        }

        @Bean
        @ConditionalOnMissingBean(CompilerConfiguration.class)
        public CompilerConfiguration compilerConfiguration(SecureASTCustomizer secure) {
            final CompilerConfiguration config = new CompilerConfiguration(); // 自定义CompilerConfiguration，设置AST
            config.addCompilationCustomizers(secure);
            config.setSourceEncoding("UTF-8");
            config.addCompilationCustomizers(new SandboxTransformer());
            return config;
        }

    }
}
