/**
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
package com.meistercharts.canvas.layout.cache

import it.neckar.open.unit.number.MayBeNegative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.Rectangle

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
