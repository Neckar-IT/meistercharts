package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.AnnotationUtils
import java.awt.GraphicsEnvironment

/**
 * Disables tests when running headless
 *
 */
class DisableIfHeadlessCondition : ExecutionCondition {
  override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
    val optional = AnnotationUtils.findAnnotation(context.element, DisableIfHeadless::class.java)
    return if (optional.isPresent) {
      if (GraphicsEnvironment.isHeadless()) {
        DISABLED_HEADLESS
      } else ENABLED_NOT_HEADLESS
    } else ENABLED_BY_DEFAULT
  }

  companion object {
    private val ENABLED_BY_DEFAULT = ConditionEvaluationResult.enabled("@DisableWhenHeadless is not present")

    @JvmField
    val DISABLED_HEADLESS: ConditionEvaluationResult = ConditionEvaluationResult.disabled("Disabled because running headless")

    @JvmField
    val ENABLED_NOT_HEADLESS: ConditionEvaluationResult = ConditionEvaluationResult.enabled("Enabled - *not* running headless")
  }
}
