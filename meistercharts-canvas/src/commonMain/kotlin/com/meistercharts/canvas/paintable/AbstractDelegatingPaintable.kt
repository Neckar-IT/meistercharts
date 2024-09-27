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
import com.meistercharts.model.Anchoring
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size

/**
 * Abstract base class for delegating paintables that delegate *all* required paint related methods.
 */
abstract class AbstractDelegatingPaintable : Paintable {
  /**
   * The delegate paintable
   */
  abstract val delegate: Paintable


  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return delegate.boundingBox(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    delegate.paint(paintingContext, x, y)
  }

  override fun paint(paintingContext: LayerPaintingContext, location: Coordinates) {
    delegate.paint(paintingContext, location)
  }

  override fun paintInBoundingBox(paintingContext: LayerPaintingContext, anchoring: Anchoring, boundingBoxSize: Size, objectFit: ObjectFit) {
    delegate.paintInBoundingBox(paintingContext, anchoring, boundingBoxSize, objectFit)
  }

  override fun paintInBoundingBox(paintingContext: LayerPaintingContext, location: Coordinates, direction: Direction, boundingBoxSize: Size, objectFit: ObjectFit) {
    delegate.paintInBoundingBox(paintingContext, location, direction, boundingBoxSize, objectFit)
  }

  override fun paintInBoundingBox(
    paintingContext: LayerPaintingContext, x: Double, y: Double, anchorDirection: Direction,
    gapHorizontal: Double, gapVertical: Double,
    boundingBoxSize: Size, objectFit: ObjectFit,
  ) {
    delegate.paintInBoundingBox(paintingContext, x, y, anchorDirection, gapHorizontal, gapVertical, boundingBoxSize, objectFit)
  }

  override fun paintInBoundingBox(
    paintingContext: LayerPaintingContext, x: Double, y: Double, anchorDirection: Direction,
    gapHorizontal: Double, gapVertical: Double, width: Double, height: Double, objectFit: ObjectFit,
  ) {
    delegate.paintInBoundingBox(paintingContext, x, y, anchorDirection, gapHorizontal, gapVertical, width, height, objectFit)
  }

  override fun paintSizeForced(paintingContext: LayerPaintingContext, x: Double, y: Double, forcedSize: Size) {
    delegate.paintSizeForced(paintingContext, x, y, forcedSize)
  }
}
