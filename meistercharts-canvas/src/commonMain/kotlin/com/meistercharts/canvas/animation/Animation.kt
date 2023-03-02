package com.meistercharts.canvas.animation

import it.neckar.open.unit.si.ms

/**
 * Represents an actual animation
 */
interface Animation {
  /**
   * The time when the animation has been started / will be started
   */
  val startTime: @ms Double

  /**
   * Returns true if the animation has been finished at the given point in time.
   */
  fun isFinished(timestamp: @ms Double): Boolean

  /**
   * Returns the elapsed time since start.
   */
  fun elapsedTime(timestamp: @ms Double): @ms Double = timestamp - startTime
}
