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
package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DefaultLayerSupport
import com.meistercharts.canvas.DirtyReasonBitSet
import com.meistercharts.canvas.LayerIndex
import com.meistercharts.canvas.MockCanvas
import com.meistercharts.canvas.allocation.AllocationRecordingEngine
import com.meistercharts.canvas.allocation.AllocationRecordingMode
import com.meistercharts.canvas.mock.MockCanvasRenderingContext
import com.meistercharts.loop.PaintingLoopIndex
import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import it.neckar.geometry.Size

/**
 * Smoke test: the overlay must paint without throwing - both while recording (with samples) and off.
 */
class AllocationRecordingLayerTest {
  @AfterEach
  fun resetEngine() {
    AllocationRecordingEngine.mode = AllocationRecordingMode.Off
  }

  @Test
  fun `paints without throwing while recording with samples`() {
    AllocationRecordingEngine.mode = AllocationRecordingMode.ByType
    AllocationRecordingEngine.recordSample("com.meistercharts.model.Insets", 32, "ValueAxisLayer", null)
    AllocationRecordingEngine.recordSample("[D", 256, "ValueAxisLayer", null)

    AllocationRecordingLayer().also {
      it.layout(paintingContext())
      it.paint(paintingContext())
    }
  }

  @Test
  fun `does not paint while off`() {
    AllocationRecordingEngine.mode = AllocationRecordingMode.Off
    AllocationRecordingLayer().also {
      it.layout(paintingContext())
      it.paint(paintingContext())
    }
  }

  @Test
  fun `rebuilds the report only once per update rate`() {
    AllocationRecordingEngine.mode = AllocationRecordingMode.ByType
    AllocationRecordingEngine.recordSample("com.meistercharts.model.Insets", 32, "ValueAxisLayer", null)

    val layer = AllocationRecordingLayer()
    layer.layout(paintingContext(frameTimestamp = 1000.0))
    assertThat(layer.paintingVariables().lines).contains("1 samples")

    AllocationRecordingEngine.recordSample("[D", 256, "ValueAxisLayer", null)

    layer.layout(paintingContext(frameTimestamp = 1400.0))
    assertThat(layer.paintingVariables().lines).contains("1 samples")

    layer.layout(paintingContext(frameTimestamp = 1600.0))
    assertThat(layer.paintingVariables().lines).contains("2 samples")
  }

  @Test
  fun `rebuilds the report immediately when the mode changes`() {
    AllocationRecordingEngine.mode = AllocationRecordingMode.ByType
    AllocationRecordingEngine.recordSample("com.meistercharts.model.Insets", 32, "ValueAxisLayer", null)

    val layer = AllocationRecordingLayer()
    layer.layout(paintingContext(frameTimestamp = 1000.0))
    assertThat(layer.paintingVariables().lines.first()).contains(AllocationRecordingMode.ByType.name)

    AllocationRecordingEngine.mode = AllocationRecordingMode.ByTypeAndStacktrace

    layer.layout(paintingContext(frameTimestamp = 1010.0))
    assertThat(layer.paintingVariables().lines.first()).contains(AllocationRecordingMode.ByTypeAndStacktrace.name)
  }

  private fun paintingContext(frameTimestamp: Double = 10.0): LayerPaintingContext {
    val canvasSize = Size.of(800.0, 600.0)
    val canvas = MockCanvas()
    val chartSupport = ChartSupport(canvas)
    BindContentAreaSize2ContentViewport().bindResize(chartSupport)
    canvas.size = canvasSize

    return LayerPaintingContext(
      gc = MockCanvasRenderingContext().also { it.canvasSize = canvasSize },
      layerSupport = DefaultLayerSupport(chartSupport),
      frameTimestamp = frameTimestamp,
      frameTimestampDelta = 0.0,
      loopIndex = PaintingLoopIndex(0),
      layerLayoutIndex = LayerIndex.unknown,
      layerPaintIndex = LayerIndex.unknown,
      dirtyReasons = DirtyReasonBitSet.empty,
    )
  }
}
