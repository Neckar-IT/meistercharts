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
package com.meistercharts.algorithms.layers

import com.meistercharts.color.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.RectanglePaintable
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import it.neckar.open.unit.si.rad

/**
 * Layer painting an image by rotating and translating it
 */
class PaintableTranslateRotateLayer(
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    @Window val x = chartCalculator.domainRelative2windowX(data.x())
    @Window val y = chartCalculator.domainRelative2windowY(data.y())

    gc.translate(x, y)
    gc.rotateRadians(data.angle())
    data.image().let {
      val boundingBox = it.boundingBox(paintingContext)
      it.paintInBoundingBox(paintingContext, 0.0, 0.0, style.direction, 0.0, 0.0, boundingBox.getWidth(), boundingBox.getHeight())
    }

  }

  class Data(
    /**
     * The image to be painted
     */
    var image: () -> Paintable = { RectanglePaintable(Size.PX_90, Color.bisque) },

    /**
     * The (domain relative) coordinates to paint the image at
     */
    var x: () -> @DomainRelative Double = { 0.0 },

    /**
     * The (domain relative) coordinates to paint the image at
     */
    var y: () -> @DomainRelative Double = { 0.0 },

    /**
     * The rotation angle of the image in radians clockwise
     */
    var angle: () -> @rad Double = { 0.0 }
  )

  @ConfigurationDsl
  class Style {
    /**
     * Direction to paint the paintable relative to the given Coordinates
     */
    var direction: Direction = Direction.Center
  }
}
