package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.createGrid
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.createEnumConfigs
import com.meistercharts.model.Insets
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.kotlin.lang.asProvider1

/**
 */
class GridWithValueAxisDemoDescriptor() : ChartingDemoDescriptor<Side> {
  override val name: String = "Grid + Value Axis"
  override val description: String = "Grid connected to a value axis"
  override val category: DemoCategory = DemoCategory.Layers

  override val predefinedConfigurations: List<PredefinedConfiguration<Side>> = createEnumConfigs()

  override fun createDemo(configuration: PredefinedConfiguration<Side>?): ChartingDemo {
    require(configuration != null)
    val axisSide: Side = configuration.payload

    return ChartingDemo {
      meistercharts {

        val insets = when (axisSide) {
          Side.Left -> Insets(0.0, 0.0, 0.0, 90.0)
          Side.Right -> Insets(0.0, 90.0, 0.0, 0.0)
          Side.Top -> Insets(60.0, 0.0, 0.0, 0.0)
          Side.Bottom -> Insets(0.0, 0.0, 60.0, 0.0)
        }

        zoomAndTranslationDefaults {
          FittingWithMargin(insets)
        }

        configure {
          layers.addClearBackground()

          val valueRange = ValueRange.linear(-7.0, 92.5)
          val valueAxisLayer = ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { valueRange })) {
            titleProvider = { _, _ -> "Performance [c/h²]" }
            size = insets[axisSide]
            tickOrientation = Vicinity.Outside
            side = axisSide
          }

          val gridLayer = valueAxisLayer.createGrid() {
            passpartout = insets
          }

          layers.addLayer(valueAxisLayer)
          layers.addLayer(gridLayer)

          var (gridColor, gridLineWidth, gridLineStyle) = gridLayer.configuration.lineStyles(0.0)

          configurableColor("Grid Color", gridColor) {
            onChange {
              gridColor = it
              gridLayer.configuration.lineStyles = LineStyle(color = gridColor, lineWidth = gridLineWidth, dashes = gridLineStyle).asProvider1()
              markAsDirty()
            }
          }

          configurableDouble("Line Width", gridLineWidth) {
            max = 10.0

            onChange {
              gridLineWidth = it
              gridLayer.configuration.lineStyles = LineStyle(color = gridColor, lineWidth = gridLineWidth, dashes = gridLineStyle).asProvider1()
              markAsDirty()
            }
          }

          configurableList("Line style", gridLineStyle, Dashes.predefined) {
            onChange {
              gridLineStyle = it
              gridLayer.configuration.lineStyles = LineStyle(color = gridColor, lineWidth = gridLineWidth, dashes = gridLineStyle).asProvider1()
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
