package yazio.ktlint

import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId

@Suppress("unused")
class YazioRuleSetProvider : RuleSetProviderV3(RuleSetId("yazio")) {
  override fun getRuleProviders(): Set<RuleProvider> {
    return setOf(
      RuleProvider { NonSuspendingFlowFunctionRule() },
      RuleProvider { NonReflectionSerializationRule() },
      RuleProvider { SealedSerializableClassSerialNameRule() },
      RuleProvider { MagicAndroidVersionsRule() },
    )
  }
}
