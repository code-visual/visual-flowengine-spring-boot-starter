package com.github.managetech.web;

import com.github.managetech.model.GroovyScript;
import com.github.managetech.scriptcache.ScriptCachingEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/engine")
@ConditionalOnProperty(name = "visual.flow.enableDefaultApi", havingValue = "true", matchIfMissing = true)
public class DiagnosticController {


    private final ScriptCachingEngine scriptCachingEngine;

    @Autowired
    public DiagnosticController(ScriptCachingEngine scriptCachingEngine) {
        this.scriptCachingEngine = scriptCachingEngine;
    }

    @PostMapping("/groovyScript")
    public Object createGroovyScript(@RequestBody GroovyScript groovyScript) {
        return scriptCachingEngine.createGroovyScript(groovyScript);
    }

    @DeleteMapping("/groovyScript/{scriptId}")
    public Object deleteGroovyScript(@PathVariable String scriptId) {
        return scriptCachingEngine.deleteGroovyScript(scriptId);
    }

    @GetMapping("/groovyScripts")
    public List<?> listAllGroovyScripts() {
        return null;
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
