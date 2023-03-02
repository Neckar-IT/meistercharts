package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.LinearValueRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.withMaxNumberOfTicks
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.algorithms.model.Series
import com.meistercharts.algorithms.model.SeriesIndex
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.SplineLinePainter
import com.meistercharts.annotations.Domain
import com.meistercharts.charts.CategoryLineChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableInt
import com.meistercharts.painter.DotCategoryPointPainter
import com.meistercharts.painter.XyCategoryLinePainter
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.kotlin.lang.fastMap
import it.neckar.open.kotlin.lang.randomNormal
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider1
import it.neckar.open.i18n.TextKey
import com.meistercharts.style.BoxStyle

class CategoryLineChartGestaltDemoDescriptor : ChartingDemoDescriptor<(gestalt: CategoryLineChartGestalt) -> Unit> {
  override val name: String = "Category Line Chart"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val predefinedConfigurations: List<PredefinedConfiguration<(gestalt: CategoryLineChartGestalt) -> Unit>> = listOf(
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(10.0, 12.0)
      },
      "linear"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.applyValueAxisTitleOnTop()
        gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "MyValue Axis Title" }
      },
      "linear - Title on top"
    ),
    PredefinedConfiguration(
      { gestalt ->

        gestalt.configuration.valueRange = ValueRange.linear(0.0, 20.0)

        gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("Jan")),
            Category(TextKey.simple("Feb")),
            Category(TextKey.simple("Mar")),
            Category(TextKey.simple("Apr")),
            Category(TextKey.simple("May")),
            Category(TextKey.simple("Jun")),
            Category(TextKey.simple("Jul")),
            Category(TextKey.simple("Aug")),
            Category(TextKey.simple("Sep")),
            Category(TextKey.simple("Oct")),
            Category(TextKey.simple("Nov")),
            Category(TextKey.simple("Dec"))
          ),
          listOf(
            DefaultSeries("1", listOf(11.0, 0.1, 0.0, -0.1, 0.0001, 0.5, 19.9, 19.99999999, 20.0, 20.1, 19.5, 12.0))
          )
        )
      },
      "linear - min/max values"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("Monkeys")),
            Category(TextKey.simple("Giraffes")),
            Category(TextKey.simple("Parrots")),
            Category(TextKey.simple("")),
            Category(TextKey.simple("Tigers")),
          ),
          listOf(
            DefaultSeries("Chicago", listOf(10.0, 20.0, 30.0, 40.0, 50.0)),
            DefaultSeries("Paris", listOf(50.0, 10.0, 30.0, 80.0, 0.0)),
            DefaultSeries("Berlin", listOf(50.0, 100.0, 10.0, 10.0, 40.0)),
            DefaultSeries("Oslo", listOf(20.0, 50.0, 0.0, 100.0, 80.0)),
            DefaultSeries("Warsow", listOf(70.0, 60.0, 50.0, 40.0, 60.0)),
          )
        )
        val colors = listOf(Color.orangered, Color.blueviolet, Color.greenyellow, Color.limegreen, Color.chocolate)
        gestalt.categoryLinesLayer.style.lineStyles = MultiProvider.forListModulo(colors.map { color -> LineStyle(color) })
        gestalt.crossWireLabelsLayer.style.valueLabelBoxStyle = MultiProvider.forListModulo(colors.map { color -> BoxStyle(color) })
        gestalt.categoryLinesLayer.style.pointPainters = MultiProvider.forListModulo(colors.map { color -> DotCategoryPointPainter(snapXValues = false, snapYValues = false).apply { pointStylePainter.color = color } })
      },
      "multiple series"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.categoryLinesLayer.style.linePainters = MultiProvider.always(XyCategoryLinePainter(snapXValues = false, snapYValues = false, SplineLinePainter(false, false)))
      },
      "splines"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.valueRange = gestalt.configuration.valueRange.let {
          ValueRange.logarithmic(logarithmicRangeStart, it.end)
        }
        gestalt.valueAxisLayer.style.applyLogarithmicScale()
      },
      "logarithmic"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("1")),
            Category(TextKey.simple("2")),
            Category(TextKey.simple("3")),
            Category(TextKey.simple("4")),
            Category(TextKey.simple("5")),
            Category(TextKey.simple("6")),
            Category(TextKey.simple("7")),
            Category(TextKey.simple("8")),
            Category(TextKey.simple("9")),
          ),
          listOf(
            DefaultSeries("1", listOf(3000.0, 100.0, -10.0, 0.3, 0.0, 0.3, -10.0, 100.0, 3000.0))
          )
        )
        gestalt.configuration.thresholdValues = DoublesProvider.forValues(1900.0, 0.8)
        gestalt.configuration.thresholdLabels = MultiProvider1 { index, param1 ->
          when (index) {
            0 -> listOf("Max", "1.9K")
            1 -> listOf("Min", "0.8")
            else -> listOf("Unknown")
          }
        }
        gestalt.configuration.valueRange = ValueRange.logarithmic(0.1, 10000.0)
        gestalt.valueAxisLayer.style.applyLogarithmicScale()
      },
      "logarithmic with invalid values"
    ),
  )

  override fun createDemo(configuration: PredefinedConfiguration<(gestalt: CategoryLineChartGestalt) -> Unit>?): ChartingDemo {
    return ChartingDemo {
      val gestalt = CategoryLineChartGestalt()

      configuration?.payload?.invoke(gestalt)
      val defaultValueRange = gestalt.configuration.valueRange

      meistercharts {
        gestalt.configure(this)

        configure {

          configurableBoolean("Show values grid", gestalt.configuration::showValuesGrid)
          configurableBoolean("Show categories grid", gestalt.configuration::showCategoriesGrid)

          declare {
            button("Add line") {

              val categories = mutableListOf<Category>()
              val categoryModel = gestalt.configuration.categorySeriesModel as DefaultCategorySeriesModel

              for (categoryIndex in 0 until categoryModel.numberOfCategories) {
                categories.add(categoryModel.categoryAt(CategoryIndex(categoryIndex)))
              }
              val series = mutableListOf<Series>()
              for (seriesIndex in 0 until categoryModel.numberOfSeries) {
                series.add(categoryModel.seriesAt(SeriesIndex(seriesIndex)))
              }
              val values = mutableListOf<Double>()
              for (categoryIndex in 0 until categoryModel.numberOfCategories) {
                values.add(randomNormal((gestalt.configuration.valueRange as LinearValueRange).center(), (series.size) * 5.0))
              }
              series.add(DefaultSeries(series.size.toString(), values))
              gestalt.configuration.categorySeriesModel = DefaultCategorySeriesModel(categories, series)
              markAsDirty()
            }
          }

          configurableDouble("Min category size", gestalt.configuration::minCategorySize) {
            max = 200.0
          }

          configurableDouble("Max category size", gestalt.configuration::maxCategorySize, 150.0) {
            max = 200.0
          }

          configurableDouble("Category gap", gestalt.configuration::categoryGap) {
            max = 200.0
          }

          configurableEnum("Axis scale", if (defaultValueRange is LinearValueRange) ValueAxisScale.Linear else ValueAxisScale.Logarithmic, enumValues()) {
            onChange {
              when (it) {
                ValueAxisScale.Linear -> gestalt.configuration.valueRange = ValueRange.linear(defaultValueRange.start, defaultValueRange.end)
                ValueAxisScale.Logarithmic -> gestalt.configuration.valueRange = ValueRange.logarithmic(logarithmicRangeStart, defaultValueRange.end)
              }
              markAsDirty()
            }
          }

          val visibleLineIndices = 5.fastMap { lineIndex -> gestalt.configuration.lineIsVisible.valueAt(lineIndex) }.toMutableList()
          gestalt.configuration.lineIsVisible = MultiProvider.forListModulo(visibleLineIndices)
          visibleLineIndices.fastForEachIndexed { index, isVisible ->
            configurableBoolean("$index. line is visible") {
              value = isVisible
              onChange {
                visibleLineIndices[index] = it
                markAsDirty()
              }
            }
          }

          configurableInt("Max tick count") {
            min = 0
            max = 20
            value = 20
            onChange {
              gestalt.valueAxisLayer.style.ticks = gestalt.valueAxisLayer.style.ticks.withMaxNumberOfTicks(it)
              markAsDirty()
            }
          }

          configurableInsetsSeparate("Content Viewport Margin", gestalt::contentViewportMargin)

          configurableFont("Axis tick font", gestalt.valueAxisLayer.style.tickFont) {
            onChange {
              gestalt.applyAxisTickFont(it)
              markAsDirty()
            }
          }

          configurableFont("Axis title font", gestalt.valueAxisLayer.style.titleFont) {
            onChange {
              gestalt.applyAxisTitleFont(it)
              markAsDirty()
            }
          }

          configurableFont("Cross wire label font", gestalt.crossWireLabelsLayer.style::valueLabelFont) {
          }

        }
      }
    }
  }

  private enum class ValueAxisScale {
    Linear,
    Logarithmic
  }

  companion object {
    private const val logarithmicRangeStart: @Domain Double = 0.1
  }

}


