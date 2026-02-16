package yazio.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtTypeElement
import org.jetbrains.kotlin.psi.KtUserType

class NonSuspendingFlowFunctionRule :
  Rule(
    ruleId = RuleId("yazio:suspending-flow"),
    about = aboutYazio,
  ),
  RuleAutocorrectApproveHandler {
  override fun beforeVisitChildNodes(
    node: ASTNode,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
  ) {
    if (node.elementType != TYPE_REFERENCE) return

    val function = node.psi.parent as? KtNamedFunction ?: return
    val returnType = function.typeReference ?: return
    if (returnType.node != node) return

    if (!isFlowType(returnType.typeElement)) return
    if (!function.hasModifier(KtTokens.SUSPEND_KEYWORD)) return
    val suspendKeywordOffset = function.modifierList?.getModifier(KtTokens.SUSPEND_KEYWORD)?.textOffset ?: node.startOffset

    emit(
      suspendKeywordOffset,
      ERROR_MESSAGE,
      false,
    )
  }

  private fun isFlowType(typeElement: KtTypeElement?): Boolean {
    val unwrappedType = when (typeElement) {
      is KtNullableType -> typeElement.innerType
      else -> typeElement
    }

    val userType = unwrappedType as? KtUserType ?: return false
    return userType.referencedName == "Flow"
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
