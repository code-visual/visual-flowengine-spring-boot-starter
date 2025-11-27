/*
 * Copyright (c) 2023-2024, levi li (levi.lideng@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law-or-agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.code.visual.workflow.script;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import io.github.code.visual.config.VisualFlowProperties;
import io.github.code.visual.model.Diagnostic;
import io.github.code.visual.model.ScriptMetadata;
import io.github.code.visual.utils.CommonUtils;
import io.github.code.visual.workflow.WorkflowMetadataRepository;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.runtime.EncodingGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScriptExecutionServiceImpl implements ScriptExecutionService {

    private final CompilerConfiguration config;
    private final WorkflowMetadataRepository workflowMetadataRepository;
    private final VisualFlowProperties visualFlowProperties;
    private GroovyClassLoader groovyClassLoader;

    @Autowired
    public ScriptExecutionServiceImpl(CompilerConfiguration config,
                                      WorkflowMetadataRepository workflowMetadataRepository,
                                      VisualFlowProperties visualFlowProperties) {
        this.config = config;
        this.workflowMetadataRepository = workflowMetadataRepository;
        this.groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
        this.visualFlowProperties = visualFlowProperties;
    }

    @Override
    public Object executeScript(ScriptMetadata scriptMetadata, Binding binding) {
        Class<?> aClass = workflowMetadataRepository.getClassFromCache(groovyClassLoader, scriptMetadata);
        if (aClass != null) {
            Script script = InvokerHelper.createScript(aClass, binding);
            return script.run();
        }

        String filename;
        try {
            filename = scriptMetadata.getScriptType().name() + "_Id_" + scriptMetadata.getScriptId() + "_MD5_" + EncodingGroovyMethods.md5(scriptMetadata.getScriptText()) + ".groovy";
        } catch (NoSuchAlgorithmException e) {
            throw new GroovyBugError("Failed to generate md5", e);
        }
        GroovyCodeSource codeSource = new GroovyCodeSource(scriptMetadata.getScriptText(), filename, "/groovy/script");
        Class parseClass = groovyClassLoader.parseClass(codeSource, visualFlowProperties.isEnableCacheSource());
        Script script = InvokerHelper.createScript(parseClass, binding);
        return script.run();
    }

    @Override
    public List<Diagnostic> compileGroovyScript(String code) {
        try (GroovyClassLoader tempGroovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config)) {
            tempGroovyClassLoader.parseClass(code);
            return new ArrayList<>();
        } catch (Exception e) {
            if (!(e instanceof MultipleCompilationErrorsException)) {
                throw new RuntimeException(e);
            }
            List<? extends Message> errors = ((MultipleCompilationErrorsException) e).getErrorCollector().getErrors();
            return CommonUtils.getDiagnostics(errors);
        }
    }

    public void resetGroovyClassLoader() throws IOException {

        if (this.groovyClassLoader != null) {
            this.groovyClassLoader.close();
        }
        this.groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
    }
}
