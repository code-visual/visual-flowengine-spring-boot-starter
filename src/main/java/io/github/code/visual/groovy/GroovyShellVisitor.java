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
package io.github.code.visual.groovy;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroovyShellVisitor extends ClassCodeVisitorSupport {

    private static final List<String> EXCLUDE_IN_PARAM = Arrays.asList("args", "context", "this", "super");

    private final Map<String, Class> dynamicVariables = new HashMap<>();

    private final Set<String> declarationVariables = new HashSet<>();


    @Override
    public void visitVariableExpression(VariableExpression expression) {    //�������ʽ����
        super.visitVariableExpression(expression);
        if (EXCLUDE_IN_PARAM.stream().noneMatch(x -> x.equals(expression.getName()))) {

            if (!declarationVariables.contains(expression.getName())) {

                if (expression.getAccessedVariable() instanceof DynamicVariable) { // ��̬����,�������Ͷ���Object
                    dynamicVariables.put(expression.getName(), expression.getOriginType().getTypeClass());
                } else {
                    // ��̬���� Groovy֧�־�̬����
                    dynamicVariables.put(expression.getName(), expression.getOriginType().getTypeClass());
                }
            }
        }
    }

    @Override
    public void visitMethod(MethodNode methodNode) {
        super.visitMethod(methodNode);
    }

    /**
     * ��ȡ�ű��ڲ������ı���
     */
    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        // ����ű��ڲ��������
        declarationVariables.add(expression.getVariableExpression().getName());
        super.visitDeclarationExpression(expression);
    }

    /**
     * ���Զ��﷨���հ��ķ���
     */
    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        // ignore
//        System.out.println("expression = " + expression);
    }

    public Set<String> getDynamicVariables() {
        return dynamicVariables.keySet();
    }

    public Map<String, Class> getDynamicVarAndClass() {
        return dynamicVariables;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }
}