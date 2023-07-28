package com.meistercharts.axis

import com.meistercharts.model.Zoom
import it.neckar.geometry.Axis


/**
 * Extracts the correct value for the zoom depending on the given axis
 */
fun Axis.extract(zoom: Zoom): Double {
  return when (this) {
    Axis.X -> zoom.scaleX
    Axis.Y -> zoom.scaleY
  }
}
