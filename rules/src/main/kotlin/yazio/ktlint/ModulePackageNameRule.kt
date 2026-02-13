package yazio.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPackageDirective

class ModulePackageNameRule :
  Rule(
    ruleId = RuleId("yazio:module-package-name"),
    about = aboutYazio,
  ),
  RuleAutocorrectApproveHandler {
  override fun beforeVisitChildNodes(
    node: ASTNode,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
  ) {
    if (node.elementType != FILE) return

    val ktFile = node.psi as? KtFile ?: return
    if (ktFile.isScript()) return
    val filePath = ktFile.virtualFilePath.takeUnless { it.isBlank() } ?: ktFile.name
    if (filePath.isBlank()) return

    when (val contextResult = findModuleContext(filePath)) {
      is ModuleContextResult.None -> return
      is ModuleContextResult.InvalidLayer -> return
      is ModuleContextResult.Match -> {
        val context = contextResult.context

        val packageDirectiveNode = node.findChildByType(PACKAGE_DIRECTIVE)
        if (packageDirectiveNode == null) {
          emit(
            0,
            missingPackageMessage(context.expectedPrefix),
            false,
          )
          return
        }

        val packageName =
          (packageDirectiveNode.psi as? KtPackageDirective)?.fqName?.asString()
            ?: packageDirectiveNode.text.removePrefix("package ").trim()
        if (packageName.isBlank()) {
          emit(
            packageDirectiveNode.startOffset,
            missingPackageMessage(context.expectedPrefix),
            false,
          )
          return
        }

        if (context.featureName in RESERVED_FEATURE_NAMES) {
          emit(
            packageDirectiveNode.startOffset,
            reservedFeatureNameMessage(context.featureName, context.expectedPrefix),
            false,
          )
          return
        }

        if (!packageNameIsValid(packageName, context.expectedPrefix)) {
          emit(
            packageDirectiveNode.startOffset,
            wrongPackageMessage(context.expectedPrefix),
            false,
          )
        }
      }
    }
  }

  private fun packageNameIsValid(
    packageName: String,
    expectedPrefix: String,
  ): Boolean {
    return packageName == expectedPrefix || packageName.startsWith("$expectedPrefix.")
  }

  private fun findModuleContext(filePath: String): ModuleContextResult {
    val segments = filePath.split('/', '\\').filter { it.isNotBlank() }
    if (segments.size < 4) return ModuleContextResult.None

    for (index in 0..segments.size - 4) {
      val moduleType = segments[index]
      if (moduleType !in MODULE_TYPES) continue

      val featureName = segments[index + 1]
      val layer = segments[index + 2]
      val srcMarker = segments[index + 3]

      if (srcMarker != "src") continue

      val allowedLayers = ALLOWED_LAYERS_BY_MODULE[moduleType] ?: continue
      if (layer !in allowedLayers) {
        return ModuleContextResult.InvalidLayer(moduleType, layer, allowedLayers)
      }

      return ModuleContextResult.Match(ModuleContext(moduleType, featureName, layer))
    }

    return ModuleContextResult.None
  }

  private data class ModuleContext(
    val moduleType: String,
    val featureName: String,
    val layer: String,
  ) {
    val expectedPrefix: String = "yazio.$featureName.$layer"
  }

  private sealed class ModuleContextResult {
    data object None : ModuleContextResult()

    data class Match(val context: ModuleContext) : ModuleContextResult()

    data class InvalidLayer(
      val moduleType: String,
      val layer: String,
      val allowedLayers: Set<String>,
    ) : ModuleContextResult()
  }

  internal companion object {
    private val MODULE_TYPES = setOf("feature", "reusable", "core")
    private val RESERVED_FEATURE_NAMES = setOf("core", "feature", "reusable")

    private val ALLOWED_LAYERS_BY_MODULE =
      mapOf(
        "feature" to setOf("api", "implementation", "presenter", "ui", "testing"),
        "reusable" to setOf("api", "implementation", "presenter", "ui", "testing"),
        "core" to setOf("api", "implementation", "testing"),
      )

    internal fun wrongPackageMessage(expectedPrefix: String): String = "Package name should start with \"$expectedPrefix\""

    internal fun reservedFeatureNameMessage(
      featureName: String,
      expectedPrefix: String,
    ): String = "Feature name \"$featureName\" is reserved; use an actual feature name (expected package prefix \"$expectedPrefix\")"

    internal fun missingPackageMessage(expectedPrefix: String): String =
      "Missing package declaration. Expected package to start with \"$expectedPrefix\""

    internal fun invalidLayerMessage(
      layer: String,
      moduleType: String,
      allowedLayers: Set<String>,
    ): String = "Layer \"$layer\" is not allowed for module \"$moduleType\". Allowed layers: ${allowedLayers.sorted().joinToString(", ")}"
  }
}
