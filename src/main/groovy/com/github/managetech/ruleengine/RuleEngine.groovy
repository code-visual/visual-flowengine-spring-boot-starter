package com.github.managetech.ruleengine

import com.github.managetech.utils.SpringContext
import com.github.managetech.workflow.WorkflowManager

/**
 * 当你的应用程序包含大量复杂的业务逻辑，这些逻辑需要根据多个变量进行决策时。规则引擎使得这些逻辑的管理变得更加集中和系统化。
 * 当业务规则经常变更，你希望能够动态地添加、修改或移除规则，而不需要每次都重新部署整个应用程序时。
 * 如果需要评估大量规则，并根据规则的优先级或其他条件动态地选择哪些规则应该被执行
 * if-else语句适用于代码中的简单条件逻辑，而Groovy规则引擎适用于管理复杂的业务规则和决策逻辑.并且是当前业务逻辑比较集中 的地方.多个条件影响同一个东西
 */
class RuleEngine {

    static def execute(List<Rule> rules, Object inputData) {

        def matchedRule = rules.find { rule ->
            rule.when(inputData)
        }
        if (matchedRule) {
            def action = matchedRule.then(inputData)
            if (action instanceof List) {
                // 如果action是列表，对每个元素处理
                action.each { it["system_function_decision_rule"] = matchedRule.name }
            } else {
                // 对于单个对象，直接添加ruleName
                action["system_function_decision_rule"] = matchedRule.name
            }
            return action
        }
        return null
    }

    static List<Rule> parser(String rulesDefinition, Binding binding) {
        List<Rule> localRules = []
        binding.setVariable("system_function_decision_rule", { String name, Closure cl ->
            def newRule = new Rule(name: name)
            cl.delegate = newRule

//             Closure.OWNER_FIRST (默认值): 如果闭包内引用的变量或方法在闭包自身中未定义，则Groovy首先尝试在闭包的拥有者（通常是定义闭包的那个类或对象）中解析这些引用。如果在拥有者中也找不到，Groovy会尝试在delegate对象中解析。
//             Closure.DELEGATE_FIRST: 如果闭包内引用的变量或方法在闭包自身中未定义，则Groovy首先尝试在闭包的delegate对象中解析这些引用。如果在delegate中找不到，Groovy会尝试在闭包的拥有者中解析。
//             Closure.OWNER_ONLY: Groovy只会在闭包的拥有者中解析闭包内引用的变量或方法。
//             Closure.DELEGATE_ONLY: Groovy只会在闭包的delegate对象中解析闭包内引用的变量或方法。
//             Closure.TO_SELF: Groovy只会在闭包自身的作用域内解析变量或方法。如果闭包内部没有定义引用的变量或方法，将会导致解析失败。
            cl.resolveStrategy = 1
            cl.call()
            localRules.add(newRule)
        })

        def parseScript = SpringContext.getBean(WorkflowManager.class).parseGroovyScript(rulesDefinition, binding)
        parseScript.run()
        return localRules
    }
}
