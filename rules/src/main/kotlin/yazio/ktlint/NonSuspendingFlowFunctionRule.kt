package yazio.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType.EQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SUSPEND_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.nextCodeLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class NonSuspendingFlowFunctionRule : Rule(
  ruleId = RuleId("yazio:suspending-flow"),
  about = aboutYazio,
) {
  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
  ) {
    if (node.elementType != TYPE_REFERENCE) return

    val treeParent = node.treeParent

    if (treeParent.elementType != FUN) return

    if (!node.text.startsWith("Flow<")) return

    val suspendKeyword =
      treeParent?.findChildByType(MODIFIER_LIST)?.findChildByType(SUSPEND_KEYWORD)
        ?: return

    val nextCodeLeafType =
      node.nextCodeLeaf(skipSubtree = true)?.elementType
        ?: return

    if (nextCodeLeafType != LBRACE && nextCodeLeafType != EQ) return

    emit(
      suspendKeyword.startOffset,
      ERROR_MESSAGE,
      false,
    )
  }

  companion object {
    val ERROR_MESSAGE =
      """
      Functions that return a Flow should not suspend.
      If you need some form of initial calculation you can use
      flow {
        val initial = suspendingCall()
        emitAll(myFlow(initial))
      }
      """.trimIndent()
  }
}
