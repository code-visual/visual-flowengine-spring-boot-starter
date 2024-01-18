package com.github.managetech.web;

import com.github.managetech.scriptcache.ScriptCachingEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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
