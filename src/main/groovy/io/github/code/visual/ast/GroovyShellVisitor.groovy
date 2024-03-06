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
package io.github.code.visual.ast

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.SyntaxException

class GroovyShellVisitor extends ClassCodeVisitorSupport {

    SourceUnit source

    private static final List<String> FORBIDDEN_CLASSES = [
            "java.lang.System",
            "java.lang.Runtime",
            "java.lang.reflect.Method",
    ]

    GroovyShellVisitor(SourceUnit source) {
        this.source = source
    }

    @Override
    void visitVariableExpression(VariableExpression expression) {
        super.visitVariableExpression(expression)
    }

    @Override
    void visitMethod(MethodNode methodNode) {

        if (methodNode.name.size() == 1) {
            throw new SyntaxException(
                    "single letter is forbidden",
                    methodNode.lineNumber,
                    methodNode.columnNumber
            )
        }
        super.visitMethod(methodNode)
    }

    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        if (FORBIDDEN_CLASSES.contains(call.objectExpression.type.toString())) {
            source.addError(
                    new SyntaxException("Usage of ${call.objectExpression.type} is forbidden: "
                            + "${call.methodAsString}", call.lineNumber, call.columnNumber))
        }
        super.visitMethodCallExpression(call)
    }

    @Override
    void visitConstructorCallExpression(ConstructorCallExpression call) {
        if (FORBIDDEN_CLASSES.contains(call.type.toString())) {
            source.addError(new SyntaxException("Usage of ${call.type} is forbidden",
                    call.lineNumber, call.columnNumber))
        }
        super.visitConstructorCallExpression(call)
    }

    @Override
    void visitDeclarationExpression(DeclarationExpression expression) {
        super.visitDeclarationExpression(expression)
    }


    @Override
    void visitClosureExpression(ClosureExpression expression) {
        super.visitClosureExpression(expression)
    }


    @Override
    protected SourceUnit getSourceUnit() {
        return null
    }
}