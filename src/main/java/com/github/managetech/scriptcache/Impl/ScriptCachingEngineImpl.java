package com.github.managetech.scriptcache.Impl;

import com.github.managetech.scriptcache.ScriptCachingEngine;
import com.github.managetech.groovy.GroovyNotSupportInterceptor;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
@Service
@SuppressWarnings("unchecked")
public class ScriptCachingEngineImpl implements ScriptCachingEngine {

    private final CompilerConfiguration config;
    private final Map<String, Class<? extends Script>> parseScriptCache = new ConcurrentHashMap<>();

    @Autowired
    public ScriptCachingEngineImpl(CompilerConfiguration config) {
        this.config = config;
    }


    @Override
    public Script parseScript(String scriptText, Binding binding) throws IOException {

        Class<? extends Script> script = parseScriptCache.get(DigestUtils.md5DigestAsHex(scriptText.getBytes()));
        GroovyClassLoader groovyClassLoader = null;

        if (script == null) {
            //假如我有多个呢。这样要做成配置的。提供GroovyInterceptor子类。然后全部执行
            new GroovyNotSupportInterceptor().register();

            try {
                groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
                Class<? extends Script> aClass = groovyClassLoader.parseClass(scriptText);
                parseScriptCache.put(DigestUtils.md5DigestAsHex(scriptText.getBytes()), aClass);
                script = aClass;
            } finally {
                if (groovyClassLoader != null) {
                    groovyClassLoader.close();
                }
            }
        }

        return InvokerHelper.createScript(script, binding);
    }

}
