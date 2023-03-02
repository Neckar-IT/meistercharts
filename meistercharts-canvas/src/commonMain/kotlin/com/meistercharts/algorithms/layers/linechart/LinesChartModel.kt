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

