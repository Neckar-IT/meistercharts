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
package com.meistercharts.canvas.allocation

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DefaultLayerSupport
import com.meistercharts.canvas.DirtyReasonBitSet
import com.meistercharts.canvas.LayerIndex
import com.meistercharts.canvas.MockCanvas
import com.meistercharts.canvas.mock.MockCanvasRenderingContext
import com.meistercharts.loop.PaintingLoopIndex
import com.meistercharts.model.Insets
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Size

/**
 * Runnable demo for the JFR allocation recording. Run its `main` (IDE: "Run AllocationRecordingDemoKt").
 *
 * It paints a layer that deliberately allocates on every paint, then prints the recorded report - the
 * types allocated per layer, with the allocation site. To profile a real chart, replace
 * [DemoAllocatingLayer] with the layer(s) of your gestalt and paint them the same way (headless via
 * [MockCanvasRenderingContext], or drive the real render loop).
 *
 * ATTENTION: JVM only. On JS the engine is inert. JFR *samples*, so counts are relative.
 */
fun main() {
  val canvasSize = Size.of(800.0, 600.0)
  val paintingContext = headlessPaintingContext(canvasSize)
  val layer = DemoAllocatingLayer()

  AllocationRecordingEngine.mode = AllocationRecordingMode.ByTypeAndStacktrace

  //The recording only keeps samples from the paint thread (the JavaFX Application Thread). Run the
  //paint loop on a thread with that name so this headless demo represents the real paint thread.
  val paintThread = Thread({ repeat(3000) { layer.paint(paintingContext) } }, "JavaFX Application Thread")
  paintThread.start()
  paintThread.join()

  Thread.sleep(1500) //let JFR flush the async samples

  val report = AllocationRecordingEngine.currentReport()
  AllocationRecordingEngine.mode = AllocationRecordingMode.Off

  println("=== Allocation report - ${report.totalSamples} samples, ~${report.estimatedBytes} bytes ===")
  report.layerAllocations.forEach { layerAllocations ->
    println("[${layerAllocations.layerName}] ${layerAllocations.totalSamples} samples")
    layerAllocations.allocationsByType.forEach { type ->
      val site = type.stacktraces.keys.firstOrNull()?.substringBefore('\n') ?: ""
      println("    ${type.samples}x ${type.typeName}  @ $site")
    }
  }
}

/**
 * A layer that allocates on every paint - stand-in for a real chart layer to demonstrate the report.
 */
private class DemoAllocatingLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content
  private var sink: Any? = null

  override fun paint(paintingContext: LayerPaintingContext) {
    repeat(3000) { index ->
      sink = Coordinates(index.toDouble(), index.toDouble())
      sink = Insets(1.0, 2.0, 3.0, 4.0)
      sink = ArrayList<Int>(4)
    }
  }
}

private fun headlessPaintingContext(canvasSize: Size): LayerPaintingContext {
  val canvas = MockCanvas()
  val chartSupport = ChartSupport(canvas)
  BindContentAreaSize2ContentViewport().bindResize(chartSupport)
  canvas.size = canvasSize

  return LayerPaintingContext(
    gc = MockCanvasRenderingContext().also { it.canvasSize = canvasSize },
    layerSupport = DefaultLayerSupport(chartSupport),
    frameTimestamp = 10.0,
    frameTimestampDelta = 0.0,
    loopIndex = PaintingLoopIndex(0),
    layerLayoutIndex = LayerIndex.unknown,
    layerPaintIndex = LayerIndex.unknown,
    dirtyReasons = DirtyReasonBitSet.empty,
  )
}
