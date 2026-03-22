/*
 * Copyright (c) 2023-2024, levi li (levi.lideng@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.code.visual.utils;

import io.github.code.visual.model.Diagnostic;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.util.List;

/**
 * @author Levi Li
 * @since 01/25/2024
 */
public class CommonUtils {

    public static List<Diagnostic> getDiagnostics(List<? extends Message> messages) {
        return getDiagnostics(messages, Diagnostic.SEVERITY_ERROR);
    }

    public static List<Diagnostic> getDiagnostics(List<? extends Message> messages, int severity) {
        List<Diagnostic> diagnostics = new java.util.ArrayList<>();
        for (Message message : messages) {
            if (message instanceof SyntaxErrorMessage) {
                SyntaxException cause = ((SyntaxErrorMessage) message).getCause();
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setMessage(cause.getMessage());
                diagnostic.setStartLineNumber(cause.getStartLine());
                diagnostic.setStartColumn(cause.getStartColumn());
                diagnostic.setEndLineNumber(cause.getEndLine());
                diagnostic.setEndColumn(cause.getEndColumn());
                diagnostic.setSeverity(severity);
                diagnostics.add(diagnostic);
            } else if (message instanceof WarningMessage) {
                WarningMessage warning = (WarningMessage) message;
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setMessage(warning.getMessage());
                diagnostic.setSeverity(Diagnostic.SEVERITY_WARNING);
                diagnostics.add(diagnostic);
            } else if (message instanceof ExceptionMessage) {
                Exception cause = ((ExceptionMessage) message).getCause();
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setMessage(cause.getMessage());
                diagnostic.setSeverity(severity);
                if (cause.getStackTrace().length > 0) {
                    diagnostic.setStartLineNumber(cause.getStackTrace()[0].getLineNumber());
                    diagnostic.setEndLineNumber(cause.getStackTrace()[0].getLineNumber());
                }
                diagnostics.add(diagnostic);
            } else {
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setMessage(message.toString());
                diagnostic.setSeverity(severity);
                diagnostics.add(diagnostic);
            }
        }
        return diagnostics;
    }
}
