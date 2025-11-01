package it.neckar.open.test.utils

import it.neckar.runtime.context.RuntimeContext
import it.neckar.runtime.context.guessInCIEnvironment
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.AnnotationUtils

/**
 * Disables tests that are annotated with [SkipInCi] when the runtime indicates a CI environment.
 */
class DisableIfInCiCondition : ExecutionCondition {
  override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
    val skipInCiAnnotationOptional = AnnotationUtils.findAnnotation(context.element, SkipInCi::class.java)

    if (skipInCiAnnotationOptional.isPresent.not()) return EnabledByDefault

    val skipInCiAnnotation = skipInCiAnnotationOptional.get()

    return if (RuntimeContext.inCI || guessInCIEnvironment()) {
      ConditionEvaluationResult.disabled(skipInCiAnnotation.reason)
    } else {
      ConditionEvaluationResult.enabled(EnabledOutsideCi)
    }
  }

  companion object {
    private val EnabledByDefault: ConditionEvaluationResult = ConditionEvaluationResult.enabled("@SkipInCi is not present")
    private const val EnabledOutsideCi: String = "Enabled because not running inside CI"
  }
}
