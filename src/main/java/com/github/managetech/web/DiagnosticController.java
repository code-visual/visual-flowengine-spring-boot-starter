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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/compileGroovyScript")
    public Object compileGroovyScript(@RequestBody String code) {
        return scriptCachingEngine.compileGroovyScript(code);
    }

    public static void main(String[] args) throws IOException {
        String a = "def exampleMethod() {\n" +
                "    int a = 10\n" +
                "    int b = 10\n" +
                "    a = \"thirty\"\n" +
                "    return a + b\n" +
                "}\n" +
                "\n" +
                "println exampleMethod()\n" +
                "\n" +
                "\n";

        String b ="def exampleMethod() {\n    int a = 10\n    int b = \" \n    return a + b\n}\n\nprintln exampleMethod()\n\n\n\n\n\n\n\n";
        ScriptCachingEngineImpl scriptCachingEngine1 = new ScriptCachingEngineImpl(null);
        Object o = scriptCachingEngine1.compileGroovyScript(b);
        System.out.println("o = " + o);

    }
}
