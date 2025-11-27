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
import org.codehaus.groovy.syntax.SyntaxException;

import java.util.List;

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
