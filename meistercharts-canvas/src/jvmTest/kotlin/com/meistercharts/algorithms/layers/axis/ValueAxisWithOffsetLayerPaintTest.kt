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
package com.meistercharts.algorithms.layers.axis

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DefaultLayerSupport
import com.meistercharts.canvas.DirtyReasonBitSet
import com.meistercharts.canvas.LayerIndex
import com.meistercharts.canvas.MockCanvas
import com.meistercharts.canvas.mock.MockCanvasRenderingContext
import com.meistercharts.loop.PaintingLoopIndex
import com.meistercharts.range.ValueRange
import it.neckar.geometry.Direction
import it.neckar.geometry.Side
import it.neckar.geometry.Size
import org.junit.jupiter.api.Test

/**
 * Smoke test for [ValueAxisWithOffsetLayer].
 *
 * [ValueAxisWithOffsetLayer.paint] delegates through [com.meistercharts.algorithms.layers.barchart.AbstractAxisLayer],
 * whose translate helpers require a real backing canvas (not available for a mock gc). Therefore the tick + offset
 * painting entry points - [ValueAxisWithOffsetLayer.paintTicksWithLabelsVertically] and
 * [ValueAxisWithOffsetLayer.paintTicksWithLabelsHorizontally] - are exercised directly here.
 */
class ValueAxisWithOffsetLayerPaintTest {

  @Test
  fun `lays out and paints a large range vertically without throwing`() {
    val layer = ValueAxisWithOffsetLayer("Offset", ValueRange.linear(23_000.0, 24_000.0)) {
      side = Side.Left
      spaceForDigits = 3
    }

    val paintingContext = paintingContext(Size.of(800.0, 600.0))
    layer.layout(paintingContext)

    val paintingVariables = layer.paintingVariables()
    assertThat(paintingVariables.tickDomainValues.size).isGreaterThan(0)
    assertThat(paintingVariables.offsetTicks.size).isGreaterThan(0)
    assertThat(paintingVariables.offsetStep).isEqualTo(1_000.0)

    //Both vertical anchor directions (Outside / Inside)
    layer.paintTicksWithLabelsVertically(paintingContext, Direction.CenterRight)
    layer.paintTicksWithLabelsVertically(paintingContext, Direction.CenterLeft)
  }

  @Test
  fun `lays out and paints a large range horizontally without throwing`() {
    val layer = ValueAxisWithOffsetLayer("Offset", ValueRange.linear(23_000.0, 24_000.0)) {
      side = Side.Bottom
      spaceForDigits = 3
    }

    val paintingContext = paintingContext(Size.of(800.0, 600.0))
    layer.layout(paintingContext)

    val paintingVariables = layer.paintingVariables()
    assertThat(paintingVariables.tickDomainValues.size).isGreaterThan(0)
    assertThat(paintingVariables.offsetTicks.size).isGreaterThan(0)

    //Both horizontal anchor directions (Outside / Inside)
    layer.paintTicksWithLabelsHorizontally(paintingContext, Direction.TopCenter)
    layer.paintTicksWithLabelsHorizontally(paintingContext, Direction.BottomCenter)
  }

  @Test
  fun `paints a range crossing zero in both orientations`() {
    val vertical = ValueAxisWithOffsetLayer("Offset", ValueRange.linear(-40_000.0, 140_000.0)) {
      side = Side.Left
      spaceForDigits = 3
    }
    val horizontal = ValueAxisWithOffsetLayer("Offset", ValueRange.linear(-40_000.0, 140_000.0)) {
      side = Side.Bottom
      spaceForDigits = 3
    }

    val paintingContext = paintingContext(Size.of(800.0, 600.0))

    vertical.layout(paintingContext)
    vertical.paintTicksWithLabelsVertically(paintingContext, Direction.CenterRight)
    assertThat(vertical.paintingVariables().tickDomainValues.size).isGreaterThan(0)

    horizontal.layout(paintingContext)
    horizontal.paintTicksWithLabelsHorizontally(paintingContext, Direction.TopCenter)
    assertThat(horizontal.paintingVariables().tickDomainValues.size).isGreaterThan(0)
  }

  private fun paintingContext(canvasSize: Size): LayerPaintingContext {
    val canvas = MockCanvas()
    val chartSupport = ChartSupport(canvas)
    BindContentAreaSize2ContentViewport().bindResize(chartSupport)
    canvas.size = canvasSize

    val gc = MockCanvasRenderingContext().also { it.canvasSize = canvasSize }

    return LayerPaintingContext(
      gc = gc,
      layerSupport = DefaultLayerSupport(chartSupport),
      frameTimestamp = 10.0,
      frameTimestampDelta = 0.0,
      loopIndex = PaintingLoopIndex(0),
      layerLayoutIndex = LayerIndex.unknown,
      layerPaintIndex = LayerIndex.unknown,
      dirtyReasons = DirtyReasonBitSet.empty,
    )
  }
}
