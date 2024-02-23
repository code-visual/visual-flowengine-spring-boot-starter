package groovy

onMethodSelection { expr, methodNode ->
    if (methodNode.declaringClass.name=='java.lang.System'
            ||methodNode.declaringClass.name=='java.lang.Runtime'
            ||methodNode.declaringClass.name=='java.lang.Class') {
        addStaticTypeError("Method  is not allowed!", expr)
    }


}
