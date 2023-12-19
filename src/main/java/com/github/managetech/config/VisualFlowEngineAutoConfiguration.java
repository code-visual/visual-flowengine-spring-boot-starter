package com.github.managetech.config;

import com.github.managetech.cache.ScriptCachingEngine;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.codehaus.groovy.syntax.Types;
import org.kohsuke.groovy.sandbox.SandboxTransformer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Levi Li
 * @since 12/19/2023
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.github.managetech")
//@ConditionalOnClass(MongoClient.class)
public class VisualFlowEngineAutoConfiguration {




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
            secure.setIndirectImportCheckEnabled(true); // 设置为false, 可以在代码中定义并直接使用class, 否则需要在白名单中指定
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
