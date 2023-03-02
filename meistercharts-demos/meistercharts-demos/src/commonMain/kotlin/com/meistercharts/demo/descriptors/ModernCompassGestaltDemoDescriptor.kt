package com.meistercharts.demo.descriptors

import com.meistercharts.charts.ModernCompassGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.section
import it.neckar.open.provider.DoubleProvider
import kotlin.math.PI

class ModernCompassGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "Compass (Modern)"
  override val description: String = "Compass"
  override val category: DemoCategory = DemoCategory.Gestalt

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {

      val gestalt = ModernCompassGestalt()

      val currentValue = object {
        var value: Double = gestalt.data.currentValueProvider()
      }

      gestalt.data.currentValueProvider = DoubleProvider { currentValue.value }

      meistercharts {
        gestalt.configure(this)

        configure {
          configurableDouble("Current Value", currentValue::value) {
            min = gestalt.data.valueRangeProvider().start
            max = gestalt.data.valueRangeProvider().end
          }

          section("Needle")

          configurableDouble("Height", gestalt.compassTriangleValuePainter.style::height) {
            min = 0.0
            max = 200.0
          }

          configurableDouble("Width (rad)", gestalt.compassTriangleValuePainter.style::baseWidthRad) {
            min = -PI
            max = PI
          }

          configurableInsets("Margin", gestalt.style::margin) {
          }
        }
      }
    }
  }
}
