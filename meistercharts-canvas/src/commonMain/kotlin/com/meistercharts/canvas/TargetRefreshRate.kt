package com.meistercharts.canvas

import it.neckar.open.unit.other.fps
import it.neckar.open.unit.si.ms

/**
 * The target refresh rate for the canvas support
 */
data class TargetRefreshRate(@fps val refreshRate: Double?) {

  init {
    require(refreshRate == null || refreshRate > 0.0) { "Invalid refresh rate: <$refreshRate>. Must be greater then <0.0>" }
  }

  /**
   * The minimum distance between two refresh events.
   * If a refresh is triggered before the min distance has passed, the refresh will be skipped
   *
   * Returns <null> if refresh rate is not limited
   */
  val distance: @ms Double? = refreshRate?.let { 1000.0 / this.refreshRate }

  companion object {
    /**
     * Returns the refresh rate from an int value
     */
    fun from(targetRefreshRate: Double): TargetRefreshRate {
      require(targetRefreshRate >= 0.0) { "Invalid refresh rate: <$targetRefreshRate>. Must not be smaller than <0.0>" }

      if (targetRefreshRate == 0.0) {
        return unlimited
      }

      return TargetRefreshRate(targetRefreshRate)
    }

    /**
     * Unlimited - usually 60 Hz.
     * Be careful - in JavaFX this may be really unlimited (>200fps)
     */
    val unlimited: TargetRefreshRate = TargetRefreshRate(null)

    val veryFast60: TargetRefreshRate = TargetRefreshRate(60.0)
    val fast30: TargetRefreshRate = TargetRefreshRate(30.0)
    val slow15: TargetRefreshRate = TargetRefreshRate(15.0)
    val verySlow5: TargetRefreshRate = TargetRefreshRate(5.0)
    val halfSecond: TargetRefreshRate = TargetRefreshRate(2.0)
    val onceASecond: TargetRefreshRate = TargetRefreshRate(1.0)
    val everyTwoSeconds: TargetRefreshRate = TargetRefreshRate(1 / 2.0)
    val everyFiveSeconds: TargetRefreshRate = TargetRefreshRate(1 / 5.0)

    /**
     * Contains all predefined target refresh rates
     */
    val predefined: List<TargetRefreshRate> = listOf(unlimited, veryFast60, fast30, slow15, verySlow5, halfSecond, onceASecond, everyTwoSeconds, everyFiveSeconds)
  }
}
