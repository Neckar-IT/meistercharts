/*
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
package com.meistercharts.geometry


import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.geometry.Rectangle
import it.neckar.open.annotations.Hot
import it.neckar.open.unit.other.pct


/**
 * Returns the base point from a bounding box
 */
fun interface BasePointProvider {
  /**
   * Returns the coordinates for a given bounding box
   */
  @Hot
  fun calculateBasePoint(boundingBox: Rectangle): Coordinates
}

/**
 * Base point provider that uses a direction
 */
class DirectionBasedBasePointProvider(
  /**
   * The translation that is added to calculate the base point
   */
  val translation: Distance = Distance.zero,

  /**
   * The direction that describes the side/corner of the bounding box
   */
  val direction: () -> Direction,
) : BasePointProvider {

  constructor(
    direction: Direction,
    translation: Distance = Distance.zero,
  ) : this(
    translation = translation,
    direction = { direction }
  )

  @Hot
  override fun calculateBasePoint(boundingBox: Rectangle): Coordinates {
    return boundingBox.findCoordinates(direction()).plus(translation)
  }
}

/**
 * Calculates the base point using percentages
 */
class RelativeBasePointProvider(
  val xPercentage: @pct Double,
  val yPercentage: @pct Double,
) : BasePointProvider {
  @Hot
  override fun calculateBasePoint(boundingBox: Rectangle): Coordinates {
    return boundingBox.findCoordinatesRelative(xPercentage, yPercentage)
  }
}
