package com.github.managetech.scriptcache;


import com.github.managetech.model.Diagnostic;
import groovy.lang.Binding;
import groovy.lang.Script;

import java.io.IOException;
import java.util.List;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
public interface ScriptCachingEngine {


    Script parseScript(String scriptText, Binding binding) throws IOException;


    List<Diagnostic> compileGroovyScript(String code) throws IOException;
    Object runGroovyScript(String code,Binding binding) throws IOException;

    Object getScriptMetadataByName(String scriptName);
}
