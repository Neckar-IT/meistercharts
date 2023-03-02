package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.axis.AxisEndConfiguration
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.addClearBackground
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
import it.neckar.open.provider.BooleanProvider

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class ValueAxisTopTopTitleWithAxisLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value Axis Top Top Title"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val contentViewportGestalt = ContentViewportGestalt(Insets.of(40.0, 20.0, 20.0, 20.0))
        contentViewportGestalt.configure(this@meistercharts)


        configure {
          layers.addClearBackground()

          val valueAxisLayer = ValueAxisLayer("MyAxis has a long title", ValueRange.default) {
            tickOrientation = Vicinity.Outside
            axisEndConfiguration = AxisEndConfiguration.Default
            paintRange = AxisStyle.PaintRange.ContentArea

            titleVisible = BooleanProvider.False
          }

          val layer = AxisTopTopTitleLayer.forAxis(valueAxisLayer)
          layers.addLayer(valueAxisLayer)
          layers.addLayer(layer)


          section("Axis")

          configurableEnum("Paint Range", valueAxisLayer.style::paintRange)
          configurableEnum("Tick Orientation", valueAxisLayer.style::tickOrientation)

          configurableDouble("Size", valueAxisLayer.style::size) {
            max = 200.0
          }

          configurableEnum("Side", valueAxisLayer.style::side)

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
