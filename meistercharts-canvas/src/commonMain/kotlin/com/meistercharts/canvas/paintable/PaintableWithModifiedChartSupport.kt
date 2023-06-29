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

import com.meistercharts.axis.AxisOrientationX
import com.meistercharts.axis.AxisOrientationY
import com.meistercharts.axis.AxisSelection
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.withCurrentChartState
import com.meistercharts.state.withAxisOrientation
import com.meistercharts.state.withZoom
import com.meistercharts.geometry.Rectangle
import com.meistercharts.model.Zoom
import com.meistercharts.state.ChartState

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
