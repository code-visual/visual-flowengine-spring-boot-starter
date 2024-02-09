package com.github.managetech.workflow;

import com.github.managetech.model.ScriptMetadata;
import com.github.managetech.model.ScriptType;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Stack;

/**
 * @author Levi Li
 * @since 01/24/2024
 */
@Service
public class WorkflowEngine {
    private final CompilerConfiguration config;

    @Autowired
    public WorkflowEngine(CompilerConfiguration config) {
        this.config = config;
    }

    public void execute(ScriptMetadata scriptMetadata, Binding binding) throws ScriptException {
        try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config)) {
            Stack<ScriptMetadata> nodeDeque = new Stack<>();
            nodeDeque.push(scriptMetadata);

            while (!nodeDeque.isEmpty()) {
                ScriptMetadata metadata = nodeDeque.pop();
                String scriptContent = metadata.getScriptText();
                Class<?> aClass = groovyClassLoader.parseClass(scriptContent);
                if (CollectionUtils.isEmpty(metadata.getChildren())) {
                    for (ScriptMetadata child : metadata.getChildren()) {
                        if (child.getScriptType() != ScriptType.End) {
                            nodeDeque.push(child);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ScriptException("parseScriptAndCache error:" + e.getMessage());
        }
    }

}
