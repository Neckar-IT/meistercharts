package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.LowerLimit
import com.meistercharts.algorithms.layers.UpperLimit
import com.meistercharts.charts.PixelValuesGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section

/**
 *
 */
class As30DiagramDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "AS30 - Category Line"
  override val category: DemoCategory = DemoCategory.Automation

  //language=HTML
  override val description: String = "Visualizes the output of a optical sensor (256 values)"

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val gestalt = PixelValuesGestalt()
        gestalt.configure(this)

        configurableList(
          "Value Axis Units Override", gestalt.style.yValueAxisValueRangeOverride,
          listOf(ValueRange.default, ValueRange.linear(7.0, 203.0), ValueRange.percentage, ValueRange.linear(777.0, 33333.0))
        ) {
          onChange {
            gestalt.style.yValueAxisValueRangeOverride = it
            markAsDirty()
          }
        }

        configurableEnum("held?", gestalt.model::mode, enumValues())
        configurableBoolean("Limits visible", gestalt.model::limitsVisible)

        configurableDouble("lower limit", gestalt.model.lowerLimit.limit) {
          onChange {
            gestalt.model.lowerLimit = LowerLimit(it)
            markAsDirty()
          }
        }
        configurableDouble("upper limit", gestalt.model.upperLimit.limit) {
          onChange {
            gestalt.model.upperLimit = UpperLimit(it)
            markAsDirty()
          }
        }

        configurableBoolean("Detected Edges", gestalt.model::detectedEdgesVisible)
        configurableBoolean("Live Edges", gestalt.model::liveEdgesVisible)
        configurableBoolean("Teach Edges", gestalt.model::teachEdgesVisible)

        section("Line Style")

        configurableBoolean("Show Dots", gestalt.style::showDots)
        configurableBoolean("Show Lines", gestalt.style::showLines)
      }
    }
  }
}
