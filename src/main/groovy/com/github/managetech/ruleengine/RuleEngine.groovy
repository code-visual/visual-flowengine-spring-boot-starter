package com.github.managetech.ruleengine

import com.github.managetech.utils.SpringContext
import com.github.managetech.workflow.WorkflowManager


class RuleEngine {

    static def execute(List<Rule> rules, Object user) {

        def matchedRule = rules.find { rule ->
            rule.when(user)
        }
        if (matchedRule) {
            def action = matchedRule.then(user)
            action["ruleName"] = matchedRule.name
            return action
        }
        return null
    }

    static def parser(String rulesDefinition) {
         def binding = new Binding()
         List<Rule> localRules = []
         binding.setVariable("rule", { String name, Closure cl ->
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
