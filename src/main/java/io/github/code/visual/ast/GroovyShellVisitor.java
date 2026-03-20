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
package io.github.code.visual.ast;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;

import java.util.Arrays;
import java.util.List;

public class GroovyShellVisitor extends ClassCodeVisitorSupport {

    private final SourceUnit source;

    private static final List<String> FORBIDDEN_CLASSES = Arrays.asList(
            "java.lang.System",
            "java.lang.Runtime",
            "java.lang.reflect.Method"
    );

    public GroovyShellVisitor(SourceUnit source) {
        this.source = source;
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        super.visitVariableExpression(expression);
    }

    @Override
    public void visitMethod(MethodNode methodNode) {
        if (methodNode.getName().length() == 1) {
            throw new RuntimeException(new SyntaxException(
                    "single letter is forbidden",
                    methodNode.getLineNumber(),
                    methodNode.getColumnNumber()
            ));
        }
        super.visitMethod(methodNode);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        if (FORBIDDEN_CLASSES.contains(call.getObjectExpression().getType().toString())) {
            source.addError(new SyntaxException(
                    "Usage of " + call.getObjectExpression().getType() + " is forbidden: " + call.getMethodAsString(),
                    call.getLineNumber(),
                    call.getColumnNumber()
            ));
        }
        super.visitMethodCallExpression(call);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        if (FORBIDDEN_CLASSES.contains(call.getType().toString())) {
            source.addError(new SyntaxException(
                    "Usage of " + call.getType() + " is forbidden",
                    call.getLineNumber(),
                    call.getColumnNumber()
            ));
        }
        super.visitConstructorCallExpression(call);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        super.visitClosureExpression(expression);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }
}
