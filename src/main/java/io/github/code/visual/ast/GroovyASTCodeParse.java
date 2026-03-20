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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import javax.script.ScriptException;

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class GroovyASTCodeParse implements ASTTransformation {

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        for (ASTNode node : nodes) {
            if (node instanceof ModuleNode) {
                ModuleNode moduleNode = (ModuleNode) node;

                if (moduleNode.getMainClassName().startsWith("Condition")) {
                    if (moduleNode.getStatementBlock() != null
                            && moduleNode.getStatementBlock().getStatements() != null) {
                        for (Statement statement : moduleNode.getStatementBlock().getStatements()) {
                            if (statement instanceof ExpressionStatement) {
                                Expression expression = ((ExpressionStatement) statement).getExpression();
                                if (expression instanceof MethodCallExpression) {
                                    MethodCallExpression methodCall = (MethodCallExpression) expression;
                                    if (methodCall.getMethod() instanceof ConstantExpression
                                            && methodCall.getObjectExpression() instanceof VariableExpression) {
                                        String methodName = ((ConstantExpression) methodCall.getMethod()).getValue().toString();
                                        String objName = ((VariableExpression) methodCall.getObjectExpression()).getName();
                                        if ("setVariable".equals(methodName) && "binding".equals(objName)) {
                                            throw new RuntimeException(
                                                    new ScriptException("binding.setVariable is not allowed in condition node"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (ClassNode classNode : source.getAST().getClasses()) {
            classNode.visitContents(new GroovyShellVisitor(source));
        }
    }
}
