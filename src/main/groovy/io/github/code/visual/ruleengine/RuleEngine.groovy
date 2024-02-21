package io.github.code.visual.ruleengine


import io.github.code.visual.utils.SpringContext
import io.github.code.visual.workflow.WorkflowManager

/**
 * 当你的应用程序包含大量复杂的业务逻辑，这些逻辑需要根据多个变量进行决策时。规则引擎使得这些逻辑的管理变得更加集中和系统化。
 * 当业务规则经常变更，你希望能够动态地添加、修改或移除规则，而不需要每次都重新部署整个应用程序时。
 * 如果需要评估大量规则，并根据规则的优先级或其他条件动态地选择哪些规则应该被执行
 * if-else语句适用于代码中的简单条件逻辑，而Groovy规则引擎适用于管理复杂的业务规则和决策逻辑.并且是当前业务逻辑比较集中 的地方.多个条件影响同一个东西
 */
class RuleEngine {
    /**
     * 规则引擎的核心是规则。规则是一个包含两个闭包的对象。一个闭包用于评估输入数据是否满足规则的条件，另一个闭包用于应用规则。
     * 当规则引擎执行时，它会遍历所有规则并评估它们的条件。如果条件满足，规则引擎会应用规则。
     */

    static String execute(List<Rule> rules, Binding inputData) {
        // 查找所有匹配的规则
        List<Rule> matchedRules = rules.findAll { rule ->
            rule.when(inputData)
        }

        // 如果没有规则匹配成功，将"miss"赋值给"decision_rule"
        if (matchedRules.isEmpty()) {
            inputData.setVariable("decision_rule", "miss")
        } else {
            // 如果有匹配的规则，更新"decision_rule"为匹配规则的名称列表
            matchedRules.each { rule ->
                rule.then(inputData)
            }
            List<String> decisionRules = matchedRules.collect { it.name }
            inputData.setVariable("decision_rule", decisionRules)
        }

        return inputData.getVariable("decision_rule").toString()
    }

    static List<Rule> parser(String rulesDefinition) {
        List<Rule> localRules = []
        def binding = new Binding();
        binding.setVariable("decision_rule", { String name, Closure cl ->
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
