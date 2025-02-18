package yazio.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.parents

class MagicAndroidVersionsRule :
  Rule(
    ruleId = RuleId("yazio:android-versions"),
    about = aboutYazio,
  ),
  RuleAutocorrectApproveHandler {
  private val reported = mutableSetOf<Int>()

  override fun beforeVisitChildNodes(
    node: ASTNode,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
  ) {
    fun report() {
      if (reported.add(node.startOffset)) {
        emit(
          node.startOffset,
          ERROR_MESSAGE,
          false,
        )
      }
    }
    when (node.elementType) {
      IMPORT_DIRECTIVE -> {
        if (node.text.startsWith("import android.os.Build.VERSION_CODES")) {
          report()
        }
      }

      DOT_QUALIFIED_EXPRESSION -> {
        if (node.text.contains("Build.VERSION_CODES") && node.parents().none { it.elementType == IMPORT_DIRECTIVE }) {
          report()
        }
      }
    }
  }

  override fun afterLastNode() {
    super.afterLastNode()
    reported.clear()
  }

  companion object {
    val ERROR_MESSAGE =
      """
      Use the value of the constants directly. For Android version
      codes, the tooling and documentation most of the times refers
      to the actual integer values. Therefore in code we should refer
      to integer values as well.
      """.trimIndent()
  }
}
