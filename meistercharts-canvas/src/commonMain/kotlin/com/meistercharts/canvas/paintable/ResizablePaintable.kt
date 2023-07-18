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
package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.geometry.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px

/**
 * Represents a paintable that is resizable
 */
@Deprecated("No longer supported")
interface ResizablePaintable : Paintable {
  var size: @px Size
}


/**
 * Abstract base class that automatically updates the bounding box
 */
@Deprecated("No longer supported")
abstract class AbstractResizablePaintable(
  initialSize: @px Size,

  /**
   * Updates the bounding box for a new size.
   * This method is called when the size has been updated
   */
  val calculateBoundingBox: (size: Size) -> @px Rectangle,

  ) : ResizablePaintable {

  override var size: @px Size = initialSize
    set(value) {
      field = value

      boundingBox = calculateBoundingBox(value)
    }

  private var boundingBox: Rectangle = calculateBoundingBox(initialSize)

  final override fun boundingBox(paintingContext: LayerPaintingContext): @px Rectangle {
    return boundingBox
  }
}
