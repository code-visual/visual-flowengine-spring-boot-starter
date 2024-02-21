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


import io.github.code.visual.utils.SpringContext
import io.github.code.visual.workflow.WorkflowManager

/**
 * �����Ӧ�ó�������������ӵ�ҵ���߼�����Щ�߼���Ҫ���ݶ���������о���ʱ����������ʹ����Щ�߼��Ĺ����ø��Ӽ��к�ϵͳ����
 * ��ҵ����򾭳��������ϣ���ܹ���̬����ӡ��޸Ļ��Ƴ����򣬶�����Ҫÿ�ζ����²�������Ӧ�ó���ʱ��
 * �����Ҫ�����������򣬲����ݹ�������ȼ�������������̬��ѡ����Щ����Ӧ�ñ�ִ��
 * if-else��������ڴ����еļ������߼�����Groovy�������������ڹ����ӵ�ҵ�����;����߼�.�����ǵ�ǰҵ���߼��Ƚϼ��� �ĵط�.�������Ӱ��ͬһ������
 */
class RuleEngine {
    /**
     * ��������ĺ����ǹ��򡣹�����һ�����������հ��Ķ���һ���հ������������������Ƿ�����������������һ���հ�����Ӧ�ù���
     * ����������ִ��ʱ������������й����������ǵ�����������������㣬���������Ӧ�ù���
     */

    static String execute(List<Rule> rules, Binding inputData) {
        // ��������ƥ��Ĺ���
        List<Rule> matchedRules = rules.findAll { rule ->
            rule.when(inputData)
        }

        // ���û�й���ƥ��ɹ�����"miss"��ֵ��"decision_rule"
        if (matchedRules.isEmpty()) {
            inputData.setVariable("decision_rule", "miss")
        } else {
            // �����ƥ��Ĺ��򣬸���"decision_rule"Ϊƥ�����������б�
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

//             Closure.OWNER_FIRST (Ĭ��ֵ): ����հ������õı����򷽷��ڱհ�������δ���壬��Groovy���ȳ����ڱհ���ӵ���ߣ�ͨ���Ƕ���հ����Ǹ��������н�����Щ���á������ӵ������Ҳ�Ҳ�����Groovy�᳢����delegate�����н�����
//             Closure.DELEGATE_FIRST: ����հ������õı����򷽷��ڱհ�������δ���壬��Groovy���ȳ����ڱհ���delegate�����н�����Щ���á������delegate���Ҳ�����Groovy�᳢���ڱհ���ӵ�����н�����
//             Closure.OWNER_ONLY: Groovyֻ���ڱհ���ӵ�����н����հ������õı����򷽷���
//             Closure.DELEGATE_ONLY: Groovyֻ���ڱհ���delegate�����н����հ������õı����򷽷���
//             Closure.TO_SELF: Groovyֻ���ڱհ�������������ڽ��������򷽷�������հ��ڲ�û�ж������õı����򷽷������ᵼ�½���ʧ�ܡ�
            cl.resolveStrategy = 1
            cl.call()
            localRules.add(newRule)
        })

        def parseScript = SpringContext.getBean(WorkflowManager.class).parseGroovyScript(rulesDefinition, binding)
        parseScript.run()
        return localRules
    }
}
