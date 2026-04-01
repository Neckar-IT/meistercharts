/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
