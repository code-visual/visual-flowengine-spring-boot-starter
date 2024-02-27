package groovy


onMethodSelection { expr, methodNode ->

    // 安全检查，防止调用敏感方法
    if (methodNode.declaringClass.name=='java.lang.System' ||
            methodNode.declaringClass.name=='java.lang.Runtime' ||
            methodNode.declaringClass.name=='java.lang.Class') {
        addStaticTypeError("Method is not allowed!", expr)
    }
}
