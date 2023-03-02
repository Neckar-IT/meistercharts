package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.charts.bullet.BulletChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInsetsSeparate
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.formatting.decimalFormat

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class BulletChartGestaltDemoDescriptor : ChartingDemoDescriptor<(gestalt: BulletChartGestalt) -> Unit> {
  override val name: String = "Bullet Chart"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val predefinedConfigurations: List<PredefinedConfiguration<(gestalt: BulletChartGestalt) -> Unit>> = listOf(
    PredefinedConfiguration({ gestalt ->
      gestalt.configuration.categoryNames = MultiProvider {
        if (it == 2) {
          ""
        } else {
          "Category $it"
        }
      }

    }, "Vertical"),
    PredefinedConfiguration({ gestalt ->
      gestalt.configuration.applyAxisTitleOnTop()
      gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "MyValue Axis Title" }
      gestalt.configuration.thresholdValues = DoublesProvider.forValues(12.0, 37.0)
      gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ ->
        listOf("Threshold value", decimalFormat.format(gestalt.configuration.thresholdValues.valueAt(index)))
      }
    }, "Vertical - title top"),

    PredefinedConfiguration({ gestalt ->
      gestalt.configuration.currentValues = DoublesProvider.forValues(5.0, Double.NaN, 55.0, Double.NaN, 22.4)
      gestalt.configuration.areaValueRanges = MultiProvider.modulo(
        ValueRange.linear(-15.0, 25.0),
        null,
        null,
        ValueRange.linear(11.0, 55.0),
        null,
      )
      gestalt.configuration.applyAxisTitleOnTop()
      gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "MyValue Axis Title" }
      gestalt.configuration.thresholdValues = DoublesProvider.forValues(12.0, Double.NaN)
      gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ ->
        listOf("Threshold value", decimalFormat.format(gestalt.configuration.thresholdValues.valueAt(index)))
      }
    }, "Vertical - invalid values"),

    PredefinedConfiguration({ gestalt ->
      gestalt.configuration.applyHorizontalConfiguration()
      gestalt.configuration.thresholdValues = DoublesProvider.forValues(12.0, 37.0)
      gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ ->
        listOf("Threshold value", decimalFormat.format(gestalt.configuration.thresholdValues.valueAt(index)))
      }
    }, "Horizontal"),

    PredefinedConfiguration({ gestalt ->
      gestalt.configuration.applyHorizontalConfiguration()
      gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "MyValue Axis Title" }
      gestalt.categoryAxisLayer.style.titleProvider = { _, _ -> "My Category Title" }
      gestalt.configuration.thresholdValues = DoublesProvider.forValues(12.0, 37.0)
      gestalt.configuration.thresholdLabels = MultiProvider1 { index, _ ->
        listOf("Threshold value", decimalFormat.format(gestalt.configuration.thresholdValues.valueAt(index)))
      }
      gestalt.configuration.applyAxisTitleOnTop()
    }, "Horizontal - title top"),


    PredefinedConfiguration({ gestalt ->
      gestalt.configuration.applyValueRange(ValueRange.logarithmic(0.1, 100.0))
    }, "Vertical - Logarithmic"),

    PredefinedConfiguration({ gestalt ->
      gestalt.configuration.applyHorizontalConfiguration()
      gestalt.configuration.applyValueRange(ValueRange.logarithmic(0.1, 100.0))
    }, "Horizontal - Logarithmic"),

    PredefinedConfiguration({ gestalt ->
      gestalt.configuration.applyValueRange(ValueRange.logarithmic(1000.0, 10_000.0))
    }, "Vertical - Logarithmic 1000-10000"),
  )

  override fun createDemo(configuration: PredefinedConfiguration<(gestalt: BulletChartGestalt) -> Unit>?): ChartingDemo {
    requireNotNull(configuration)

    return ChartingDemo {
      meistercharts {
        val gestalt = BulletChartGestalt().also {
          it.configuration.thresholdValues = DoublesProvider.forDoubles(25.0, 50.0, 75.0)
          it.configuration.thresholdLabels = MultiProvider1 { index, _ ->
            val value = it.configuration.thresholdValues.valueAt(index)
            listOf("Value:", decimalFormat.format(value))
          }
        }

        configuration.payload.invoke(gestalt)
        gestalt.configure(this)

        configurableDouble("Min Category Gap", gestalt.configuration::minCategoryGap) {
          max = 200.0
        }
        configurableDouble("Max Category Gap", gestalt.configuration::maxCategoryGap) {
          max = 300.0
        }

        configurableDouble("Current Value Indicator Size", gestalt.bulletChartPainter.configuration::currentValueIndicatorSize) {
          max = 100.0
        }
        configurableDouble("Area Size", gestalt.bulletChartPainter.configuration::barSize) {
          max = 150.0
        }

        configurableDouble("Current Line width", gestalt.bulletChartPainter.configuration::currentValueIndicatorWidth) {
          max = 10.0
        }
        configurableDouble("Current Line outline", gestalt.bulletChartPainter.configuration::currentValueIndicatorOutlineWidth) {
          max = 10.0
        }

        configurableInsetsSeparate("Content Viewport Margin", gestalt::contentViewportMargin)
      }
    }
  }
}
