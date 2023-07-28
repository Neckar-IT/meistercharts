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
package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import com.meistercharts.zoom.UpdateReason
import com.meistercharts.zoom.ZoomAndTranslationModifier
import com.meistercharts.calc.ZoomLevelCalculator
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import com.meistercharts.zoom.ZoomAndTranslationModifiersBuilder
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.charts.ChartId
import it.neckar.geometry.Distance
import it.neckar.geometry.Size
import com.meistercharts.model.Zoom
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class LayerSupportTest {
  @Test
  fun testRemoveAll() {
    val layers = Layers(ChartId(17))

    assertThat(layers.layers).hasSize(0)

    var removeCalled0 = false
    layers.addLayer(object : AbstractLayer() {
      override val type: LayerType = LayerType.Content

      override fun paint(paintingContext: LayerPaintingContext) {
      }

      override fun removed() {
        super.removed()
        assertThat(removeCalled0).isFalse()
        removeCalled0 = true
      }
    })

    var removeCalled1 = false
    layers.addLayer(object : AbstractLayer() {
      override val type: LayerType = LayerType.Content

      override fun paint(paintingContext: LayerPaintingContext) {
      }

      override fun removed() {
        super.removed()
        assertThat(removeCalled1).isFalse()
        removeCalled1 = true
      }
    })

    assertThat(removeCalled0).isFalse()
    assertThat(removeCalled1).isFalse()

    layers.removeAll { true }.let {
      assertThat(it).hasSize(2)
    }

    assertThat(removeCalled0).isTrue()
    assertThat(removeCalled1).isTrue()

    assertThat(layers.isEmpty()).isTrue()
  }

  @Test
  fun testRemoveCalled() {
    val layers = Layers(ChartId(17))

    assertThat(layers.layers).hasSize(0)

    var removeCalled = false

    layers.addLayer(object : AbstractLayer() {
      override val type: LayerType = LayerType.Content

      override fun paint(paintingContext: LayerPaintingContext) {
      }

      override fun removed() {
        super.removed()
        assertThat(removeCalled).isFalse()
        removeCalled = true
      }
    })

    assertThat(removeCalled).isFalse()

    assertThat(layers.layers).hasSize(1)
    layers.removeLayer(layers.layers[0])
    assertThat(layers.layers).hasSize(0)

    assertThat(removeCalled).isTrue()
  }

  @Test
  fun testResize() {
    //Because of the overscan!
    val expectedDefaultZoom = Zoom.of(0.9, 0.9)

    val zoomAndPanModifier = ZoomAndTranslationModifiersBuilder()
      .build()

    val canvas = MockCanvas()

    val chartSupport = ChartSupport(canvas, zoomAndPanModifier, ZoomAndTranslationDefaults.tenPercentMargin, ZoomLevelCalculator.SQRT_2)
    BindContentAreaSize2ContentViewport().bindResize(chartSupport)

    val chartState = chartSupport.currentChartState

    assertThat(chartState.windowTranslation).isEqualTo(Distance.none)
    assertThat(chartState.zoom).isEqualTo(expectedDefaultZoom)

    assertThat(canvas.size).isEqualTo(Size.none)
    assertThat(chartState.contentAreaSize).isEqualTo(Size.none)

    //Update canvas size
    canvas.size = Size.of(800.0, 600.0) //--> this results in an automatic call to resetToDefaults
    assertThat(chartState.contentAreaSize).isEqualTo(canvas.size)

    chartSupport.zoomAndTranslationSupport.resetToDefaults(reason = UpdateReason.UserInteraction)

    assertThat(chartState.contentAreaSize).isEqualTo(canvas.size)
    assertThat(chartState.zoom).isEqualTo(expectedDefaultZoom)
    assertThat(chartState.windowTranslation).isEqualTo(Distance.of(800 / 10.0 / 2 * expectedDefaultZoom.scaleX, 600 / 10.0 / 2 * expectedDefaultZoom.scaleY)) //Should be centered


    //Update the canvas size further
    canvas.size = Size.of(1000.0, 720.0)
    assertThat(chartState.contentAreaSize).isEqualTo(canvas.size)

    assertThat(chartState.windowTranslation.x).isEqualTo(1000 / 10.0 / 2 * expectedDefaultZoom.scaleX)
    //Should be centered (again)
    Offset.offset(0.01)
    assertThat(chartState.windowTranslation.y).isCloseTo(720 / 10.0 / 2 * expectedDefaultZoom.scaleY, 0.000111111111111111)
    //Should be centered (again)
  }

  @Test
  fun testResizeStrategyBind() {
    val canvas = MockCanvas()
    val chartSupport = ChartSupport(canvas, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation, ZoomLevelCalculator.SQRT_2)
    BindContentAreaSize2ContentViewport().bindResize(chartSupport)

    assertThat(chartSupport.currentChartState.contentAreaSize).isEqualTo(Size.none)

    canvas.size = Size.of(800.0, 600.0)
    assertThat(chartSupport.currentChartState.contentAreaSize).isEqualTo(canvas.size)
  }

  @Test
  fun testResizeStrategyFixed() {
    val canvas = MockCanvas()
    val fixedSize = Size.of(800.8, 600.6)
    val chartSupport = ChartSupport(canvas, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation, ZoomLevelCalculator.SQRT_2)
    FixedContentAreaSize(fixedSize).bindResize(chartSupport)

    assertThat(chartSupport.currentChartState.contentAreaSize).isEqualTo(fixedSize)

    canvas.size = Size.of(800.0, 600.0)

    //*not* equal to canvas size - but set to the fixed size
    assertThat(chartSupport.currentChartState.contentAreaSize).isEqualTo(fixedSize)
  }

  @Test
  fun testResizeXFixedYBoundContentAreaSizing() {
    val canvas = MockCanvas()
    val fixedX = 800.8
    val layerSupport = ChartSupport(canvas, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation, ZoomLevelCalculator.SQRT_2)
    FixedContentAreaWidth(fixedX).bindResize(layerSupport)

    assertThat(layerSupport.currentChartState.contentAreaSize.width).isEqualTo(fixedX)
    assertThat(layerSupport.currentChartState.contentAreaSize.height).isEqualTo(0.0)

    canvas.size = Size.of(800.0, 600.0)

    //*not* equal to canvas size - but set to the fixed size
    assertThat(layerSupport.currentChartState.contentAreaSize.width).isEqualTo(fixedX)
    assertThat(layerSupport.currentChartState.contentAreaSize.height).isEqualTo(canvas.height)
  }
}

