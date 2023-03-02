package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.ChartState
import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.withCurrentChartState
import com.meistercharts.algorithms.withAxisOrientation
import com.meistercharts.algorithms.withZoom
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Zoom

/**
 * A paintable that has a modified chart support
 */
class PaintableWithModifiedChartSupport(
  val delegate: Paintable,
  /**
   * The chart state provider that will be used to create the used chart state
   */
  val chartStateProvider: ChartState.() -> ChartState
) : Paintable {

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    val chartSupport = paintingContext.chartSupport
    return chartSupport.withCurrentChartState(chartStateProvider) {
      delegate.boundingBox(paintingContext)
    }
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    paintingContext.withCurrentChartState(chartStateProvider) {
      delegate.paint(paintingContext, x, y)
    }
  }
}

/**
 * Returns a paintable that paints this paintable flipped on the selected axis
 */
fun Paintable.withAxisOrientation(
  axisOrientationXOverride: AxisOrientationX?,
  axisOrientationYOverride: AxisOrientationY?
): Paintable {
  if (axisOrientationXOverride == null && axisOrientationYOverride == null) {
    return this
  }
  return withAxisOrientation({ axisOrientationXOverride }, { axisOrientationYOverride })
}

fun Paintable.withOriginAtBottom(): Paintable {
  return withAxisOrientation(null, AxisOrientationY.OriginAtBottom)
}

/**
 * Flips the paintable
 */
fun Paintable.withAxisOrientation(
  axisOrientationXOverrideProvider: () -> AxisOrientationX?,
  axisOrientationYOverrideProvider: () -> AxisOrientationY?
): PaintableWithModifiedChartSupport {
  return PaintableWithModifiedChartSupport(this) {
    this.withAxisOrientation(axisOrientationXOverrideProvider(), axisOrientationYOverrideProvider())
  }
}

/**
 * Flips the axis of the given selection
 */
fun Paintable.withFlippedAxisOrientation(
  axisSelection: AxisSelection
): PaintableWithModifiedChartSupport {
  return withFlippedAxisOrientation { axisSelection }
}

/**
 * Flips the axis of the given selection
 */
fun Paintable.withFlippedAxisOrientation(
  flippedAxisOrientationSelectionProvider: () -> AxisSelection
): PaintableWithModifiedChartSupport {

  return PaintableWithModifiedChartSupport(this) {
    val flippedAxisOrientationSelection = flippedAxisOrientationSelectionProvider()

    val orientationX = axisOrientationX.oppositeIf(flippedAxisOrientationSelection.containsX)
    val orientationY = axisOrientationY.oppositeIf(flippedAxisOrientationSelection.containsY)

    withAxisOrientation(orientationX, orientationY)
  }
}

/**
 * Paints the paintable with the given zoom
 */
fun Paintable.withZoom(
  provider: () -> Zoom
): PaintableWithModifiedChartSupport {

  return PaintableWithModifiedChartSupport(this) {
    withZoom(provider())
  }
}

/**
 * Paints the paintable with the given zoom
 */
fun Paintable.withZoom(
  zoom: Zoom
): PaintableWithModifiedChartSupport {
  return withZoom { zoom }
}

inline fun Paintable.withDefaultZoom(
): PaintableWithModifiedChartSupport {
  return withZoom(Zoom.default)
}
