package com.meistercharts.canvas.layout.cache

import it.neckar.open.unit.number.MayBeNegative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Rectangle

/**
 * Caches bounds (x,y,width,height) for one object.
 *
 * Must only be used for layout objects in layers.
 *
 * Is *NOT* thread safe!
 *
 * NOTE: For multiple bounds use [BoundsLayoutCache]
 */
class BoundsCache : LayoutVariablesCache {
  /**
   * The x locations
   */
  var x: @Window Double = 0.0

  /**
   * The y locations
   */
  var y: @Window Double = 0.0

  /**
   * The widths
   */
  var width: @Zoomed @MayBeNegative Double = 0.0

  /**
   * The heights
   */
  var height: @Zoomed @MayBeNegative Double = 0.0

  override fun reset() {
    x = 0.0
    y = 0.0
    width = 0.0
    height = 0.0
  }

  /**
   * Sets the location for the given index
   */
  fun location(x: @Window Double, y: @Window Double) {
    this.x = x
    this.y = y
  }

  /**
   * Sets the size for the given index
   */
  fun size(width: @Zoomed Double, height: @Zoomed Double) {
    this.width = width
    this.height = height
  }

  /**
   * Sets the interpreting the given coordinates as *center* values
   */
  fun centered(
    centerX: @Window Double,
    centerY: @Window Double,
    width: @Zoomed Double,
    height: @Zoomed Double
  ) {
    this.x = centerX - width / 2.0
    this.y = centerY - height / 2.0
    this.width = width
    this.height = height
  }

  /**
   * Returns a (new instance) rectangle representing the bounds for the given index
   *
   * Attention: A new object is instantiated every time this method is called
   */
  fun asRect(): Rectangle {
    return Rectangle(
      x,
      y,
      width,
      height
    )
  }
}
