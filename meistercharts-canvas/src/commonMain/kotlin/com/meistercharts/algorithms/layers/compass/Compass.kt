package com.meistercharts.algorithms.layers.compass

import com.meistercharts.annotations.Domain
import it.neckar.open.unit.other.deg

/**
 * The type of ticks encountered in a compass
 */
enum class CompassTickType {
  /**
   * Large tick distance (usually every 45°)
   */
  Major,

  /**
   * Medium tick distance (e.g. every 15°)
   */
  Medium,

  /**
   * Medium tick distance (usually every 3°)
   */
  Minor;
}

/**
 * Tick provider for a compass
 */
fun interface CompassTicksProvider {
  /**
   * Returns the minor ticks - that are only painted as
   */
  fun ticks(gaugePaintable: GaugePaintable, tickType: CompassTickType): @Domain List<Double>
}

class DefaultCompassTicksProvider : CompassTicksProvider {
  val minorTicks: List<Double> = (0 until 360 step 3)
    .filter { it % 15 != 0 }
    .map { it.toDouble() }

  val mediumTicks: List<Double> = (0 until 360 step 15)
    .filter { it % 45 != 0 }
    .map { it.toDouble() }

  val largeTicks: List<Double> = (0 until 360 step 45)
    .map { it.toDouble() }

  override fun ticks(gaugePaintable: GaugePaintable, tickType: CompassTickType): List<Double> {
    return when (tickType) {
      CompassTickType.Major  -> largeTicks
      CompassTickType.Medium -> mediumTicks
      CompassTickType.Minor  -> minorTicks
    }
  }
}


/**
 * Formats degrees as direction. Returns null if the angle is not one of the 8 predefined directions
 */
fun formatDegreesAsDirection(tickAngleDegree: @deg Double): String? {
  return when (tickAngleDegree) {
    0.0   -> "N"
    45.0  -> "NE"
    90.0  -> "E"
    135.0 -> "SE"
    180.0 -> "S"
    225.0 -> "SW"
    270.0 -> "W"
    315.0 -> "NW"
    else  -> null
  }
}

