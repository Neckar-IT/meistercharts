package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.compass.ArrowValuePainter
import com.meistercharts.annotations.Domain
import com.meistercharts.charts.ClassicCompassGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInsetsSeparate

class ClassicCompassGestaltDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "Compass (Classic)"

  //language=HTML
  override val description: String = "Compass (Classic)"
  override val category: DemoCategory = DemoCategory.Gestalt

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      var valueRangeStart = 0.0
      var valueRangeEnd = 200.0

      var currentValue: @Domain Double = 17.0

      val valueRangeProvider = { ValueRange.linear(valueRangeStart, valueRangeEnd) }
      val gestalt = ClassicCompassGestalt(
        ClassicCompassGestalt.Data(
          valueRangeProvider = valueRangeProvider,
          currentValueProvider = { currentValue }
        )
      )

      meistercharts {
        gestalt.configure(this)

        configure {
          configurableInsetsSeparate("Margin", gestalt.style.marginProperty) {
            onChange {
              markAsDirty()
            }
          }

          configurableDouble("Current Value", currentValue) {
            min = -360.0
            max = 360.0

            onChange {
              currentValue = it
              markAsDirty()
            }
          }

          configurableDouble("Value Range from", valueRangeStart) {
            min = -500.0
            max = 500.0

            onChange {
              valueRangeStart = it
              markAsDirty()
            }
          }
          configurableDouble("Value Range until", valueRangeEnd) {
            min = -500.0
            max = 500.0

            onChange {
              valueRangeEnd = it
              markAsDirty()
            }
          }

          val valuePainter = gestalt.gaugePaintable.style.valuePainter as ArrowValuePainter
          configurableDouble("Pointer width", valuePainter.style::pointerLineWidth) {
            max = 20.0
          }
        }
      }
    }
  }
}
