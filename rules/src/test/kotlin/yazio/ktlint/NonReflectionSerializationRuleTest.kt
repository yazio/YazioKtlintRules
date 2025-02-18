package yazio.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class NonReflectionSerializationRuleTest {

  @Test
  fun `reports errors for JSON serialization calls using reflection`() {
    KtLintAssertThat.assertThatRule { NonReflectionSerializationRule() }(
        // language=kotlin
        """
      package test

      import kotlinx.serialization.decodeFromString
      import kotlinx.serialization.encodeToString
      import kotlinx.serialization.json.Json

      private fun testJson() {
        // Reflection-based calls: these should trigger errors.
        Json.encodeToString(42)
        Json.decodeFromString<Int>("42")
      }
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
        LintViolation(9, 8, NonReflectionSerializationRule.ERROR_MESSAGE),
        LintViolation(10, 8, NonReflectionSerializationRule.ERROR_MESSAGE),
    )
  }

  @Test
  fun `reports errors for stream serialization calls using reflection`() {
    KtLintAssertThat.assertThatRule { NonReflectionSerializationRule() }(
        // language=kotlin
        """
      package test

      import kotlinx.serialization.decodeFromStream
      import kotlinx.serialization.encodeToStream
      import kotlinx.serialization.json.Json
      import java.io.ByteArrayInputStream
      import java.io.ByteArrayOutputStream

      private fun testStream() {
        // Reflection-based calls: these should trigger errors.
        Json.encodeToStream("42", ByteArrayOutputStream())
        Json.decodeFromStream<Int>(ByteArrayInputStream(ByteArray(0)))
      }
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
        LintViolation(11, 8, NonReflectionSerializationRule.ERROR_MESSAGE),
        LintViolation(12, 8, NonReflectionSerializationRule.ERROR_MESSAGE),
    )
  }

  @Test
  fun `reports errors for JSON element serialization calls using reflection`() {
    KtLintAssertThat.assertThatRule { NonReflectionSerializationRule() }(
        // language=kotlin
        """
      package test

      import kotlinx.serialization.decodeFromJsonElement
      import kotlinx.serialization.encodeToJsonElement
      import kotlinx.serialization.json.Json
      import kotlinx.serialization.json.JsonObject

      private fun testJsonElement() {
        // Reflection-based calls: these should trigger errors.
        Json.encodeToJsonElement(42)
        Json.decodeFromJsonElement<Int>(JsonObject(emptyMap()))
      }
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
        LintViolation(10, 8, NonReflectionSerializationRule.ERROR_MESSAGE),
        LintViolation(11, 8, NonReflectionSerializationRule.ERROR_MESSAGE),
    )
  }

  @Test
  fun `reports errors for proto byte array and hex string serialization calls using reflection`() {
    KtLintAssertThat.assertThatRule { NonReflectionSerializationRule() }(
        // language=kotlin
        """
      package test

      import kotlinx.serialization.decodeFromByteArray
      import kotlinx.serialization.decodeFromHexString
      import kotlinx.serialization.encodeToByteArray
      import kotlinx.serialization.encodeToHexString
      import kotlinx.serialization.protobuf.ProtoBuf

      private fun testProto() {
        // Reflection-based calls: these should trigger errors.
        ProtoBuf.encodeToByteArray(42)
        ProtoBuf.decodeFromByteArray<Int>(ByteArray(0))
        ProtoBuf.encodeToHexString(42)
        ProtoBuf.decodeFromHexString<Int>("")
      }
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
        LintViolation(11, 12, NonReflectionSerializationRule.ERROR_MESSAGE),
        LintViolation(12, 12, NonReflectionSerializationRule.ERROR_MESSAGE),
        LintViolation(13, 12, NonReflectionSerializationRule.ERROR_MESSAGE),
        LintViolation(14, 12, NonReflectionSerializationRule.ERROR_MESSAGE),
    )
  }

  @Test
  fun `reports error for forbidden import of kotlinx serialization serializer`() {
    KtLintAssertThat.assertThatRule { NonReflectionSerializationRule() }(
        // language=kotlin
        """
      package test

      import kotlinx.serialization.serializer

      private fun dummy() {}
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
        LintViolation(3, 1, NonReflectionSerializationRule.IMPORT_ERROR_MESSAGE),
    )
  }

  @Test
  fun `happy path does not report any errors`() {
    KtLintAssertThat.assertThatRule { NonReflectionSerializationRule() }(
        // language=kotlin
        """
      package test

      import kotlinx.serialization.builtins.serializer
      import kotlinx.serialization.decodeFromString
      import kotlinx.serialization.encodeToString
      import kotlinx.serialization.json.Json

      private fun testJson() {
        // Correct usage with explicit serializers: no errors expected.
        Json.encodeToString(Int.serializer(), 42)
        Json.decodeFromString(Int.serializer(), "42")
      }
      """.trimIndent(),
    ).hasNoLintViolations()
  }
}
