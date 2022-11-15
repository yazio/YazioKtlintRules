package yazio.ktlint

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.children
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

private val methodNamesWithExpectedArgumentsCount = mapOf(
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

class NonReflectionSerializationRule : Rule("yazio:serialization-no-reflect") {

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
  ) {
    if (node.elementType != ElementType.CALL_EXPRESSION) return

    val referenceExpression = node.findChildByType(REFERENCE_EXPRESSION) ?: return
    val requiredArgumentCount = methodNamesWithExpectedArgumentsCount[referenceExpression.text]
      ?: return

    val actualArgumentCount = node.findChildByType(VALUE_ARGUMENT_LIST)!!.children()
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
    val ERROR_MESSAGE = """For better performance, specify the serializer manually instead of relying on reflection.
      |Instead of json.encodeToString(Person("Alice")), use json.encodeToString(Person.serializer(), Person("Alice"))
    """.trimMargin()
  }
}
