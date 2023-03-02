package com.meistercharts.algorithms.painter

import com.meistercharts.annotations.Window
import it.neckar.open.unit.other.px

/**
 * Base class for a painter
 */
interface Painter {
  /**
   * If set to true the x values should be snapped to full pixel values
   */
  val isSnapXValues: Boolean

  /**
   * If set to true the y values should be snapped to full pixel values
   */
  val isSnapYValues: Boolean

  /**
   * Snaps a position on the x axis if [isSnapXValues] is set to true.
   * Uses round.
   */
  @px
  @Window
  fun snapXPosition(@px @Window xValue: Double): Double

  /**
   * Snaps a width on the x axis if [isSnapXValues] is set to true.
   * Uses ceil.
   */
  @px
  @Window
  fun snapWidth(@px @Window xValue: Double): Double

  /**
   * Snaps a position on the x axis if [isSnapYValues] is set to true.
   * Uses round.
   */
  @px
  @Window
  fun snapYPosition(@px @Window yValue: Double): Double

  /**
   * Snaps a height on the x axis if [isSnapXValues] is set to true.
   * Uses ceil.
   */
  @px
  @Window
  fun snapHeight(@px @Window yValue: Double): Double
}
