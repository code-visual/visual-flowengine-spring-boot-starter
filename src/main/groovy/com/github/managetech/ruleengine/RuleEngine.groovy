package com.github.managetech.ruleengine


class RuleEngine {

    static def execute(List<Rule> rules, Object user) {

        def matchedRule = rules.find { rule ->
            rule.condition(user)
        }
        if (matchedRule) {
            def action = matchedRule.action(user)
            action["ruleName"] = matchedRule.name
            return action
        }
        return null
    }

    static def parser(String rulesDefinition) {
        RuleParser parser = new RuleParser()
        return parser.parse(rulesDefinition)
    }
}
