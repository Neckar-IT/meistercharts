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
package com.meistercharts.algorithms.layers.linechart

/**
 * A model for a line chart
 *
 */
interface LinesChartModel {
  /**
   * The number of lines
   */
  val linesCount: Int

  /**
   * Returns the number of points for the given line
   *
   * @param lineIndex must be within 0..[linesCount] -1
   */
  fun pointsCount(lineIndex: Int): Int

  /**
   * The x-value of the line [lineIndex] at position [index]
   *
   * @param lineIndex must be within 0..[linesCount] -1
   */
  fun valueX(lineIndex: Int, index: Int): Double

  /**
   * The Y-value of the line [lineIndex] at position [index]
   *
   * @param lineIndex must be within 0..[linesCount] -1
   */
  fun valueY(lineIndex: Int, index: Int): Double
}

/**
 * Delegates to another model using the given provider
 */
class DelegatingLinesChartModel(
  val delegateProvider: () -> LinesChartModel
) : LinesChartModel {
  override val linesCount: Int
    get() = delegateProvider().linesCount

  override fun pointsCount(lineIndex: Int): Int {
    return delegateProvider().pointsCount(lineIndex)
  }

  override fun valueX(lineIndex: Int, index: Int): Double {
    return delegateProvider().valueX(lineIndex, index)
  }

  override fun valueY(lineIndex: Int, index: Int): Double {
    return delegateProvider().valueY(lineIndex, index)
  }
}

