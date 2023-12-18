package com.github.eyesli.config;

import com.github.eyesli.cache.Impl.ManageCacheServiceImpl;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.Types;
import org.kohsuke.groovy.sandbox.SandboxTransformer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class Main {
    public static void main(String[] args) throws IOException {

        Binding binding = new Binding();
        binding.setVariable("requestNo", 1);

        new GroovyNotSupportInterceptor().register();

        final SecureASTCustomizer secure = new SecureASTCustomizer();
        secure.setClosuresAllowed(true); // 允许使用闭包

        List<Integer> tokensBlacklist = new ArrayList<>();
        tokensBlacklist.add(Types.KEYWORD_WHILE); // 添加关键字黑名单 while和goto
        tokensBlacklist.add(Types.KEYWORD_GOTO);
        secure.setDisallowedTokens(tokensBlacklist);
        secure.setIndirectImportCheckEnabled(true); // 设置为false, 可以在代码中定义并直接使用class, 否则需要在白名单中指定
        secure.setDisallowedImports(Arrays.asList("org.codehaus.groovy.runtime.*", "groovy.json.*"));


        final CompilerConfiguration config = new CompilerConfiguration(); // 自定义CompilerConfiguration，设置AST
        config.addCompilationCustomizers(secure);
        config.setSourceEncoding("UTF-8");
        config.addCompilationCustomizers(new SandboxTransformer());


        ManageCacheServiceImpl manageCacheService = new ManageCacheServiceImpl(config);
        Script script = manageCacheService.parseScript("int i = 0\n" +
                "while (i < 5) {\n" +
                "    println \"Value of i is: $i\"\n" +
                "    i++\n" +
                "}\n", binding);
        Object result = script.run();
        System.out.println(result);


    }

}
