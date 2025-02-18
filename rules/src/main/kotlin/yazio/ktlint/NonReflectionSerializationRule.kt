package yazio.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType
import com.pinterest.ktlint.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.children
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

private val methodNamesWithExpectedArgumentsCount =
  mapOf(
    "encodeToString" to 2,
    "decodeFromString" to 2,
    "encodeToStream" to 3,
    "decodeFromStream" to 2,
    "encodeToJsonElement" to 2,
    "decodeFromJsonElement" to 2,
    "encodeToByteArray" to 2,
    "decodeFromByteArray" to 2,
    "encodeToHexString" to 2,
    "encodeToHexString" to 2,
    "decodeFromHexString" to 2,
  )

class NonReflectionSerializationRule :
  Rule(
    ruleId = RuleId("yazio:serialization-no-reflect"),
    about = aboutYazio,
  ),
  RuleAutocorrectApproveHandler {
  override fun beforeVisitChildNodes(
    node: ASTNode,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
  ) {
    if (node.elementType == ElementType.IMPORT_DIRECTIVE) {
      if (node.text.contains("import kotlinx.serialization.serializer")) {
        emit(
          node.startOffset,
          IMPORT_ERROR_MESSAGE,
          false,
        )
      }
      return
    }

    if (node.elementType != ElementType.CALL_EXPRESSION) return

    val referenceExpression = node.findChildByType(REFERENCE_EXPRESSION) ?: return
    val requiredArgumentCount =
      methodNamesWithExpectedArgumentsCount[referenceExpression.text]
        ?: return

    val actualArgumentCount =
      node.findChildByType(VALUE_ARGUMENT_LIST)!!.children()
        .count { it.elementType == VALUE_ARGUMENT }
    if (actualArgumentCount != requiredArgumentCount) {
      emit(
        node.startOffset,
        ERROR_MESSAGE,
        false,
      )
    }
  }

  companion object {
    val ERROR_MESSAGE =
      """For better performance, specify the serializer manually instead of relying on reflection.
      |Instead of json.encodeToString(Person("Alice")), use json.encodeToString(Person.serializer(), Person("Alice"))
      """.trimMargin()
    val IMPORT_ERROR_MESSAGE =
      """The import 'kotlinx.serialization.serializer' is forbidden.
        |To improve performance, use explicit serializers rather than relying on reflection.
      """.trimMargin()
  }
}
