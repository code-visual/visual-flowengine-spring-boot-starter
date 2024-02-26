package groovy

import org.codehaus.groovy.ast.expr.MethodCallExpression

onMethodSelection { expr, methodNode ->

//    GroovyTypeCheckingExtensionSupport groovyTypeCheckingExtensionSupport = new GroovyTypeCheckingExtensionSupport();

    if (expr instanceof MethodCallExpression) {
        // 检查方法名
        if (expr.methodAsString == 'decision_rule') {
            return // 如果是decision_rule方法，就不进行后续的安全检查
        }
    }

    // 安全检查，防止调用敏感方法
    if (methodNode.declaringClass.name=='java.lang.System' ||
            methodNode.declaringClass.name=='java.lang.Runtime' ||
            methodNode.declaringClass.name=='java.lang.Class') {
        addStaticTypeError("Method is not allowed!", expr)
    }
}
