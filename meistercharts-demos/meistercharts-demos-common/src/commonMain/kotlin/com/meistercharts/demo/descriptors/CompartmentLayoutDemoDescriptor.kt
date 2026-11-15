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
package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.TransformingChartStateLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.color.Color
import com.meistercharts.state.withAdditionalTranslation
import com.meistercharts.state.withContentAreaSize
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.saved
import com.meistercharts.charts.FitContentInViewportGestalt
import com.meistercharts.color.ColorProvider
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Distance
import com.meistercharts.model.Insets
import it.neckar.geometry.Size

/**
 */
class CompartmentLayoutDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("df3cc8b2-87a0-4b80-be56-aa2d69242afc")
  override val name: String = "Compartment Layout"

  override val description: String = "Three layers laid out by modifying the chart state"

  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        FitContentInViewportGestalt(Insets.of(25.0)).configure(this)

        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          val chartCalculator = chartSupport.chartCalculator

          layers.addLayer(TransformingChartStateLayer(MyDebugLayer(Color.aquamarine)) { chartState ->
            chartState.withContentAreaSize(chartState.contentAreaSize.let {
              Size(it.width, it.height / 3.0)
            })
          })

          layers.addLayer(TransformingChartStateLayer(MyDebugLayer(Color.blue)) {
            @ContentArea val contentAreaSize = chartCalculator.chartState.contentAreaSize
            @ContentArea val height = contentAreaSize.height / 3.0

            @Zoomed val additionalTranslation = Distance.of(0.0, chartCalculator.contentArea2zoomedY(height))

            it.withContentAreaSize(Size(contentAreaSize.width, height)).withAdditionalTranslation(additionalTranslation)
          })

          layers.addLayer(TransformingChartStateLayer(MyDebugLayer(Color.green)) {
            @ContentArea val contentAreaSize = chartCalculator.chartState.contentAreaSize
            @ContentArea val height = contentAreaSize.height / 3.0

            @Zoomed val additionalTranslation = Distance.of(0.0, chartCalculator.contentArea2zoomedY(height) * 2)
            it.withContentAreaSize(Size(contentAreaSize.width, height)).withAdditionalTranslation(additionalTranslation)
          })
        }
      }
    }
  }
}

class MyDebugLayer(val color: ColorProvider) : ContentAreaDebugLayer({
  lines(color)
}) {
  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.paintMark(color = color())

    gc.saved {
      val chartCalculator = paintingContext.chartCalculator
      gc.translate(chartCalculator.contentAreaRelative2windowX(0.0), chartCalculator.contentAreaRelative2windowY(0.0))
      gc.paintMark(color = color())
    }

    super.paint(paintingContext)
  }
}
