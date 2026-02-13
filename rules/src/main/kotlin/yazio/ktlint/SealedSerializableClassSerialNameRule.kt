package yazio.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class SealedSerializableClassSerialNameRule :
  Rule(
    ruleId = RuleId("yazio:sealed-serializable-class-serial-name"),
    about = aboutYazio,
  ),
  RuleAutocorrectApproveHandler {
  private val sealedTypesByFileOffset = mutableMapOf<Int, Set<String>>()

  override fun beforeVisitChildNodes(
    node: ASTNode,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
  ) {
    if (node.elementType == FILE) {
      sealedTypesByFileOffset[node.startOffset] = collectSealedTypeNames(node)
      return
    }

    val declaration = node.psi as? KtClassOrObject ?: return
    if (declaration is KtClass && declaration.isInterface()) return
    if (!declaration.hasAnnotationNamed("Serializable")) return
    if (declaration.hasAnnotationNamed("SerialName")) return

    val fileNode = node.psi.containingFile.node
    val sealedTypes = sealedTypesByFileOffset[fileNode.startOffset] ?: emptySet()
    if (sealedTypes.isEmpty()) return

    val superTypeNames = declaration.superTypeListEntries.mapNotNull { it.referencedTypeName() }
    if (superTypeNames.any(sealedTypes::contains)) {
      emit(node.startOffset, ERROR_MESSAGE, false)
    }
  }

  override fun afterLastNode() {
    super.afterLastNode()
    sealedTypesByFileOffset.clear()
  }

  private fun collectSealedTypeNames(fileNode: ASTNode): Set<String> {
    val declarations =
      fileNode.psi.collectDescendantsOfType<KtClassOrObject> { declaration ->
        declaration.hasModifier(KtTokens.SEALED_KEYWORD)
      }

    return declarations.mapNotNull { it.name }.toSet()
  }

  private fun KtClassOrObject.hasAnnotationNamed(name: String): Boolean {
    return annotationEntries.any { entry ->
      val annotationName = entry.shortName?.asString() ?: return@any false
      annotationName == name
    }
  }

  private fun KtSuperTypeListEntry.referencedTypeName(): String? {
    val superTypeText = typeReference?.text ?: return null
    return superTypeText
      .substringBefore('<')
      .substringBefore('(')
      .substringAfterLast('.')
      .substringAfterLast('?')
      .takeIf { it.isNotBlank() }
  }

  companion object {
    const val ERROR_MESSAGE =
      "A @Serializable class that is part of a sealed hierarchy must be annotated with @SerialName."
  }
}
