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
import com.meistercharts.zoom.FittingWithMargin
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.createGrid
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.algorithms.layout.BoxLayoutCalculator
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.textService
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.createEnumConfigs
import com.meistercharts.model.Insets
import it.neckar.geometry.Side
import com.meistercharts.model.Vicinity
import com.meistercharts.provider.forTextKeys
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.SizedProvider2
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.asProvider

/**
 *
 */
class GridWithCategoryAxisDemoDescriptor() : MeisterchartsDemoDescriptor<Side> {
  override val uuid: Uuid = Uuid.parse("b694edcc-4049-4e8e-9053-45d708753a15")
  override val name: String = "Grid + Category Axis"
  override val description: String = "Grid connected to a category axis"
  override val category: DemoCategory = DemoCategory.Layers
  override val predefinedConfigurations: List<PredefinedConfiguration<Side>> = createEnumConfigs(uuid)
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Side>?): MeisterchartsDemo {
    require(configuration != null) { "GridWithCategoryAxisDemoDescriptor.prepareDemo requires a predefined configuration" }
    val axisSide: Side = configuration.payload

    return MeisterchartsDemo(this) {
      meistercharts {

        val insets = when (axisSide) {
          Side.Left -> Insets(0.0, 0.0, 0.0, 150.0)
          Side.Right -> Insets(0.0, 150.0, 0.0, 0.0)
          Side.Top -> Insets(60.0, 0.0, 0.0, 0.0)
          Side.Bottom -> Insets(0.0, 0.0, 60.0, 0.0)
        }

        val layoutDirection: LayoutDirection = when (axisSide) {
          Side.Left -> LayoutDirection.TopToBottom
          Side.Right -> LayoutDirection.TopToBottom
          Side.Top -> LayoutDirection.LeftToRight
          Side.Bottom -> LayoutDirection.LeftToRight
        }

        zoomAndTranslationDefaults {
          FittingWithMargin(insets)
        }

        configure {
          layers.addClearBackground()


          val labelsProvider = SizedProvider2.forTextKeys(
            listOf(
              TextKey.simple("Product 1"),
              TextKey.simple("Product 2"),
              TextKey.simple("Product 3"),
              TextKey.simple("Product 4"),
            )
          )
          val categoryAxisLayer = CategoryAxisLayer(
            CategoryAxisLayer.Configuration(labelsProvider) {
              BoxLayoutCalculator.layout(
                if (layoutDirection.orientation.isVertical()) chartSupport.currentChartState.windowHeight else chartSupport.currentChartState.windowWidth,
                labelsProvider.size(chartSupport.textService, chartSupport.i18nConfiguration),
                layoutDirection
              )
            }) {
            titleProvider = { _, _ -> "Categories" }
            size = insets[axisSide]
            tickOrientation = Vicinity.Outside
            side = axisSide
          }

          val gridLayer = categoryAxisLayer.createGrid {
            this.applyPasspartout(insets)
          }

          layers.addLayer(categoryAxisLayer)
          layers.addLayer(gridLayer)

          var (gridLineColor, gridLineWidth, gridLineStyle) = gridLayer.configuration.lineStyles.valueAt(0)

          configurableColorPicker("Line color", gridLineColor()) {
            onChange {
              gridLineColor = it.asProvider()
              gridLayer.configuration.lineStyles = MultiProvider.always(LineStyle(color = gridLineColor, lineWidth = gridLineWidth, dashes = gridLineStyle))
              markAsDirty()
            }
          }

          configurableDouble("Line width", gridLineWidth) {
            max = 10.0

            onChange {
              gridLineWidth = it
              gridLayer.configuration.lineStyles = MultiProvider.always(LineStyle(color = gridLineColor, lineWidth = gridLineWidth, dashes = gridLineStyle))
              markAsDirty()
            }
          }

          configurableList("Line style", gridLineStyle, Dashes.predefined) {
            onChange {
              gridLineStyle = it
              gridLayer.configuration.lineStyles = MultiProvider.always(LineStyle(color = gridLineColor, lineWidth = gridLineWidth, dashes = gridLineStyle))
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
