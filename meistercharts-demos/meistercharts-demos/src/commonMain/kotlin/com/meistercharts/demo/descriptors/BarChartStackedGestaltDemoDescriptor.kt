package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.BorderRadius
import com.meistercharts.charts.BarChartStackedGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableColorPickerNullable
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDecimals
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.section
import com.meistercharts.demo.style
import it.neckar.open.provider.MultiProvider
import it.neckar.open.i18n.TextKey
import com.meistercharts.style.Palette

class BarChartStackedGestaltDemoDescriptor : ChartingDemoDescriptor<(gestalt: BarChartStackedGestalt) -> Unit> {

  override val name: String = "Bar Chart Stacked"

  //language=HTML
  override val description: String = "Bar Chart Stacked"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val predefinedConfigurations: List<PredefinedConfiguration<(gestalt: BarChartStackedGestalt) -> Unit>> = listOf(
    PredefinedConfiguration(
      { _ ->
      },
      "default (vertical)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.style.applyAxisTitleOnTop(40.0)
        gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "MyValue Axis Title" }
        gestalt.style.applyValueAxisVisible()
      },
      "default (vertical) Title on top"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.valueAxisLayer.style.titleProvider = { _, _ -> "MyValue Axis Title" }
        gestalt.style.applyValueAxisVisible()
      },
      "default (vertical) with Value Axis"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.style.applyHorizontalConfiguration()
        gestalt.style.applyAxisTitleOnTop(40.0)
        gestalt.categoryAxisLayer.style.titleProvider = { _, _ -> "MyValue Category Title" }
      },
      "default (horizontal) Title on top"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.style.applyHorizontalConfiguration()
      },
      "default (horizontal)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.style.applyHorizontalConfiguration()
        gestalt.style.applyValueAxisVisible()
      },
      "default (horizontal) - with value axis"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.data.categorySeriesModel = createSampleCategoryModel()
        gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange = ValueRange.linear(-10.0, 14.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.style.colorsProvider = MultiProvider.forListModulo(Palette.stateColors)
      },
      "positive + negative values (vertical)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.data.categorySeriesModel = createSampleCategoryModel()
        gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange = ValueRange.linear(-10.0, 14.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.style.colorsProvider = MultiProvider.forListModulo(Palette.stateColors)
        gestalt.style.applyHorizontalConfiguration()
      },
      "positive + negative values (horizontal)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.data.categorySeriesModel = createSampleCategoryModelManySmallValues()
        gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange = ValueRange.linear(-10.0, 10.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.style.colorsProvider = MultiProvider.forListModulo(Palette.stateColors)
        gestalt.style.applyHorizontalConfiguration()
      },
      "many positive + negative values (horizontal)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.data.categorySeriesModel = createSampleNegativeCategoryModel()
        gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange = ValueRange.linear(-24.0, 0.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.style.colorsProvider = MultiProvider.forListModulo(Palette.stateColors)
      },
      "only negative values (vertical)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.data.categorySeriesModel = createSampleNegativeCategoryModel()
        gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange = ValueRange.linear(-24.0, 0.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.style.colorsProvider = MultiProvider.forListModulo(Palette.stateColors)
        gestalt.style.applyHorizontalConfiguration()
      },
      "only negative values (horizontal)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.data.categorySeriesModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("Product 1")),
            Category(TextKey.simple("Product 2")),
            Category(TextKey.simple("Product 3")),
            Category(TextKey.simple("Product 4")),
            Category(TextKey.simple("Product 5")),
          ),
          listOf(
            DefaultSeries("1", listOf(0.0, 0.0, 0.0, 0.0, 0.0)),
            DefaultSeries("2", listOf(0.0, 0.0, 0.0, 0.0, 0.0)),
            DefaultSeries("3", listOf(0.0, 0.0, 0.0, 0.0, 0.0)),
            DefaultSeries("4", listOf(0.0, 0.0, 0.0, 0.0, 0.0)),
          )
        )

        gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange = ValueRange.linear(-24.0, 0.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.style.colorsProvider = MultiProvider.forListModulo(Palette.stateColors)
        gestalt.style.applyHorizontalConfiguration()
      },
      "only remainders (horizontal)"
    ),
    PredefinedConfiguration(
      { gestalt ->
        gestalt.data.categorySeriesModel = DefaultCategorySeriesModel(
          listOf(
            Category(TextKey.simple("Product 1")),
            Category(TextKey.simple("Product 2")),
            Category(TextKey.simple("Product 3")),
            Category(TextKey.simple("Product 4")),
            Category(TextKey.simple("Product 5")),
          ),
          listOf(
            DefaultSeries("1", listOf(0.0, 10.0, -10.0, 10.0, 10.0)),
            DefaultSeries("2", listOf(0.0, 0.0, 0.0, -0.0, 1.0)),
            DefaultSeries("3", listOf(0.0, 0.0, 0.0, 0.0, -5.0)),
            DefaultSeries("4", listOf(0.0, 0.0, 0.0, 0.0, -5.0)),
          )
        )

        gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange = ValueRange.linear(-10.0, 20.0)
        gestalt.stackedBarsPainter.stackedBarPaintable.style.colorsProvider = MultiProvider.forListModulo(Palette.stateColors)
      },
      "All combinations (Vertical)"
    ),
  )

  private fun createSampleCategoryModel(): DefaultCategorySeriesModel {
    return DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Product 1")),
        Category(TextKey.simple("Product 2")),
        Category(TextKey.simple("Product 3")),
        Category(TextKey.simple("Product 4")),
        Category(TextKey.simple("Product 5")),
      ),
      listOf(
        DefaultSeries("1", listOf(-10.0, 7.0, 10.0, -5.0, 1.0)),
        DefaultSeries("2", listOf(7.0, -10.0, -5.0, 1.0, 0.0)),
        DefaultSeries("3", listOf(7.0, 5.0, -3.0, 0.0, 0.0)),
        DefaultSeries("4", listOf(0.0, 0.0, 0.0, 0.0, -2.0)),
      )
    )
  }

  /**
   * Creates many values in the range from -10 to +10
   */
  private fun createSampleCategoryModelManySmallValues(): DefaultCategorySeriesModel {
    return DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Product 1")),
        Category(TextKey.simple("Product 2")),
        Category(TextKey.simple("Product 3")),
        Category(TextKey.simple("Product 4")),
        Category(TextKey.simple("Product 5")),
      ),

      buildList {
        val valuesPositive: List<@Domain Double> = buildList {
          for (i in 0 until 5) {
            add((i + 1) / 50.0)
          }
        }
        val valuesNegative: List<@Domain Double> = buildList {
          for (i in 0 until 5) {
            add(-(i + 1) / 50.0)
          }
        }

        //Create 20 series until 100 is reached
        for (i in 0 until 100) {
          //Add 5 data points to each series (one for every category)
          add(DefaultSeries("$i", valuesPositive))
          add(DefaultSeries("-$i", valuesNegative))
        }
      }
    )
  }

  private fun createSampleNegativeCategoryModel(): DefaultCategorySeriesModel {
    return DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Product 1")),
        Category(TextKey.simple("Product 2")),
        Category(TextKey.simple("Product 3")),
        Category(TextKey.simple("Product 4")),
        Category(TextKey.simple("Product 5")),
      ),
      listOf(
        DefaultSeries("1", listOf(-10.0, -7.0, -10.0, -5.0, -1.0)),
        DefaultSeries("2", listOf(-7.0, -10.0, -5.0, -1.0, 0.0)),
        DefaultSeries("3", listOf(-7.0, -5.0, -3.0, 0.0, 0.0)),
        DefaultSeries("4", listOf(0.0, 0.0, 0.0, 0.0, -2.0)),
      )
    )
  }

  override fun createDemo(configuration: PredefinedConfiguration<(gestalt: BarChartStackedGestalt) -> Unit>?): ChartingDemo {
    require(configuration != null) { "Config required" }

    return ChartingDemo {

      val gestalt = BarChartStackedGestalt()
      configuration.payload(gestalt)

      meistercharts {
        gestalt.configure(this)

        configure {

          configurableDecimals(property = gestalt.stackedBarsPainter.stackedBarPaintable.style::valueLabelFormat)

          configurableDouble("Min category size", gestalt.categoryLayer.style.layoutCalculator.style::minCategorySize) {
            max = 1000.0
          }

          configurableDouble("Max category size", gestalt.categoryLayer.style.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 1000.0
          }

          declare {
            button("Horizontal orientation") {
              gestalt.style.applyHorizontalConfiguration()
              markAsDirty()
            }
          }

          declare {
            button("Vertical orientation") {
              gestalt.style.applyVerticalConfiguration()
              markAsDirty()
            }
          }

          configurableDouble("Value range start", gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange.start) {
            min = -100.0
            max = 100.0
            onChange {
              gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange = gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange.withStart(it)
              markAsDirty()
            }
          }

          configurableDouble("Value range end", gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange.end) {
            min = -100.0
            max = 100.0
            onChange {
              gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange = gestalt.stackedBarsPainter.stackedBarPaintable.data.valueRange.withEnd(it)
              markAsDirty()
            }
          }

          configurableDouble("Segments gap", gestalt.stackedBarsPainter.stackedBarPaintable.style::segmentsGap) {
            min = 0.0
            max = 10.0
          }

          configurableBoolean("Grid-0", gestalt.style::showGrid) { }

          configurableDouble("Segment radii", gestalt.stackedBarsPainter.stackedBarPaintable.style.segmentRadii.topLeft) {
            min = 0.0
            max = 10.0
            onChange {
              gestalt.stackedBarsPainter.stackedBarPaintable.style.segmentRadii = BorderRadius.of(it)
              markAsDirty()
            }
          }

          section("Remainder")
          configurableBoolean("Show as segment", gestalt.stackedBarsPainter.stackedBarPaintable.style::showRemainderAsSegment) { }
          configurableColorPickerNullable("Background", gestalt.stackedBarsPainter.stackedBarPaintable.style::remainderSegmentBackgroundColor) { }
          configurableColorPicker("Border color", gestalt.stackedBarsPainter.stackedBarPaintable.style::remainderSegmentBorderColor) { }
          configurableDouble("Border width", gestalt.stackedBarsPainter.stackedBarPaintable.style::remainderSegmentBorderLineWidth) { max = 10.0 }

          section("Border")
          configurableBoolean("Show", gestalt.stackedBarsPainter.stackedBarPaintable.style::showBorder)
          configurableColorPicker("Border color", gestalt.stackedBarsPainter.stackedBarPaintable.style::borderColor) { }
          configurableDouble("Border width", gestalt.stackedBarsPainter.stackedBarPaintable.style::borderLineWidth) { max = 10.0 }

          configurableFont("Axis tick font", gestalt.valueAxisLayer.style.tickFont) {
            onChange {
              gestalt.style.applyAxisTickFont(it)
              markAsDirty()
            }
          }

          configurableFont("Axis title font", gestalt.valueAxisLayer.style.titleFont) {
            onChange {
              gestalt.style.applyAxisTitleFont(it)
              markAsDirty()
            }
          }

          configurableColorPickerProvider("Axis line color", gestalt.categoryAxisLayer.style::lineColor) {
          }

          configurableDouble("Axis line width", gestalt.categoryAxisLayer.style::axisLineWidth) {
            max = 10.0
          }

          configurableFont("Value label font", gestalt.stackedBarsPainter.stackedBarPaintable.style.valueLabelFont) {
            onChange {
              gestalt.style.applyValueLabelFont(it)
              markAsDirty()
            }
          }

          configurableColorPickerNullable("Value label color", gestalt.stackedBarsPainter.stackedBarPaintable.style::valueLabelColor)


          configurableInsetsSeparate("Content Viewport Margin", gestalt::contentViewportMargin)
        }
      }
    }
  }
}
