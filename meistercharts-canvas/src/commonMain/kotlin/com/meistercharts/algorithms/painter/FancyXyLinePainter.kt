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
package com.meistercharts.algorithms.painter

import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.color.Color
import com.meistercharts.geometry.Coordinates
import it.neckar.open.unit.other.px

/**
 * Paints a xy line with optional shadow
 *
 */
open class FancyXyLinePainter(
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractPainter(snapXValues, snapYValues), XYPainter {

  /**
   * The path
   */
  protected val path: MutableList<Coordinates> = mutableListOf()

  /**
   * The color of the line
   */
  var color: Color = Color.black

  /**
   * The width
   */
  @px
  var width: Double = 1.0


  /**
   * The (optional) shadow color.
   * If set to null no shadow is painted
   */
  var shadowColor: Color? = null

  /**
   * The offset of the shadow
   */
  @px
  var shadowOffset: Double = 2.0

  override fun addCoordinate(gc: CanvasRenderingContext, @px @Window x: Double, @px @Window y: Double) {
    path.add(Coordinates(x, y))
  }

  override fun finish(gc: CanvasRenderingContext) {
    if (path.size < 2) {
      return
    }

    //Paint the shadow if there is one
    shadowColor?.let {
      gc.strokeStyle(it)
      gc.translate(shadowOffset, shadowOffset)

      gc.beginPath()
      path.forEach { coordinates ->
        gc.lineTo(coordinates)
      }

      gc.lineWidth = width
      gc.stroke()
      gc.translate(-shadowOffset, -shadowOffset)
    }


    //Paint the path
    gc.beginPath()
    path.forEach { coordinates ->
      gc.lineTo(coordinates)
    }

    gc.strokeStyle(color)
    gc.stroke()
  }
}
