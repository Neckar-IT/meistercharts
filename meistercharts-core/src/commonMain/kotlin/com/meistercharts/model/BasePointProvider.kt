package com.meistercharts.model

import it.neckar.open.unit.other.pct


/**
 * Returns the base point from a bounding box
 */
fun interface BasePointProvider {
  /**
   * Returns the coordinates for a given bounding box
   */
  fun calculateBasePoint(boundingBox: Rectangle): Coordinates

  ///**
  // * Returns the coordinates for a given bounding box
  // */
  //fun calculateBasePoint(
  //  x: Double,
  //  y: Double,
  //  width: @MayBeNegative Double,
  //  height: @MayBeNegative Double
  //): Coordinates
}

/**
 * Base point provider that uses a direction
 */
class DirectionBasedBasePointProvider(
  /**
   * The direction that describes the side/corner of the bounding box
   */
  val direction: Direction,
  /**
   * The translation that is added to calculate the base point
   */
  val translation: Distance = Distance.zero
) : BasePointProvider {
  override fun calculateBasePoint(boundingBox: Rectangle): Coordinates {
    return boundingBox.findCoordinates(direction).plus(translation)
  }
}

/**
 * Calculates the base point using percentages
 */
class RelativeBasePointProvider(
  val xPercentage: @pct Double,
  val yPercentage: @pct Double
) : BasePointProvider {
  override fun calculateBasePoint(boundingBox: Rectangle): Coordinates {
    return boundingBox.findCoordinatesRelative(xPercentage, yPercentage)
  }
}
