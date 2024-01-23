package com.github.managetech.ruleengine

import com.github.managetech.scriptcache.WorkflowEngine
import com.github.managetech.utils.SpringContext

class RuleParser {
    private List<Rule> rules = []

    private def parseRule(String name, Closure cl) {
        Rule newRule = new Rule(name: name)
        cl.delegate = newRule
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl.call()
        rules.add(newRule)
    }


    def parse(String script) {

        def bindings = new Binding()
        bindings.setVariable("rule", this.&parseRule)

        def parseScript = SpringContext.getBean(WorkflowEngine.class).parseGroovyScript(script, bindings)
        parseScript.run()
        return rules

    }
}
