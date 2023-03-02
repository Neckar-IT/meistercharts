package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.charts.BarChartStackedGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.model.Insets
import it.neckar.open.i18n.TextKey

/**
 *
 */
class StackedBarChartClippingDemoDescriptor : ChartingDemoDescriptor<Any> {
  override val name: String = "Stacked Bar Chart Clipping"
  override val category: DemoCategory = DemoCategory.BugHunting

  override fun createDemo(configuration: PredefinedConfiguration<Any>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val categoryModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("Product A")),
            Category(TextKey.simple("Product B")),
            Category(TextKey.simple("Product C")),
            Category(TextKey.simple("Product D")),
            Category(TextKey.simple("Product E")),
          ),
          listOf(
            DefaultSeries("1", listOf(0.9, 1.1, 7.8, 0.5, 1.0)),
            DefaultSeries("2", listOf(1.1, 2.5, 3.6, 4.7, 0.0)),
            DefaultSeries("3", listOf(8.2, 3.5, 4.6, 5.7, 0.0)),
            DefaultSeries("4", listOf(0.5, 4.5, 5.6, 6.7, 2.0)),
          )
        )

        val gestalt = BarChartStackedGestalt(data = BarChartStackedGestalt.Data(categoryModel))
        gestalt.configure(this)
        gestalt.style.valueRange = ValueRange.linear(1.0, 10.0)
        gestalt.contentViewportMargin = Insets.of(100.0)

        configure {
          layers.addLayer(ContentAreaDebugLayer())
        }

        configurableInsetsSeparate("content viewport margin", gestalt::contentViewportMargin)
      }
    }
  }
}
