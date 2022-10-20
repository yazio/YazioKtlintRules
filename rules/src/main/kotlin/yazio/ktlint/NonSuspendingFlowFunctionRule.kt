package yazio.ktlint

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.SUSPEND_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class NonSuspendingFlowFunctionRule : Rule("yazio:suspending-flow") {

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {
    if (node.elementType != FUN) return
    val typeReference = node.findChildByType(TYPE_REFERENCE)
      ?: return
    if (!typeReference.text.startsWith("Flow<")) {
      return
    }
    val modifierList = node.findChildByType(MODIFIER_LIST)
      ?: return
    val suspendKeyword = modifierList.findChildByType(SUSPEND_KEYWORD)
      ?: return
    emit(
      suspendKeyword.startOffset,
      ERROR_MESSAGE,
      false
    )
  }

  companion object {
    val ERROR_MESSAGE = """
      Functions that return a Flow should not suspend.
      If you need some form of initial calculation you can use
      flow {
        val initial = suspendingCall()
        emitAll(myFlow(initial))
      }
    """.trimIndent()
  }
}
