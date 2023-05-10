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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.axis.AxisEndConfiguration
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layout.BoxLayoutCalculator
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.textService
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.section
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import com.meistercharts.provider.SizedLabelsProvider
import com.meistercharts.provider.forList
import it.neckar.open.provider.BooleanProvider
import it.neckar.open.provider.SizedProvider2
import it.neckar.open.i18n.TextKey

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class CategoryAxisTopTopTitleWithAxisLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Category Axis Top Top Title"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val contentViewportGestalt = ContentViewportGestalt(Insets.of(40.0, 20.0, 20.0, 20.0))
        contentViewportGestalt.configure(this@meistercharts)


        configure {
          layers.addClearBackground()

          val labelsProvider: SizedLabelsProvider = SizedProvider2.forList(listOf(TextKey.simple("Hello"), TextKey.simple("World"), TextKey.simple("Dadada")))


          val axisLayer = CategoryAxisLayer(
            CategoryAxisLayer.Data(
              labelsProvider = labelsProvider,
              layoutProvider = {
                BoxLayoutCalculator.layout(chartSupport.chartCalculator.contentAreaRelative2zoomedX(1.0), labelsProvider.size(chartSupport.textService, chartSupport.i18nConfiguration), layoutDirection = LayoutDirection.TopToBottom)
              }
            )
          ) {
            tickOrientation = Vicinity.Outside
            axisEndConfiguration = AxisEndConfiguration.Default
            paintRange = AxisStyle.PaintRange.ContentArea

            titleVisible = BooleanProvider.False
          }

          val layer = AxisTopTopTitleLayer.forAxis(axisLayer)
          layers.addLayer(axisLayer)
          layers.addLayer(layer)


          section("Axis")

          configurableEnum("Paint Range", axisLayer.style::paintRange)
          configurableEnum("Tick Orientation", axisLayer.style::tickOrientation)

          configurableDouble("Size", axisLayer.style::size) {
            max = 200.0
          }

          configurableEnum("Side", axisLayer.style::side)

          section("Title")

          configurableDouble("Gap Horizontal", layer.configuration::titleGapHorizontal) {
            max = 50.0
          }
          configurableDouble("Gap Vertical", layer.configuration::titleGapVertical) {
            max = 50.0
          }

          configurableColorPickerProvider("Title Color", layer.configuration::titleColor)
          configurableFont("Title Font", layer.configuration::titleFont)

          configurableInsetsSeparate("Content viewport", contentViewportGestalt::contentViewportMargin)
        }
      }
    }
  }
}
