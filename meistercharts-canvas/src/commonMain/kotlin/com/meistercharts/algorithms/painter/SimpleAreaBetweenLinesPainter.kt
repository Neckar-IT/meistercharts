package com.meistercharts.algorithms.painter

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.painter.AreaBetweenLinesPainter
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.collections.fastForEachIndexedReversed
import it.neckar.open.unit.number.IsFinite

/**
 * An open class for drawing and filling the area between two lines on a canvas.
 */
open class SimpleAreaBetweenLinesPainter(
  snapXValues: Boolean,
  snapYValues: Boolean,
) : AbstractPainter(snapXValues, snapYValues), AreaBetweenLinesPainter {

  private val xLocations = DoubleArrayList(10)
  private val y1Locations = DoubleArrayList(10)
  private val y2Locations = DoubleArrayList(10)

  override fun begin(gc: CanvasRenderingContext) {
    xLocations.clear()
    y1Locations.clear()
    y2Locations.clear()
  }

  override fun addCoordinates(gc: CanvasRenderingContext, x: @Zoomed @IsFinite Double, y1: @Zoomed @IsFinite Double, y2: @Zoomed @IsFinite Double) {
    require(x.isFinite()) { "x must be a finite number but was $x" }
    require(y1.isFinite()) { "y1 must be a finite number but was $y1" }
    require(y2.isFinite()) { "y2 must be a finite number but was $y2" }

    xLocations.add(x)
    y1Locations.add(y1)
    y2Locations.add(y2)
  }

  override fun paint(gc: CanvasRenderingContext, strokeLines: Boolean) {
    if (xLocations.size < 2 || y1Locations.size < 2 || y2Locations.size < 2) return

    // Draw and fill the area between the lines
    gc.beginPath()

    // Draw line1
    gc.moveTo(xLocations[0], y1Locations[0])
    xLocations.fastForEachIndexed { index, x ->
      if (index > 0) {
        gc.lineTo(x, y1Locations[index])
      }
    }

    // Draw line2 in reverse
    xLocations.fastForEachIndexedReversed { index, x ->
      gc.lineTo(x, y2Locations[index])
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
    gc.moveTo(xLocations[0], y1Locations[0])

    xLocations.fastForEachIndexed { index, x ->
      if (index > 0) {
        gc.lineTo(x, y1Locations[index])
      }
    }

    gc.stroke()

    // Draw and stroke line2
    gc.beginPath()
    gc.moveTo(xLocations[0], y2Locations[0])
    xLocations.fastForEachIndexed { index, x ->
      if (index > 0) {
        gc.lineTo(x, y2Locations[index])
      }
    }
    gc.stroke()
  }


  override fun toString(): String {
    return "SimpleAreaBetweenLinesPainter"
  }
}
