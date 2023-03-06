/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.canvas

import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.allBlank
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.fastForEach
import it.neckar.open.provider.isNotEmpty
import it.neckar.open.unit.other.px

/**
 * Supports calculations of (multi line) texts
 */
object TextLineCalculations {
  /**
   * Contains the [BlankFallbackText] as list
   */
  val BlankFallbackLine: List<String> = listOf(BlankFallbackText)


  /**
   * Returns a list that contains at least one line that is not blank
   */
  fun avoidAllBlankLines(lines: List<String>): List<String> {
    if (lines.isEmpty()) {
      return BlankFallbackLine
    }

    if (lines.allBlank()) {
      //TODO introduce cache(?)
      List(lines.size) { BlankFallbackText }
    }

    return lines
  }

  /**
   * Returns the height of a text block for the given lines
   */
  fun calculateTextBlockHeight(
    fontMetrics: FontMetrics, linesCount: Int,
    lineSpacing: LineSpacing,
    /**
     * The min line height - this can be used if other factors are relevant for the line height (e.g. images)
     */
    minLineHeight: @px Double = 0.0,
  ): @px Double {
    @px val spaceBetweenLines = fontMetrics.totalHeight * lineSpacing.spacePercentage
    return calculateTextBlockHeight(fontMetrics, linesCount, spaceBetweenLines)
  }

  /**
   * Returns the height of a text block
   */
  fun calculateTextBlockHeight(
    fontMetrics: FontMetrics, linesCount: Int, spaceBetweenLines: @px Double,
    /**
     * The min line height - this can be used if other factors are relevant for the line height (e.g. images)
     */
    minLineHeight: @px Double = 0.0,
  ): @px Double {
    @px val lineHeight = fontMetrics.totalHeight.coerceAtLeast(minLineHeight)
    return linesCount * lineHeight + (linesCount - 1) * spaceBetweenLines
  }

  /**
   * Returns the text width for the lines.
   * Returns at most the [maxStringWidth] (if provided).
   *
   * Throws an exception if no lines are provided
   */
  @Suppress("DuplicatedCode")
  fun calculateMultilineTextWidth(
    renderingContext: CanvasRenderingContext,
    lines: List<String>,
    /**
     * The optional max width that is returned
     */
    maxStringWidth: @px Double = Double.MAX_VALUE,
  ): @px Double {
    require(lines.isNotEmpty()) { "Need at least one line" }

    //the current max width for all lines
    var currentMaxWidth = 0.0

    lines.fastForEach { line ->
      if (currentMaxWidth >= maxStringWidth) {
        //Skip remaining lines, return immediately if max length has been reached
        return maxStringWidth
      }
      currentMaxWidth = currentMaxWidth.coerceAtLeast(renderingContext.calculateTextWidth(line))
    }

    return currentMaxWidth.coerceAtMost(maxStringWidth)
  }

  /**
   * Calculates the multi line text width using a sized provider
   */
  @Suppress("DuplicatedCode")
  fun calculateMultilineTextWidth(
    renderingContext: CanvasRenderingContext,
    lines: SizedProvider<String>,
    /**
     * The optional max width that is returned
     */
    maxStringWidth: @px Double = Double.MAX_VALUE,
  ): @px Double {
    require(lines.isNotEmpty()) { "Need at least one line" }

    //the current max width for all lines
    var currentMaxWidth = 0.0

    lines.fastForEach { line ->
      if (currentMaxWidth >= maxStringWidth) {
        //Skip remaining lines, return immediately if max length has been reached
        return maxStringWidth
      }
      currentMaxWidth = currentMaxWidth.coerceAtLeast(renderingContext.calculateTextWidth(line))
    }

    return currentMaxWidth.coerceAtMost(maxStringWidth)
  }
}
