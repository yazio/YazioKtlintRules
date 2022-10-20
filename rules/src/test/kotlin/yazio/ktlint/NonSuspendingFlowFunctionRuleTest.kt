package yazio.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class NonSuspendingFlowFunctionRuleTest {

  @Test
  fun suspendingFlowShowsError() {
    assertThatRule { NonSuspendingFlowFunctionRule() }(
      // language=kotlin
      """
  import kotlinx.coroutines.flow.Flow

  class MyClass {

    suspend fun suspendingFlow(): Flow<Unit> {
      TODO()
    }
  }

  """.trimIndent()
    ).hasLintViolationWithoutAutoCorrect(5, 3, NonSuspendingFlowFunctionRule.ERROR_MESSAGE)
  }

  @Test
  fun noErrorOnRegularSuspend() {
    assertThatRule { NonSuspendingFlowFunctionRule() }(
      // language=kotlin
      """
  import kotlinx.coroutines.flow.Flow

  class MyClass {

    suspend fun suspendMe(): Int {
      TODO()
    }
  }

""".trimIndent()
    ).hasNoLintViolations()
  }


  @Test
  fun noErrorOnNonSuspendingFlow() {
    assertThatRule { NonSuspendingFlowFunctionRule() }(
      // language=kotlin
      """
  import kotlinx.coroutines.flow.Flow

  class MyClass {

    fun suspendingFlow(): Flow<Unit> {
      TODO()
    }
  }

""".trimIndent()
    ).hasNoLintViolations()
  }
}
