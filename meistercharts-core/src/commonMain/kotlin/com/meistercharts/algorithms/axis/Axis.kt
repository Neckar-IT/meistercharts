package com.meistercharts.algorithms.axis

import com.meistercharts.model.Coordinates
import com.meistercharts.model.Distance
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom

/**
 * Identifies the axis
 */
enum class Axis {
  /**
   * The X axis (top --> down)
   */
  X,
  /**
   * The Y axis (left --> right)
   */
  Y;
}

/**
 * Extracts the correct value for the size depending on the given axis
 */
fun Axis.extract(size: Size): Double {
  return when (this) {
    Axis.X -> size.width
    Axis.Y -> size.height
  }
}

/**
 * Extracts the correct value for the coordinates depending on the given axis
 */
fun Axis.extract(coordinates: Coordinates): Double {
  return when (this) {
    Axis.X -> coordinates.x
    Axis.Y -> coordinates.y
  }
}

/**
 * Extracts the correct value for the distance depending on the given axis
 */
fun Axis.extract(distance: Distance): Double {
  return when (this) {
    Axis.X -> distance.x
    Axis.Y -> distance.y
  }
}

/**
 * Extracts the correct value for the zoom depending on the given axis
 */
fun Axis.extract(zoom: Zoom): Double {
  return when (this) {
    Axis.X -> zoom.scaleX
    Axis.Y -> zoom.scaleY
  }
}
