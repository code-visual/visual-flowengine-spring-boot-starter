package com.github.managetech.web;

import com.github.managetech.groovy.GroovyNotSupportInterceptor;
import com.github.managetech.model.Diagnostic;
import com.github.managetech.scriptcache.Impl.ScriptCachingEngineImpl;
import com.github.managetech.scriptcache.ScriptCachingEngine;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/engine")
public class DiagnosticController {


    private final ScriptCachingEngine scriptCachingEngine;

    @Autowired
    public DiagnosticController(ScriptCachingEngine scriptCachingEngine) {
        this.scriptCachingEngine = scriptCachingEngine;
    }

    @PostMapping("/groovyScript/compile")
    public Object compileGroovyScript(@RequestBody String code) throws IOException {
        return scriptCachingEngine.compileGroovyScript(code);
    }
    @GetMapping("/groovyScript/{scriptName}")
    public Object getScriptMetadataByName(@PathVariable String scriptName) {
        return scriptCachingEngine.getScriptMetadataByName(scriptName);
    }


}
