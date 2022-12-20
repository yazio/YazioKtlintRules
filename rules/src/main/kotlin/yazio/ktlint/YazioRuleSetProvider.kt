package yazio.ktlint

import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2

@Suppress("unused")
class YazioRuleSetProvider : RuleSetProviderV2(
  id = "yazio",
  about = About(
    maintainer = "YAZIO",
    description = "YAZIO ktlint rules",
    license = "https://github.com/yazio/YazioKtlintRules/blob/main/LICENSE",
    repositoryUrl = "https://github.com/yazio/YazioKtlintRules",
    issueTrackerUrl = "https://github.com/yazio/YazioKtlintRules/issues",
  ),
) {
  override fun getRuleProviders(): Set<RuleProvider> {
    return setOf(
      RuleProvider { NonSuspendingFlowFunctionRule() },
      RuleProvider { NonReflectionSerializationRule() },
      RuleProvider { MagicAndroidVersionsRule() },
    )
  }
}
