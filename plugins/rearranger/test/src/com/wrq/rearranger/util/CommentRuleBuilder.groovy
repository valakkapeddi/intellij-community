package com.wrq.rearranger.util;

import com.wrq.rearranger.settings.CommentRule;
import com.wrq.rearranger.settings.RearrangerSettings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Denis Zhdanov
 * @since 5/17/12 4:08 PM
 */
class CommentRuleBuilder extends AbstractRuleBuilder<CommentRule> {
  
  {
    def helper = { RearrangerTestDsl dslProperty, map, rulePropertyName, rule ->
      if (map.containsKey(dslProperty.value)) {
        rule."$rulePropertyName" = map[dslProperty.value]
      }
    }
    
    registerHandler(RearrangerTestDsl.COMMENT, { data, attributes, rule ->
      rule.commentText = data
      helper(RearrangerTestDsl.CONDITION, attributes, 'emitCondition', rule)
      helper(RearrangerTestDsl.ALL_SUBSEQUENT, attributes, 'allSubsequentRules', rule)
      helper(RearrangerTestDsl.ALL_PRECEDING, attributes, 'allPrecedingRules', rule)
      helper(RearrangerTestDsl.SUBSEQUENT_RULES_TO_MATCH, attributes, 'NSubsequentRulesToMatch', rule)
      helper(RearrangerTestDsl.PRECEDING_RULES_TO_MATCH, attributes, 'NPrecedingRulesToMatch', rule)
    })
  }
  
  @NotNull
  @Override
  protected CommentRule createRule() {
    new CommentRule()
  }

  @Override
  protected void registerRule(@NotNull RearrangerSettings settings, @NotNull CommentRule rule) {
    settings.addItem(rule) 
  }
}