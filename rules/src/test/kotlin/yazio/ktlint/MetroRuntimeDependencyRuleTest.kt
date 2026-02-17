package yazio.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MetroRuntimeDependencyRuleTest {

  @ParameterizedTest
  @ValueSource(
    strings = [
      "dev.zacsweers.metro.Includes",
      "dev.zacsweers.metro.createGraphFactory",
      "dev.zacsweers.metro.GraphExtension",
      "dev.zacsweers.metro.HasMemberInjections",
      "dev.zacsweers.metro.gradle.MetroPluginExtension",
    ],
  )
  fun allowedMetroImport(import_: String) {
    assertThatRule { MetroRuntimeDependencyRule() }(
      // language=kotlin
      """
      package yazio.feature

      import $import_

      class MyComponent
      """.trimIndent(),
    ).hasNoLintViolations()
  }

  @Test
  fun errorOnDisallowedMetroImport() {
    assertThatRule { MetroRuntimeDependencyRule() }(
      // language=kotlin
      """
      package yazio.feature

      import dev.zacsweers.metro.DependencyGraph

      class MyComponent
      """.trimIndent(),
    ).hasLintViolationWithoutAutoCorrect(
      line = 3,
      col = 1,
      detail = MetroRuntimeDependencyRule.ERROR_MESSAGE,
    )
  }

  @Test
  fun allowedAndDisallowedImportsTogether() {
    assertThatRule { MetroRuntimeDependencyRule() }(
      // language=kotlin
      """
      package yazio.feature

      import dev.zacsweers.metro.GraphExtension
      import dev.zacsweers.metro.Provides

      class MyComponent
      """.trimIndent(),
    ).hasLintViolationWithoutAutoCorrect(
      line = 4,
      col = 1,
      detail = MetroRuntimeDependencyRule.ERROR_MESSAGE,
    )
  }

  @Test
  fun noErrorAllowedMetroImports() {
    assertThatRule { MetroRuntimeDependencyRule() }(
      // language=kotlin
      """
      package yazio.feature

      import dev.zacsweers.metro.Includes
      import dev.zacsweers.metro.createGraphFactory
      import dev.zacsweers.metro.GraphExtension
      import dev.zacsweers.metro.HasMemberInjections
      import dev.zacsweers.metro.gradle.MetroPluginExtension

      class MyComponent
      """.trimIndent(),
    ).hasNoLintViolations()
  }

  @Test
  fun noErrorWithNonMetroImports() {
    assertThatRule { MetroRuntimeDependencyRule() }(
      // language=kotlin
      """
      package yazio.feature

      import kotlin.collections.List
      import kotlinx.coroutines.flow.Flow
      import javax.inject.Inject

      class MyComponent
      """.trimIndent(),
    ).hasNoLintViolations()
  }
}
