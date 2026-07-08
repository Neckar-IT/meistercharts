/*
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

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.layout.buffer.AreaBandArrayList
import com.meistercharts.painter.AreaBetweenLinesPainter
import it.neckar.open.unit.number.IsFinite

/**
 * An open class for drawing and filling the area between two lines on a canvas.
 */
open class SimpleAreaBetweenLinesPainter(
  snapXValues: Boolean,
  snapYValues: Boolean,
) : AbstractPainter(snapXValues, snapYValues), AreaBetweenLinesPainter {

  private val bandPoints = AreaBandArrayList(10)

  override fun begin(gc: CanvasRenderingContext) {
    bandPoints.clear()
  }

  override fun addCoordinates(gc: CanvasRenderingContext, x: @Zoomed @IsFinite Double, y1: @Zoomed @IsFinite Double, y2: @Zoomed @IsFinite Double) {
    require(x.isFinite()) { "x must be a finite number but was $x" }
    require(y1.isFinite()) { "y1 must be a finite number but was $y1" }
    require(y2.isFinite()) { "y2 must be a finite number but was $y2" }

    bandPoints.add(x, y1, y2)
  }

  override fun paint(gc: CanvasRenderingContext, strokeLines: Boolean) {
    if (bandPoints.size < 2) return

    // Draw and fill the area between the lines
    gc.beginPath()

    // Draw line1
    gc.moveTo(bandPoints.xAt(0), bandPoints.y1At(0))
    bandPoints.fastForEachIndexed { index, x, y1, _ ->
      if (index > 0) {
        gc.lineTo(x, y1)
      }
    }

    // Draw line2 in reverse
    bandPoints.fastForEachIndexedReversed { _, x, _, y2 ->
      gc.lineTo(x, y2)
    }

    // Close the path
    gc.closePath()

    // Fill the area between the lines
    gc.fill()

    //Stroke the lines
    if (strokeLines) {
      strokeLines(gc)
    }
  }

  private fun strokeLines(gc: CanvasRenderingContext) {
    // Draw and stroke line1
    gc.beginPath()
    gc.moveTo(bandPoints.xAt(0), bandPoints.y1At(0))

    bandPoints.fastForEachIndexed { index, x, y1, _ ->
      if (index > 0) {
        gc.lineTo(x, y1)
      }
    }

    gc.stroke()

    // Draw and stroke line2
    gc.beginPath()
    gc.moveTo(bandPoints.xAt(0), bandPoints.y2At(0))
    bandPoints.fastForEachIndexed { index, x, _, y2 ->
      if (index > 0) {
        gc.lineTo(x, y2)
      }
    }
    gc.stroke()
  }


  override fun toString(): String {
    return "SimpleAreaBetweenLinesPainter"
  }
}
