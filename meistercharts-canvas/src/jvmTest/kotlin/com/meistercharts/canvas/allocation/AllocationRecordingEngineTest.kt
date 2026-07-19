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

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

/**
 * Verifies the aggregation and layer attribution of the recording engine deterministically - without
 * relying on JFR delivering samples (which is timing dependent and sampled). The real JFR pipeline is
 * verified manually, see `docs/allocation-recording.md`.
 */
class AllocationRecordingEngineTest {
  @AfterEach
  fun resetEngine() {
    AllocationRecordingEngine.mode = AllocationRecordingMode.Off
  }

  @Test
  fun `empty report when nothing recorded`() {
    AllocationRecordingEngine.reset()
    assertThat(AllocationRecordingEngine.currentReport().layerAllocations).isEmpty()
    assertThat(AllocationRecordingEngine.currentReport().totalSamples).isEqualTo(0)
  }

  @Test
  fun `samples are aggregated per layer and type and sorted by count`() {
    AllocationRecordingEngine.reset()

    AllocationRecordingEngine.recordSample("[D", 256, "ValueAxisLayer", null)
    AllocationRecordingEngine.recordSample("[D", 256, "ValueAxisLayer", null)
    AllocationRecordingEngine.recordSample("com.meistercharts.model.Insets", 32, "ValueAxisLayer", null)
    AllocationRecordingEngine.recordSample("com.meistercharts.model.Coordinates", 24, "TooltipLayer", null)

    val report = AllocationRecordingEngine.currentReport()
    assertThat(report.totalSamples).isEqualTo(4)
    assertThat(report.estimatedBytes).isEqualTo(568)

    //layers sorted by sample count descending -> ValueAxisLayer (3) before TooltipLayer (1)
    val valueAxis = report.layerAllocations.first()
    assertThat(valueAxis.layerName).isEqualTo("ValueAxisLayer")
    assertThat(valueAxis.totalSamples).isEqualTo(3)

    //types within the layer sorted by sample count descending -> [D (2) first
    val topType = valueAxis.allocationsByType.first()
    assertThat(topType.typeName).isEqualTo("[D")
    assertThat(topType.samples).isEqualTo(2)
    assertThat(topType.estimatedBytes).isEqualTo(512)

    assertThat(report.worst?.layerName).isEqualTo("ValueAxisLayer")
  }

  @Test
  fun `stacktrace samples are aggregated per stacktrace`() {
    AllocationRecordingEngine.reset()

    AllocationRecordingEngine.recordSample("[D", 256, "ValueAxisLayer", "ValueAxisLayer.paint(117)")
    AllocationRecordingEngine.recordSample("[D", 256, "ValueAxisLayer", "ValueAxisLayer.paint(117)")

    val type = AllocationRecordingEngine.currentReport().layerAllocations.single().allocationsByType.single()
    assertThat(type.samples).isEqualTo(2)
    assertThat(type.stacktraces).isEqualTo(mapOf("ValueAxisLayer.paint(117)" to 2))
  }

  @Test
  fun `extractLayerName picks the outermost paint frame of a layer`() {
    //top -> root: allocation site, a painter, the layer paint, the Layers dispatcher, the render loop
    val frames = listOf(
      FrameRef("com.meistercharts.algorithms.painter.LinePainter", "paintLine", 40),
      FrameRef("com.meistercharts.algorithms.layers.axis.ValueAxisLayer", "paint", 117),
      FrameRef("com.meistercharts.algorithms.layers.Layers", "paintLayers", 283),
      FrameRef("com.meistercharts.canvas.ChartSupport", "render", 456),
    )

    assertThat(AllocationRecordingEngine.extractLayerName(frames)).isEqualTo("ValueAxisLayer")
  }

  @Test
  fun `extractLayerName also attributes allocations from the layout phase`() {
    //top -> root: allocation site, the layer's layout, the Layers dispatcher, the render loop
    val frames = listOf(
      FrameRef("com.meistercharts.history.EnumDataSeriesIndex", "box-impl", 0),
      FrameRef("com.meistercharts.algorithms.layers.AbstractHistoryStripeLayer", "layout", 182),
      FrameRef("com.meistercharts.algorithms.layers.Layers", "paintLayers", 267),
      FrameRef("com.meistercharts.canvas.DefaultLayerSupport", "paint", 300),
    )

    assertThat(AllocationRecordingEngine.extractLayerName(frames)).isEqualTo("AbstractHistoryStripeLayer")
  }

  @Test
  fun `extractLayerName returns null when no layer paint is on the stack`() {
    val frames = listOf(
      FrameRef("com.meistercharts.canvas.events.MouseEventHandler", "onClick", 20),
      FrameRef("com.meistercharts.canvas.ChartSupport", "handleEvent", 300),
    )

    assertThat(AllocationRecordingEngine.extractLayerName(frames)).isNull()
  }

  @Test
  fun `recording infrastructure frames are recognized`() {
    with(AllocationRecordingEngine) {
      assertThat(FrameRef("com.meistercharts.canvas.allocation.AllocationRecordingEngine", "currentReport", 60).isRecordingInfrastructure()).isTrue()
      assertThat(FrameRef("com.meistercharts.algorithms.layers.debug.AllocationRecordingLayer", "paint", 40).isRecordingInfrastructure()).isTrue()
      assertThat(FrameRef("com.meistercharts.algorithms.layers.axis.ValueAxisLayer", "paint", 117).isRecordingInfrastructure()).isFalse()
    }
  }

  @Test
  fun `readableTypeName decodes JVM array descriptors`() {
    assertThat(AllocationRecordingEngine.readableTypeName("[B")).isEqualTo("byte[]")
    assertThat(AllocationRecordingEngine.readableTypeName("[D")).isEqualTo("double[]")
    assertThat(AllocationRecordingEngine.readableTypeName("[[I")).isEqualTo("int[][]")
    assertThat(AllocationRecordingEngine.readableTypeName("[Ljava.lang.Object;")).isEqualTo("java.lang.Object[]")
    assertThat(AllocationRecordingEngine.readableTypeName("[Lcom.meistercharts.model.Insets;")).isEqualTo("com.meistercharts.model.Insets[]")
    assertThat(AllocationRecordingEngine.readableTypeName("java.util.ArrayList")).isEqualTo("java.util.ArrayList")
    assertThat(AllocationRecordingEngine.readableTypeName("com.meistercharts.model.Insets")).isEqualTo("com.meistercharts.model.Insets")
  }

  @Test
  fun `mode next cycles through all modes`() {
    assertThat(AllocationRecordingMode.Off.next()).isEqualTo(AllocationRecordingMode.ByType)
    assertThat(AllocationRecordingMode.ByType.next()).isEqualTo(AllocationRecordingMode.ByTypeAndStacktrace)
    assertThat(AllocationRecordingMode.ByTypeAndStacktrace.next()).isEqualTo(AllocationRecordingMode.Off)
  }

  @Test
  fun `setting mode to off clears the accumulated samples`() {
    AllocationRecordingEngine.mode = AllocationRecordingMode.ByType
    AllocationRecordingEngine.recordSample("[D", 256, "ValueAxisLayer", null)
    assertThat(AllocationRecordingEngine.currentReport().totalSamples).isEqualTo(1)

    AllocationRecordingEngine.mode = AllocationRecordingMode.Off
    assertThat(AllocationRecordingEngine.currentReport().layerAllocations).isEmpty()
  }
}
