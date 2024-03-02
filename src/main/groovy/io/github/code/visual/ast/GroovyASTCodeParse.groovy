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

import io.github.code.visual.config.VisualFlowProperties
import io.github.code.visual.utils.SpringContext
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import javax.script.ScriptException

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class GroovyASTCodeParse implements ASTTransformation {
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        def flowProperties = SpringContext.getBean(VisualFlowProperties.class)
        nodes.each { node ->
            if (node instanceof ModuleNode) {

                def moduleNode = (ModuleNode) node

                if (moduleNode.mainClassName.startsWith("Condition")) {
                    moduleNode.statementBlock?.statements?.each { statement ->
                        if (statement instanceof ExpressionStatement) {
                            def expression = ((ExpressionStatement) statement).expression
                            if (expression instanceof MethodCallExpression) {
                                def methodCall = ((MethodCallExpression) expression)
                                if (methodCall.method instanceof ConstantExpression && methodCall.objectExpression instanceof VariableExpression) {

                                    if (((ConstantExpression) methodCall.method).value == "setVariable"
                                            && ((VariableExpression) methodCall.objectExpression).getName() == "binding") {
                                        throw new ScriptException("binding.setVariable is not allowed in condition node")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (flowProperties.enableAST) {
            for (final def aClass in source.getAST().getClasses()) {
                aClass.visitContents(new GroovyShellVisitor(source))
            }
        }

    }
}
