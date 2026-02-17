package yazio.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class MetroRuntimeDependencyRule :
  Rule(
    ruleId = RuleId("yazio:metro-runtime"),
    about = aboutYazio,
  ),
  RuleAutocorrectApproveHandler {

  override fun beforeVisitChildNodes(
    node: ASTNode,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
  ) {
    if (node.elementType == IMPORT_DIRECTIVE &&
      node.text.startsWith("import dev.zacsweers.metro.") &&
      node.text.substringAfter("import ") !in ALLOWED_IMPORTS
    ) {
      emit(node.startOffset, ERROR_MESSAGE, false)
    }
  }

  companion object {
    val ALLOWED_IMPORTS = setOf(
      "dev.zacsweers.metro.Includes",
      "dev.zacsweers.metro.createGraphFactory",
      "dev.zacsweers.metro.GraphExtension",
      "dev.zacsweers.metro.HasMemberInjections",
      "dev.zacsweers.metro.gradle.MetroPluginExtension",
    )

    val ERROR_MESSAGE =
      """
      Only approved Metro runtime APIs are allowed.
      Allowed imports: ${ALLOWED_IMPORTS.joinToString(", ")}.
      """.trimIndent()
  }
}
