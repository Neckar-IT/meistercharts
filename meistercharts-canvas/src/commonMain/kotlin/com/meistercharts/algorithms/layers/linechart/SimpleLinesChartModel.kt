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
 * A simple line chart model
 */
class SimpleLinesChartModel(
  val lines: List<LineModel>
) : LinesChartModel {
  override val linesCount: Int
    get() = lines.size

  private fun lineModel(lineIndex: Int) = lines[lineIndex]

  override fun pointsCount(lineIndex: Int): Int {
    return lineModel(lineIndex).pointsCount()
  }

  override fun valueX(lineIndex: Int, index: Int): Double {
    return lineModel(lineIndex).valueX(index)
  }

  override fun valueY(lineIndex: Int, index: Int): Double {
    return lineModel(lineIndex).valueY(index)
  }
}

/**
 * Contains the data points for one line
 */
interface LineModel {
  /**
   * Returns the point count for the line
   */
  fun pointsCount(): Int

  /**
   * Returns the x value for the given index
   */
  fun valueX(index: Int): Double

  /**
   * Returns the y value for the given index
   */
  fun valueY(index: Int): Double
}

/**
 * Holds the coordinates as arrays. Immutable.
 */
class SimpleLineModel(
  val xValues: DoubleArray,
  val yValues: DoubleArray
) : LineModel {

  init {
    require(xValues.size == yValues.size) {
      "X and y array must be of same size but was <${xValues.size}> / <${yValues.size}> "
    }
  }

  override fun pointsCount(): Int {
    return xValues.size
  }

  override fun valueX(index: Int): Double {
    return xValues[index]
  }

  override fun valueY(index: Int): Double {
    return yValues[index]
  }
}

/**
 * Holds the coordinates as a list. Mutable.
 */
class MutableLineModel(
) : LineModel {
  private val xValues: MutableList<Double> = mutableListOf()
  private val yValues: MutableList<Double> = mutableListOf()

  override fun pointsCount(): Int {
    return xValues.size
  }

  override fun valueX(index: Int): Double {
    return xValues[index]
  }

  override fun valueY(index: Int): Double {
    return yValues[index]
  }

  fun add(x: Double, y: Double) {
    xValues.add(x)
    yValues.add(y)
  }
}


