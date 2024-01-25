package com.github.managetech.utils;

import com.github.managetech.model.Diagnostic;
import com.github.managetech.model.ScriptMetadata;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author Levi Li
 * @since 01/25/2024
 */
public class CommonUtils {

    public static List<Diagnostic> getDiagnostics(List<? extends Message> errors) {
        List<Diagnostic> diagnostics = new java.util.ArrayList<>();
        for (Message error : errors) {
            if (error instanceof SyntaxErrorMessage) {
                SyntaxException cause = ((SyntaxErrorMessage) error).getCause();
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setMessage(cause.getMessage());
                diagnostic.setStartLineNumber(cause.getStartLine());
                diagnostic.setStartColumn(cause.getStartColumn());
                diagnostic.setEndLineNumber(cause.getEndLine());
                diagnostic.setEndColumn(cause.getEndColumn());
                diagnostics.add(diagnostic);
                continue;
            }
            if (error instanceof ExceptionMessage) {
                Exception cause = ((ExceptionMessage) error).getCause();
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setMessage(cause.getMessage());
                diagnostic.setStartLineNumber(cause.getStackTrace()[0].getLineNumber());
                diagnostic.setEndLineNumber(cause.getStackTrace()[0].getLineNumber());
                diagnostics.add(diagnostic);
                continue;
            } else {
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setMessage("unknown error");
                diagnostics.add(diagnostic);
            }
        }
        return diagnostics;
    }
}
