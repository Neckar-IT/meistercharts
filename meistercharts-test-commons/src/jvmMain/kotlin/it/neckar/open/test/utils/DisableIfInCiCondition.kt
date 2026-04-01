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
