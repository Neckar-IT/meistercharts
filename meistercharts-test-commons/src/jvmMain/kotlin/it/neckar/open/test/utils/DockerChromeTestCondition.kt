package it.neckar.open.test.utils

import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.AnnotationUtils

/**
 * JUnit 5 execution condition that enables tests only when Docker Chrome testing is explicitly enabled.
 *
 * Tests are enabled when:
 * - Environment variable `CHROME_TESTS_ENABLED=true` is set, OR
 * - System property `chromeTests` is present (set via `-PchromeTests` in Gradle)
 */
class DockerChromeTestCondition : ExecutionCondition {
  override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
    val annotationOptional = AnnotationUtils.findAnnotation(context.element, DockerChromeTest::class.java)

    if (annotationOptional.isPresent.not()) return EnabledByDefault

    val annotation = annotationOptional.get()

    val envEnabled = System.getenv("CHROME_TESTS_ENABLED") == "true"
    val propertyEnabled = System.getProperty("chromeTests") != null

    return if (envEnabled || propertyEnabled) {
      ConditionEvaluationResult.enabled(EnabledMessage)
    } else {
      ConditionEvaluationResult.disabled(annotation.reason)
    }
  }

  companion object {
    private val EnabledByDefault: ConditionEvaluationResult = ConditionEvaluationResult.enabled("@DockerChromeTest is not present")
    private const val EnabledMessage: String = "Docker Chrome tests enabled via environment variable or system property"
  }
}
