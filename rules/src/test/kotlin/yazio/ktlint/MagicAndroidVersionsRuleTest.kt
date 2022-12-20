package yazio.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class MagicAndroidVersionsRuleTest {

  @Test
  fun errorInUsage() {
    assertThatRule { MagicAndroidVersionsRule() }(
      // language=kotlin
      """
package yazio.feature

import android.os.Build

fun main(){
  Build.VERSION_CODES.GINGERBREAD
}
      """.trimIndent(),
    ).hasLintViolationWithoutAutoCorrect(
      line = 6,
      col = 3,
      detail = MagicAndroidVersionsRule.ERROR_MESSAGE,
    )
  }

  @Test
  fun errorInImports() {
    assertThatRule { MagicAndroidVersionsRule() }(
      // language=kotlin
      """
package yazio.feature

import android.os.Build.VERSION_CODES.GINGERBREAD

fun main(){
  GINGERBREAD
}
      """.trimIndent(),
    )
      .hasLintViolationsWithoutAutoCorrect(
        LintViolation(3, 1, MagicAndroidVersionsRule.ERROR_MESSAGE),
      )
  }

  @Test
  fun noErrorWithoutVersionCodes() {
    assertThatRule { MagicAndroidVersionsRule() }(
      // language=kotlin
      """

      fun main(){

      }

      """.trimIndent(),
    ).hasNoLintViolations()
  }
}
