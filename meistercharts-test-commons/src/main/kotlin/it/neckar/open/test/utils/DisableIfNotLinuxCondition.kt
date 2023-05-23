package it.neckar.open.test.utils

import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.AnnotationUtils

/**
 * Tests are disabled on non linux OS (windows)
 *
 */
class DisableIfNotLinuxCondition : ExecutionCondition {
  override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
    val optional = AnnotationUtils.findAnnotation(context.element, OnlyLinux::class.java)
    return if (optional.isPresent) {
      if (OS.LINUX.isCurrentOs) {
        ENABLED_LINUX
      } else {
        DISABLED_NON_LINUX
      }
    } else ENABLED_BY_DEFAULT
  }

  companion object {
    private val ENABLED_BY_DEFAULT = ConditionEvaluationResult.enabled("@OnlyLinux is not present")

    @JvmField
    val DISABLED_NON_LINUX: ConditionEvaluationResult = ConditionEvaluationResult.disabled("Disabled because running on other OS than Linux")

    @JvmField
    val ENABLED_LINUX: ConditionEvaluationResult = ConditionEvaluationResult.enabled("Enabled - running on Linux")
  }
}
