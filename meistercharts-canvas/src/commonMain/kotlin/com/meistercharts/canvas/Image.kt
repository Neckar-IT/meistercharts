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
package com.meistercharts.canvas

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmOverloads

/**
 * Describes an image that can be painted on a graphics context.
 * An image instance is *platform* dependent!
 *
 * ## Platform dependent implementations
 *
 * ### HTML
 *
 * In HTML [data] contains an HTMLImageElement
 *
 * ### JavaFX
 * In JavaFX [data] contains a javafx.scene.image.Image
 */
data class Image @JvmOverloads constructor(
  val data: Any,
  /**
   * The size of the image - may or may *not* be the natural size of the image
   */
  val size: @px Size,

  /**
   * The alignment point of the image.
   * Is useful for images that have a "natural" base - e.g. the tip of an arrow
   */
  val alignmentPoint: Coordinates = Coordinates.origin
) : Paintable {

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle = Rectangle(alignmentPoint, size)

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    gc.paintImage(this, x + alignmentPoint.x, y + alignmentPoint.y, size.width, size.height)
  }

  override fun paintSizeForced(paintingContext: LayerPaintingContext, x: Double, y: Double, forcedSize: Size) {
    val gc = paintingContext.gc
    gc.paintImage(this, x + alignmentPoint.x, y + alignmentPoint.y, forcedSize.width, forcedSize.height)
  }
}
