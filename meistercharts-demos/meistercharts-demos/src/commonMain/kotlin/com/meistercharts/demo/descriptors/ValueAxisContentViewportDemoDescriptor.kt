package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.charts.FitContentInViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.model.Insets

class ValueAxisContentViewportDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value Axis - Content Viewport"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Axis

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val valueAxisLayer = ValueAxisLayer("Da Title", ValueRange.default) {
          paintRange = AxisStyle.PaintRange.Continuous
        }

        val contentViewportGestalt = FitContentInViewportGestalt(Insets.all15)
        contentViewportGestalt.configure(this)

        configure {
          layers.addClearBackground()
          layers.addLayer(valueAxisLayer)
        }

        configurableInsetsSeparate("Content Viewport Margin", contentViewportGestalt.contentViewportMarginProperty)
      }
    }
  }
}
