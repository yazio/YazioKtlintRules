package yazio.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class MetroGraphExtensionNamingRuleTest {
  private val assertThat = KtLintAssertThat.assertThatRule { MetroGraphExtensionNamingRule() }

  @Test
  fun `valid GraphExtension with all naming conventions met`() {
    assertThat(
      // language=kotlin
      """
      package test

      @GraphExtension(AiSnapItCameraScope::class)
      interface AiSnapItCameraGraph {
        fun inject(target: AiSnapItCameraController)

        @GraphExtension.Factory
        interface Factory {
          fun createAiSnapItCameraGraph(scope: AppScope): AiSnapItCameraGraph
        }
      }
      """.trimIndent(),
    ).hasNoLintViolations()
  }

  @Test
  fun `error when GraphExtension interface does not end with Graph`() {
    assertThat(
      // language=kotlin
      """
      package test

      @GraphExtension(AiSnapItCameraScope::class)
      interface AiSnapItCamera {
        fun inject(target: AiSnapItCameraController)
      }
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
      LintViolation(3, 1, MetroGraphExtensionNamingRule.ERROR_GRAPH_SUFFIX),
    )
  }

  @Test
  fun `error when Factory interface is not named Factory`() {
    assertThat(
      // language=kotlin
      """
      package test

      @GraphExtension(AiSnapItCameraScope::class)
      interface AiSnapItCameraGraph {
        fun inject(target: AiSnapItCameraController)

        @GraphExtension.Factory
        interface Builder {
          fun createAiSnapItCameraGraph(scope: AppScope): AiSnapItCameraGraph
        }
      }
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
      LintViolation(7, 3, MetroGraphExtensionNamingRule.ERROR_FACTORY_NAME),
    )
  }

  @Test
  fun `error when create function does not match createGraphName`() {
    assertThat(
      // language=kotlin
      """
      package test

      @GraphExtension(AiSnapItCameraScope::class)
      interface AiSnapItCameraGraph {
        fun inject(target: AiSnapItCameraController)

        @GraphExtension.Factory
        interface Factory {
          fun create(scope: AppScope): AiSnapItCameraGraph
        }
      }
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
      LintViolation(
        9,
        5,
        "${MetroGraphExtensionNamingRule.ERROR_CREATE_PREFIX}'createAiSnapItCameraGraph' but found 'create'.",
      ),
    )
  }

  @Test
  fun `error when inject function is not named inject`() {
    assertThat(
      // language=kotlin
      """
      package test

      @GraphExtension(AiSnapItCameraScope::class)
      interface AiSnapItCameraGraph {
        fun bind(target: AiSnapItCameraController)
      }
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
      LintViolation(5, 3, MetroGraphExtensionNamingRule.ERROR_INJECT_NAME),
    )
  }

  @Test
  fun `no error for regular interface without GraphExtension annotation`() {
    assertThat(
      // language=kotlin
      """
      package test

      interface SomeRegularInterface {
        fun doSomething(param: String)
      }
      """.trimIndent(),
    ).hasNoLintViolations()
  }

  @Test
  fun `no error for functions without parameters on GraphExtension interface`() {
    assertThat(
      // language=kotlin
      """
      package test

      @GraphExtension(SomeScope::class)
      interface SomeGraph {
        fun providesSomething(): Something
      }
      """.trimIndent(),
    ).hasNoLintViolations()
  }

  @Test
  fun `multiple violations in a single file`() {
    assertThat(
      // language=kotlin
      """
      package test

      @GraphExtension(AiSnapItCameraScope::class)
      interface AiSnapItCamera {
        fun bind(target: AiSnapItCameraController)

        @GraphExtension.Factory
        interface Builder {
          fun create(scope: AppScope): AiSnapItCameraGraph
        }
      }
      """.trimIndent(),
    ).hasLintViolationsWithoutAutoCorrect(
      LintViolation(3, 1, MetroGraphExtensionNamingRule.ERROR_GRAPH_SUFFIX),
      LintViolation(5, 3, MetroGraphExtensionNamingRule.ERROR_INJECT_NAME),
      LintViolation(7, 3, MetroGraphExtensionNamingRule.ERROR_FACTORY_NAME),
      LintViolation(
        9,
        5,
        "${MetroGraphExtensionNamingRule.ERROR_CREATE_PREFIX}'createAiSnapItCamera' but found 'create'.",
      ),
    )
  }
}
