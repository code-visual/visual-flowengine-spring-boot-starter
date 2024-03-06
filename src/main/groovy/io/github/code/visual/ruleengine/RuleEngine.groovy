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
package io.github.code.visual.ruleengine

import io.github.code.visual.model.ScriptMetadata
import io.github.code.visual.utils.SpringContext
import io.github.code.visual.workflow.WorkflowManager

class RuleEngine {


    static String execute(List<Rule> rules, Binding inputData) {

        List<Rule> matchedRules = rules.findAll { rule ->
            rule.when(inputData)
        }
        List<String> decisionRules = ['']

        if (matchedRules.isEmpty()) {
            def decisionRuleList = inputData.getVariables().get("decision_rule")
            if (decisionRuleList != null && (decisionRuleList instanceof List)) {
                decisionRuleList.add("miss")
            } else {
                inputData.setVariable("decision_rule", ['miss'])
            }
        } else {
            matchedRules.each { rule ->
                rule.then(inputData)
            }
            decisionRules = matchedRules.collect { it.name }


            def decisionRuleList = inputData.getVariables().get("decision_rule")
            if (decisionRuleList != null && (decisionRuleList instanceof List)) {
                decisionRuleList.addAll(decisionRules)
            } else {
                inputData.setVariable("decision_rule", decisionRules)
            }
        }
        return decisionRules.toString()
    }

    static List<Rule> parser(ScriptMetadata rulesDefinition) {
        List<Rule> localRules = []
        def binding = new Binding();
        binding.setVariable("decision_rule", { String name, Closure cl ->
            def newRule = new Rule(name: name)
            cl.delegate = newRule
            cl.resolveStrategy = 1
            cl.call()
            localRules.add(newRule)
        })

        SpringContext.getBean(WorkflowManager.class).executeScript(rulesDefinition, binding)
        return localRules
    }
}
