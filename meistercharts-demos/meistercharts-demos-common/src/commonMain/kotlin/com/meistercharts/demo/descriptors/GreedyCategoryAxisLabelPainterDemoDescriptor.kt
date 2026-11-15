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

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.GreedyCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.algorithms.layout.BoxLayoutCalculator
import com.meistercharts.color.Color
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.paintable.RectanglePaintable
import com.meistercharts.canvas.textService
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.mapWithPlusUuid
import it.neckar.geometry.Side
import it.neckar.geometry.Size
import com.meistercharts.model.Vicinity
import com.meistercharts.provider.SizedLabelsProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService
import it.neckar.open.observable.ObservableObject
import kotlin.math.roundToInt

/**
 * Demonstrates the usage of the [CategoryAxisLayer]
 */
class GreedyCategoryAxisLabelPainterDemoDescriptor : MeisterchartsDemoDescriptor<CategoryAxisDemoConfig> {
  override val name: String = "Greedy Category Axis Label Painter"

  override val uuid: Uuid = Uuid.parse("9c02c3e6-5823-4a5d-a8eb-ccc5a3ea24e3")

  //language=HTML
  override val description: String = "Demonstrates how the GreedyCategoryAxisLabelPainter works"
  override val category: DemoCategory = DemoCategory.Axis

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<CategoryAxisDemoConfig>> = CategoryAxisDemoConfig.createConfigs().mapWithPlusUuid(638351)

  override fun prepareDemo(configuration: PredefinedConfiguration<CategoryAxisDemoConfig>?): MeisterchartsDemo {
    require(configuration != null) { "GreedyCategoryAxisLabelPainterDemoDescriptor.prepareDemo requires a predefined configuration" }

    val initialAxisSide: Side = configuration.payload.side
    val initialAxisTickOrientation: Vicinity = configuration.payload.axisTickOrientation

    val axisSideProperty = ObservableObject(initialAxisSide)

    return MeisterchartsDemo(this) {
      meistercharts {

        configure {

          layers.addClearBackground()

          val passpartoutLayer = PasspartoutLayer {
            color = { Color("rgba(204, 155, 33, 0.25)") } // use something different from white so the size of the axis can be better grasped
          }
          layers.addLayer(passpartoutLayer)

          val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
          var tickLabelLength = 6

          var categoriesCount = 15

          val labelsProvider: SizedLabelsProvider = object : SizedLabelsProvider {
            override fun size(param1: TextService, param2: I18nConfiguration): Int {
              return categoriesCount
            }

            override fun valueAt(index: Int, param1: TextService, param2: I18nConfiguration): String {
              return chars.substring(0, tickLabelLength)
            }
          }

          val data = CategoryAxisLayer.Configuration(
            labelsProvider
          ) {
            when (axisSideProperty.get()) {
              Side.Top, Side.Bottom -> BoxLayoutCalculator.layout(chartSupport.chartCalculator.contentAreaRelative2zoomedX(1.0), labelsProvider.size(chartSupport.textService, chartSupport.i18nConfiguration), layoutDirection = LayoutDirection.TopToBottom)
              Side.Left, Side.Right -> BoxLayoutCalculator.layout(chartSupport.chartCalculator.contentAreaRelative2zoomedY(1.0), labelsProvider.size(chartSupport.textService, chartSupport.i18nConfiguration), layoutDirection = LayoutDirection.LeftToRight)
            }
          }

          val greedyCategoryAxisLabelPainter = GreedyCategoryAxisLabelPainter()

          var axisTitle = "Category Axis"

          val categoryAxisLayer = CategoryAxisLayer(data) {
            this.axisLabelPainter = greedyCategoryAxisLabelPainter
            this.tickOrientation = initialAxisTickOrientation
            this.titleProvider = { _, _ -> axisTitle }
          }

          layers.addLayer(categoryAxisLayer)

          axisSideProperty.consumeImmediately {
            categoryAxisLayer.configuration.side = it
          }

          passpartoutLayer.configuration.bind(categoryAxisLayer.configuration)

          configurableInt("Number of categories") {
            value = categoriesCount
            min = 0
            max = 20
            onChange {
              categoriesCount = it
              markAsDirty()
            }
          }

          configurableInt("Tick label length") {
            value = tickLabelLength
            min = 0
            max = chars.length
            onChange {
              tickLabelLength = it
              markAsDirty()
            }
          }

          configurableBoolean("With title") {
            value = axisTitle.isNotBlank()
            onChange {
              axisTitle = if (it) {
                "Category Axis"
              } else {
                ""
              }
              markAsDirty()
            }
          }

          configurableBoolean("With category images") {
            value = false
            onChange {
              if (it) {
                greedyCategoryAxisLabelPainter.style.imagesProvider = MultiProvider { _ ->
                  RectanglePaintable(greedyCategoryAxisLabelPainter.style.imageSize, Color.magenta)
                }
              } else {
                greedyCategoryAxisLabelPainter.style.imagesProvider = MultiProvider.alwaysNull()
              }
              markAsDirty()
            }
          }

          configurableInt("Category image size") {
            value = greedyCategoryAxisLabelPainter.style.imageSize.width.roundToInt()
            min = 1
            step = 1
            max = 500
            onChange {
              greedyCategoryAxisLabelPainter.style.imageSize = Size(it, it)
              markAsDirty()
            }
          }

          configurable("Category label gap", greedyCategoryAxisLabelPainter.style::categoryLabelGap) {
            min = 1.0
            max = 50.0
          }

          configurableEnum("Axis side", axisSideProperty, kotlin.enums.enumEntries<Side>().toList())

          configurable("Axis size", categoryAxisLayer.configuration::size) {
            max = 300.0
            onChange {
              categoryAxisLayer.configuration.size = it
              markAsDirty()
            }
          }

          configurable("Tick orientation", categoryAxisLayer.configuration::tickOrientation, kotlin.enums.enumEntries<Vicinity>().toList()) {
          }

          configurable("Tick line width", categoryAxisLayer.configuration::tickLineWidth) {
            max = 20.0
          }

          configurable("Tick length", categoryAxisLayer.configuration::tickLength) {
            max = 20.0
          }

          configurable("Tick label gap", categoryAxisLayer.configuration::tickLabelGap) {
            min = 0.0
            max = 20.0
          }

        }
      }
    }
  }

}
