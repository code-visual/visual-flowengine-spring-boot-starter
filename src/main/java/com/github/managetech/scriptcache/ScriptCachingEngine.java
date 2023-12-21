package com.github.managetech.scriptcache;


import groovy.lang.Binding;
import groovy.lang.Script;

import java.io.IOException;

/**
 * @author Levi Li
 * @since 09/18/2023
 */
public interface ScriptCachingEngine {


    Script parseScript(String scriptText, Binding binding) throws IOException;


}
