package yazio.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class NonReflectionSerializationRuleTest {

  @Test
  fun suspendingFlowShowsError() {
    KtLintAssertThat.assertThatRule { NonReflectionSerializationRule() }(
      // language=kotlin
      """
package test

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private fun jsonString() {
  Json.encodeToString(42)
  Json.encodeToString(Int.serializer(), 42)
  Json.decodeFromString<Int>("42")
  Json.decodeFromString(Int.serializer(), "42")
}

private fun jsonStream() {
  Json.encodeToStream("42", ByteArrayOutputStream())
  Json.encodeToStream(String.serializer(), "42", ByteArrayOutputStream())
  Json.decodeFromStream<Int>(ByteArrayInputStream(ByteArray(0)))
  Json.decodeFromStream(Int.serializer(), ByteArrayInputStream(ByteArray(0)))
}

private fun jsonElement() {
  Json.encodeToJsonElement(42)
  Json.encodeToJsonElement(Int.serializer(), 42)
  Json.decodeFromJsonElement<Int>(JsonObject(emptyMap()))
  Json.decodeFromJsonElement(Int.serializer(), JsonObject(emptyMap()))
}

private fun protoByteArray() {
  ProtoBuf.encodeToByteArray(42)
  ProtoBuf.encodeToByteArray(Int.serializer(), 42)
  ProtoBuf.decodeFromByteArray<Int>(ByteArray(0))
  ProtoBuf.decodeFromByteArray(Int.serializer(), ByteArray(0))
}

private fun protoHexString() {
  ProtoBuf.encodeToHexString(42)
  ProtoBuf.encodeToHexString(Int.serializer(), 42)
  ProtoBuf.decodeFromHexString<Int>("")
  ProtoBuf.decodeFromHexString(Int.serializer(), "")
}
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
      LintViolation(21, 8, NonReflectionSerializationRule.ERROR_MESSAGE),
      LintViolation(23, 8, NonReflectionSerializationRule.ERROR_MESSAGE),
      LintViolation(28, 8, NonReflectionSerializationRule.ERROR_MESSAGE),
      LintViolation(30, 8, NonReflectionSerializationRule.ERROR_MESSAGE),
      LintViolation(35, 8, NonReflectionSerializationRule.ERROR_MESSAGE),
      LintViolation(37, 8, NonReflectionSerializationRule.ERROR_MESSAGE),
      LintViolation(42, 12, NonReflectionSerializationRule.ERROR_MESSAGE),
      LintViolation(44, 12, NonReflectionSerializationRule.ERROR_MESSAGE),
      LintViolation(49, 12, NonReflectionSerializationRule.ERROR_MESSAGE),
      LintViolation(51, 12, NonReflectionSerializationRule.ERROR_MESSAGE),
    )
  }
}
