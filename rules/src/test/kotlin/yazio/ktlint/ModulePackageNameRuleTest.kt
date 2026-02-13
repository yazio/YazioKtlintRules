package yazio.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ModulePackageNameRuleTest {
  private val assertThatRule = assertThatRule { ModulePackageNameRule() }

  @ParameterizedTest
  @MethodSource("validModulePaths")
  fun validModulePackageNamesHaveNoViolations(
    path: String,
    packageName: String,
  ) {
    assertThatRule(
      // language=kotlin
      """
      package $packageName

      class Foo
      """.trimIndent(),
    ).asFileWithPath(path).hasNoLintViolations()
  }

  @ParameterizedTest
  @MethodSource("wrongPackagePrefixes")
  fun wrongPackagePrefixShowsViolation(
    path: String,
    packageName: String,
    expectedPrefix: String,
  ) {
    assertThatRule(
      // language=kotlin
      """
      package $packageName

      class Foo
      """.trimIndent(),
    ).asFileWithPath(path)
      .hasLintViolationWithoutAutoCorrect(1, 1, ModulePackageNameRule.wrongPackageMessage(expectedPrefix))
  }

  @ParameterizedTest
  @MethodSource("reservedFeatureNames")
  fun reservedFeatureNamesAreRejected(
    path: String,
    packageName: String,
    featureName: String,
    expectedPrefix: String,
  ) {
    assertThatRule(
      // language=kotlin
      """
      package $packageName

      class Foo
      """.trimIndent(),
    ).asFileWithPath(path)
      .hasLintViolationWithoutAutoCorrect(
        1,
        1,
        ModulePackageNameRule.reservedFeatureNameMessage(featureName, expectedPrefix),
      )
  }

  @Test
  fun missingPackageDirectiveShowsViolation() {
    val path = "feature/login/api/src/main/kotlin/yazio/login/api/Foo.kt"
    assertThatRule(
      // language=kotlin
      """
      class Foo
      """.trimIndent(),
    ).asFileWithPath(path)
      .hasLintViolationWithoutAutoCorrect(1, 1, ModulePackageNameRule.missingPackageMessage("yazio.login.api"))
  }

  @Test
  fun nonMatchingPathsWithoutSrcAreIgnored() {
    val path = "feature/login/api/test/kotlin/yazio/login/api/Foo.kt"
    assertThatRule(
      // language=kotlin
      """
      package yazio.login.api

      class Foo
      """.trimIndent(),
    ).asFileWithPath(path).hasNoLintViolations()
  }

  @Test
  fun kotlinScriptFilesAreIgnored() {
    val path = "feature/login/api/src/main/kotlin/yazio/login/api/Foo.kts"
    assertThatRule(
      // language=kotlin
      """
      package yazio.feature.login.api

      class Foo
      """.trimIndent(),
    ).asFileWithPath(path).asKotlinScript().hasNoLintViolations()
  }

  companion object {
    @JvmStatic
    fun validModulePaths(): List<Arguments> =
      listOf(
        Arguments.of(
          "feature/login/api/src/main/kotlin/yazio/login/api/Foo.kt",
          "yazio.login.api",
        ),
        Arguments.of(
          "feature/login/implementation/src/main/kotlin/yazio/login/implementation/Foo.kt",
          "yazio.login.implementation",
        ),
        Arguments.of(
          "feature/login/presenter/src/main/kotlin/yazio/login/presenter/Foo.kt",
          "yazio.login.presenter",
        ),
        Arguments.of(
          "feature/login/ui/src/main/kotlin/yazio/login/ui/Foo.kt",
          "yazio.login.ui",
        ),
        Arguments.of(
          "feature/login/testing/src/main/kotlin/yazio/login/testing/Foo.kt",
          "yazio.login.testing",
        ),
        Arguments.of(
          "reusable/design/api/src/main/kotlin/yazio/design/api/Foo.kt",
          "yazio.design.api",
        ),
        Arguments.of(
          "reusable/design/implementation/src/main/kotlin/yazio/design/implementation/Foo.kt",
          "yazio.design.implementation",
        ),
        Arguments.of(
          "reusable/design/presenter/src/main/kotlin/yazio/design/presenter/Foo.kt",
          "yazio.design.presenter",
        ),
        Arguments.of(
          "reusable/design/ui/src/main/kotlin/yazio/design/ui/Foo.kt",
          "yazio.design.ui",
        ),
        Arguments.of(
          "reusable/design/testing/src/main/kotlin/yazio/design/testing/Foo.kt",
          "yazio.design.testing",
        ),
        Arguments.of(
          "core/network/api/src/main/kotlin/yazio/network/api/Foo.kt",
          "yazio.network.api",
        ),
        Arguments.of(
          "core/network/implementation/src/main/kotlin/yazio/network/implementation/Foo.kt",
          "yazio.network.implementation",
        ),
        Arguments.of(
          "core/network/testing/src/main/kotlin/yazio/network/testing/Foo.kt",
          "yazio.network.testing",
        ),
        Arguments.of(
          "legacy/network/testing/src/main/kotlin/yazio/network/testing/Foo.kt",
          "yazio.foo",
        ),
        Arguments.of(
          "android/network/testing/src/main/kotlin/yazio/network/testing/Foo.kt",
          "yazio.foo",
        ),
      )

    @JvmStatic
    fun wrongPackagePrefixes(): List<Arguments> =
      listOf(
        Arguments.of(
          "feature/login/api/src/main/kotlin/yazio/login/api/Foo.kt",
          "yazio.feature.login.api",
          "yazio.login.api",
        ),
        Arguments.of(
          "reusable/design/ui/src/main/kotlin/yazio/design/ui/Foo.kt",
          "yazio.reusable.design.ui",
          "yazio.design.ui",
        ),
        Arguments.of(
          "core/network/testing/src/main/kotlin/yazio/network/testing/Foo.kt",
          "yazio.core.network.testing",
          "yazio.network.testing",
        ),
      )

    @JvmStatic
    fun reservedFeatureNames(): List<Arguments> =
      listOf(
        Arguments.of(
          "feature/core/api/src/main/kotlin/yazio/core/api/Foo.kt",
          "yazio.core.api",
          "core",
          "yazio.core.api",
        ),
        Arguments.of(
          "reusable/feature/ui/src/main/kotlin/yazio/feature/ui/Foo.kt",
          "yazio.feature.ui",
          "feature",
          "yazio.feature.ui",
        ),
        Arguments.of(
          "core/reusable/api/src/main/kotlin/yazio/reusable/api/Foo.kt",
          "yazio.reusable.api",
          "reusable",
          "yazio.reusable.api",
        ),
      )
  }
}
