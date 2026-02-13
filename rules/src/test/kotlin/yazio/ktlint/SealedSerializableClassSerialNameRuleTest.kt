package yazio.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class SealedSerializableClassSerialNameRuleTest {
  @Test
  fun `reports error for serializable class implementing sealed interface without serial name`() {
    KtLintAssertThat.assertThatRule { SealedSerializableClassSerialNameRule() }(
      // language=kotlin
      """
      package test

      import kotlinx.serialization.Serializable

      sealed interface Message

      @Serializable
      data class StartMessage(val id: String) : Message
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
      LintViolation(7, 1, SealedSerializableClassSerialNameRule.ERROR_MESSAGE),
    )
  }

  @Test
  fun `reports error for serializable class extending sealed class without serial name`() {
    KtLintAssertThat.assertThatRule { SealedSerializableClassSerialNameRule() }(
      // language=kotlin
      """
      package test

      import kotlinx.serialization.Serializable

      sealed class Event

      @Serializable
      class LoginEvent : Event()
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
      LintViolation(7, 1, SealedSerializableClassSerialNameRule.ERROR_MESSAGE),
    )
  }

  @Test
  fun `does not report error for serializable class with serial name`() {
    KtLintAssertThat.assertThatRule { SealedSerializableClassSerialNameRule() }(
      // language=kotlin
      """
      package test

      import kotlinx.serialization.SerialName
      import kotlinx.serialization.Serializable

      sealed interface Message

      @Serializable
      @SerialName("start")
      data class StartMessage(val id: String) : Message
      """.trimIndent(),
    ).hasNoLintViolations()
  }

  @Test
  fun `does not report error for serializable class not in sealed hierarchy`() {
    KtLintAssertThat.assertThatRule { SealedSerializableClassSerialNameRule() }(
      // language=kotlin
      """
      package test

      import kotlinx.serialization.Serializable

      interface Message

      @Serializable
      data class StartMessage(val id: String) : Message
      """.trimIndent(),
    ).hasNoLintViolations()
  }

  @Test
  fun `does not report error for non serializable class in sealed hierarchy`() {
    KtLintAssertThat.assertThatRule { SealedSerializableClassSerialNameRule() }(
      // language=kotlin
      """
      package test

      sealed interface Message

      data class StartMessage(val id: String) : Message
      """.trimIndent(),
    ).hasNoLintViolations()
  }
}
