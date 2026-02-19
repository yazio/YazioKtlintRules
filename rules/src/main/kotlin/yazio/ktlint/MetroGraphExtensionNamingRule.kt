package yazio.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction

class MetroGraphExtensionNamingRule :
  Rule(
    ruleId = RuleId("yazio:metro-graph-extension-naming"),
    about = aboutYazio,
  ),
  RuleAutocorrectApproveHandler {
  override fun beforeVisitChildNodes(
    node: ASTNode,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
  ) {
    if (node.elementType != CLASS) return

    val ktClass = node.psi as? KtClass ?: return
    if (!ktClass.isInterface()) return

    when {
      ktClass.hasAnnotationNamed(GRAPH_EXTENSION_ANNOTATION) ->
        checkGraphExtension(ktClass, node, emit)
      ktClass.hasAnnotationNamed(GRAPH_EXTENSION_FACTORY_ANNOTATION) ->
        checkFactory(ktClass, node, emit)
    }
  }

  private fun checkGraphExtension(
    ktClass: KtClass,
    node: ASTNode,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
  ) {
    val name = ktClass.name ?: return

    if (!name.endsWith(GRAPH_SUFFIX)) {
      emit(node.startOffset, ERROR_GRAPH_SUFFIX, false)
    }

    ktClass.declarations
      .filterIsInstance<KtNamedFunction>()
      .filter { it.valueParameters.isNotEmpty() }
      .forEach { function ->
        if (function.name != INJECT_FUNCTION_NAME) {
          emit(function.node.startOffset, ERROR_INJECT_NAME, false)
        }
      }
  }

  private fun checkFactory(
    ktClass: KtClass,
    node: ASTNode,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
  ) {
    if (ktClass.name != FACTORY_INTERFACE_NAME) {
      emit(node.startOffset, ERROR_FACTORY_NAME, false)
    }

    val parentGraph = ktClass.parent?.parent as? KtClass
    val parentName = parentGraph?.name

    if (parentName != null) {
      ktClass.declarations
        .filterIsInstance<KtNamedFunction>()
        .forEach { function ->
          val expectedName = "$CREATE_FUNCTION_PREFIX$parentName"
          if (function.name != expectedName) {
            emit(
              function.node.startOffset,
              "$ERROR_CREATE_PREFIX'$expectedName' but found '${function.name}'.",
              false,
            )
          }
        }
    }
  }

  private fun KtClass.hasAnnotationNamed(name: String): Boolean {
    return annotationEntries.any { entry ->
      val text = entry.typeReference?.text ?: return@any false
      text == name
    }
  }

  companion object {
    private const val GRAPH_EXTENSION_ANNOTATION = "GraphExtension"
    private const val GRAPH_EXTENSION_FACTORY_ANNOTATION = "GraphExtension.Factory"
    private const val GRAPH_SUFFIX = "Graph"
    private const val FACTORY_INTERFACE_NAME = "Factory"
    private const val INJECT_FUNCTION_NAME = "inject"
    private const val CREATE_FUNCTION_PREFIX = "create"

    const val ERROR_GRAPH_SUFFIX =
      "GraphExtension interface name must end with '$GRAPH_SUFFIX'."

    const val ERROR_FACTORY_NAME =
      "GraphExtension.Factory interface must be named '$FACTORY_INTERFACE_NAME'."

    const val ERROR_INJECT_NAME =
      "Functions with parameters on a GraphExtension interface must be named '$INJECT_FUNCTION_NAME'."

    const val ERROR_CREATE_PREFIX =
      "GraphExtension.Factory function must be named "
  }
}
