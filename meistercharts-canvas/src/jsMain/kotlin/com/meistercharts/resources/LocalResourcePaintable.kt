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
package com.meistercharts.resources

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.canvas.paintable.Paintable
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.open.http.Url
import it.neckar.open.unit.other.px

/**
 * Loads a local resource
 */
actual class LocalResourcePaintable actual constructor(
  val relativePath: Url,
  size: @px Size?,

  /**
   * The alignment point for the bounding box
   */
  val alignmentPoint: Coordinates,
) : Paintable {

  /**
   * The paintable that is used
   */
  val delegate: Paintable = if (size != null) UrlPaintable.fixedSize(relativePath, size, alignmentPoint) else UrlPaintable.naturalSize(relativePath, alignmentPoint)

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return delegate.boundingBox(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    delegate.paint(paintingContext, x, y)
  }

  actual fun withSize(size: Size): LocalResourcePaintable {
    return LocalResourcePaintable(relativePath, size, alignmentPoint)
  }

  actual companion object {
  }
}
