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
    val onlyLinuxAnnotation = AnnotationUtils.findAnnotation(context.element, OnlyLinux::class.java)
    val skipWindowsAnnotation = AnnotationUtils.findAnnotation(context.element, SkipWindows::class.java)


    val onlyLinux = onlyLinuxAnnotation.isPresent
    val skipWindows = skipWindowsAnnotation.isPresent

    if (onlyLinux) {
      return if (OS.LINUX.isCurrentOs) {
        EnabledOnLinux
      } else {
        DisabledNonLinux
      }
    }

    if (skipWindows) {
      return if (OS.WINDOWS.isCurrentOs) {
        DisabledOnWindows
      } else {
        EnabledNotWindows
      }
    }

    return EnabledByDefault
  }

  companion object {
    private val EnabledByDefault: ConditionEvaluationResult = ConditionEvaluationResult.enabled("@OnlyLinux is not present")

    @JvmField
    val DisabledNonLinux: ConditionEvaluationResult = ConditionEvaluationResult.disabled("Disabled because running on other OS than Linux")

    @JvmField
    val EnabledOnLinux: ConditionEvaluationResult = ConditionEvaluationResult.enabled("Enabled - running on Linux")

    @JvmField
    val DisabledOnWindows: ConditionEvaluationResult = ConditionEvaluationResult.disabled("Disabled because running on Windows")

    @JvmField
    val EnabledNotWindows: ConditionEvaluationResult = ConditionEvaluationResult.enabled("Enabled - running not on Windows")
  }
}
