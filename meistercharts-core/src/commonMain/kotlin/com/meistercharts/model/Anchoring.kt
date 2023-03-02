package com.meistercharts.model

import com.meistercharts.annotations.Zoomed
import it.neckar.open.unit.other.px

/**
 * An anchor point (e.g. when painting texts)
 */
data class Anchoring(
  /**
   * The location of the anchoring
   */
  val anchorX: @px Double,
  val anchorY: @px Double,

  /**
   * The gap between anchor and anchored object
   */
  val gapHorizontal: @Zoomed Double = 0.0,
  val gapVertical: @Zoomed Double = gapHorizontal,
  /**
   * The direction to the *anchor*. The anchored object (e.g. a text) is located to the opposite direction
   */
  val anchorDirection: Direction
) {
  constructor(
    anchor: Coordinates,
    gapHorizontal: @Zoomed Double = 0.0,
    gapVertical: @Zoomed Double = gapHorizontal,
    anchorDirection: Direction

  ) : this(anchor.x, anchor.y, gapHorizontal, gapVertical, anchorDirection)

  /**
   * The location of the anchoring
   *
   * ATTENTION: Creates an object when called
   */
  val anchor: Coordinates
    get() {
      return Coordinates(anchorX, anchorY)
    }
}
