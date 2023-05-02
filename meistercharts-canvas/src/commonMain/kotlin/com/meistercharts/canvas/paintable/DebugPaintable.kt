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
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.paintMark
import com.meistercharts.model.Rectangle

/**
 * A paintable that can be used to diagnose paintables
 */
class DebugPaintable : Paintable {
  var strokeColor: Color = Color.orangered
  var fillColor: Color = Color.orange
  var alignmentPointMarkerColor: Color = Color.blue

  var alignmentPointX: Double = -10.0
  var alignmentPointY: Double = -20.0

  var width: Double = 240.0
  var height: Double = 130.0

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle = Rectangle(alignmentPointX, alignmentPointY, width, height)

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    gc.translate(x, y)

    gc.fill(fillColor)
    val boundingBox = boundingBox(paintingContext)

    gc.fillRect(boundingBox)

    gc.stroke(strokeColor)
    gc.strokeRect(boundingBox)

    gc.stroke(alignmentPointMarkerColor)
    gc.strokeLine(boundingBox.topLeft(), boundingBox.bottomRight())
    gc.strokeLine(boundingBox.bottomLeft(), boundingBox.topRight())

    gc.paintMark(0.0, 0.0, color = alignmentPointMarkerColor)
  }
}
