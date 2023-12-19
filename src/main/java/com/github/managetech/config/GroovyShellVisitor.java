package com.github.managetech.config;

import com.google.common.collect.ImmutableList;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;

import java.util.*;

public class GroovyShellVisitor extends ClassCodeVisitorSupport implements GroovyClassVisitor {

    private static final List<String> EXCLUDE_IN_PARAM
            = ImmutableList.of("args", "context", "this", "super");

    private final Map<String, Class> dynamicVariables = new HashMap<>();

    private final Set<String> declarationVariables = new HashSet<>();

    /**
     * 记录Groovy解析过程的变量
     **/
    @Override
    public void visitVariableExpression(VariableExpression expression) {    //变量表达式分析
        super.visitVariableExpression(expression);
        if (EXCLUDE_IN_PARAM.stream().noneMatch(x -> x.equals(expression.getName()))) {

            if (!declarationVariables.contains(expression.getName())) {

                if (expression.getAccessedVariable() instanceof DynamicVariable) { // 动态类型,变量类型都是Object
                    dynamicVariables.put(expression.getName(), expression.getOriginType().getTypeClass());
                } else {
                    // 静态类型 Groovy支持静态类型
                    dynamicVariables.put(expression.getName(), expression.getOriginType().getTypeClass());
                }
            }
        }
    }

    @Override
    public void visitMethod(MethodNode methodNode) {
        System.out.println("visitMethod = " + methodNode.getName());

    }

    /**
     * 获取脚本内部声明的变量
     */
    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        // 保存脚本内部定义变量
        declarationVariables.add(expression.getVariableExpression().getName());
        super.visitDeclarationExpression(expression);
    }

    /**
     * 忽略对语法树闭包的访问
     */
    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        // ignore
        System.out.println("expression = " + expression);
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