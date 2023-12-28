package com.github.managetech.web;

import com.github.managetech.model.Diagnostic;
import com.github.managetech.scriptcache.ScriptCachingEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/engine")
public class DiagnosticController {


    private final ScriptCachingEngine scriptCachingEngine;

    @Autowired
    public DiagnosticController(ScriptCachingEngine scriptCachingEngine) {
        this.scriptCachingEngine = scriptCachingEngine;
    }

    @PostMapping("/compileGroovyScript")
    public Diagnostic compileGroovyScript(@RequestBody String code) {
        return scriptCachingEngine.compileGroovyScript(code);
    }
}
