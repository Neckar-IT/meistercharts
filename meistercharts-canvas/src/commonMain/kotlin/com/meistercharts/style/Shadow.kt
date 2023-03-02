package com.meistercharts.style

import com.meistercharts.algorithms.painter.Color
import it.neckar.open.unit.other.px

/**
 * Contains the configuration for a shadow
 */
data class Shadow(
  val color: Color = Color.black,
  val blurRadius: @px Double = 10.0,
  val offsetX: @px Double = 0.0,
  val offsetY: @px Double = 0.0,
) {

  companion object {
    /**
     * Default shadow - without offset
     */
    val Default: Shadow = Shadow()
    val Light: Shadow = Shadow(color = Color.darkgray, blurRadius = 5.0)
    val LightDrop: Shadow = Shadow(color = Color.darkgray, blurRadius = 5.0, offsetX = 1.0, offsetY = 1.0)
  }
}
