package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.button
import com.meistercharts.demo.configurableFont
import com.meistercharts.model.Insets
import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.cached
import it.neckar.open.formatting.format
import it.neckar.open.i18n.I18nConfiguration

class ValueAxisContentViewportCalculationsDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value Axis - Content Viewport Calculations"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Calculations

  override val description: String =
    """
      Visualizes the preferred content viewport margins - calculated by the axis layer
  """.trimIndent()

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val valueAxis = ValueAxisLayer("Title", ValueRange.default) {
          size = 300.0 //space for larger fonts
          this.ticksFormat = object : NumberFormat {
            override fun format(value: Double, i18nConfiguration: I18nConfiguration): String {
              return "Äq: " + value.format()
            }
          }.cached()
        }

        val contentViewportGestalt = ContentViewportGestalt(Insets.of(40.0, 40.0, 40.0, 40.0))
        contentViewportGestalt.configure(this)

        configure {
          layers.addClearBackground()
          layers.addLayer(valueAxis)
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Notification

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.stroke(Color.green)

              valueAxis.style.calculatePreferredViewportMarginTop().let { y ->
                gc.strokeLine(0.0, y, gc.width, y)
              }
              (gc.height - valueAxis.style.calculatePreferredViewportMarginBottom()).let { y ->
                gc.strokeLine(0.0, y, gc.width, y)
              }
            }
          })
        }

        configurableFont("Tick Font", valueAxis.style::tickFont) {
        }

        button("Match CVP margin") {
          contentViewportGestalt.setMarginTop(valueAxis.style.calculatePreferredViewportMarginTop())
          contentViewportGestalt.setMarginBottom(valueAxis.style.calculatePreferredViewportMarginBottom())
        }
      }
    }
  }
}
