package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layers.barchart.DebugCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.layers.barchart.StackedBarsPainter
import com.meistercharts.algorithms.layers.barchart.createAxisLayer
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.style
import com.meistercharts.model.Insets
import com.meistercharts.model.Orientation
import com.meistercharts.model.Side
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.TextKey

class CategoryChartLayerStackedDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Category Chart : Stacked"

  //language=HTML
  override val description: String = "## Category Chart Layer with stacked bars"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      meistercharts {
        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.of(100.0))
        }

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          val categories = listOf(
            Category(TextKey("2018", "2018")),
            Category(TextKey("2019", "2019")),
            Category(TextKey("2020", "2020"))
          )
          val series = listOf(
            DefaultSeries("Gorillas", listOf(10.0, 0.0, 12.0)),
            DefaultSeries("Giraffen", listOf(5.0, 3.0, 5.0)),
            DefaultSeries("Erdmännchen", listOf(20.0, 8.0, 4.0)),
            DefaultSeries("Zebras", listOf(4.0, 3.0, 3.0))
          )
          val model = com.meistercharts.algorithms.model.DefaultCategorySeriesModel(categories, series)

          val stackedBarsPainter = StackedBarsPainter().apply {
            stackedBarPaintable.data.valueRange = CategoryChartDemoHelper.calculateValueRangeForStackedBars(model)
          }

          val categoryAxisDefaultPainter = DefaultCategoryAxisLabelPainter()
          val categoryAxisPainters = listOf(categoryAxisDefaultPainter, DebugCategoryAxisLabelPainter())

          val categoryLayer = CategoryLayer(CategoryLayer.Data<CategorySeriesModel> { model }) {
            categoryPainter = stackedBarsPainter
          }
          layers.addLayer(categoryLayer)

          val categoryAxisLayer = categoryLayer.createAxisLayer {
            axisLabelPainter = categoryAxisDefaultPainter
          }
          layers.addLayer(categoryAxisLayer)

          configurableEnum("Orientation", categoryLayer.style::orientation, CategoryChartOrientation.values()) {
            onChange {
              categoryAxisLayer.style.side = when (it.categoryOrientation) {
                Orientation.Vertical   -> Side.Bottom
                Orientation.Horizontal -> Side.Left
              }
            }
          }

          configurableDouble("Min category size", categoryLayer.style.layoutCalculator.style::minCategorySize) {
            max = 1000.0
          }

          configurableDouble("Max category size", categoryLayer.style.layoutCalculator.style::maxCategorySize, 150.0) {
            max = 1000.0
          }

          configurableList("Axis Painter", categoryAxisLayer.style.axisLabelPainter, categoryAxisPainters) {
            this.converter = { painter ->
              painter::class.simpleName ?: painter.toString()
            }
            onChange {
              categoryAxisLayer.style.axisLabelPainter = it
              markAsDirty()
            }
          }

          declare {
            section("Bars Painter")
          }

          configurableDouble("Segments gap", stackedBarsPainter.stackedBarPaintable.style::segmentsGap) {
            min = -10.0
            max = 100.0
          }

          configurableDouble("Max bar size", stackedBarsPainter.style::maxBarSize) {
            max = 100.0
          }

          configurableBoolean("Show value labels", stackedBarsPainter.stackedBarPaintable.style::showValueLabels)

          configurableDouble("Max label width", stackedBarsPainter.stackedBarPaintable.style::maxValueLabelWidth, 200.0) {
            max = 500.0
          }

          configurableInt("Value labels decimal places") {
            max = 15
            min = 0
            value = 0

            onChange {
              stackedBarsPainter.stackedBarPaintable.style.valueLabelFormat = decimalFormat(it)
              markAsDirty()
            }
          }

          configurableFont("Value labels font", stackedBarsPainter.stackedBarPaintable.style.valueLabelFont) {
            onChange {
              stackedBarsPainter.stackedBarPaintable.style.valueLabelFont = it
              markAsDirty()
            }
          }

          configurableColorPickerNullable("Value labels color", stackedBarsPainter.stackedBarPaintable.style::valueLabelColor) {
          }

          configurableDouble("Value labels gap Horizontal", stackedBarsPainter.stackedBarPaintable.style::valueLabelGapHorizontal) {
            max = 100.0
          }
          configurableDouble("Value labels gap Vertical", stackedBarsPainter.stackedBarPaintable.style::valueLabelGapVertical) {
            max = 100.0
          }

          configurableEnum("Value labels anchor direction", stackedBarsPainter.stackedBarPaintable.style::valueLabelAnchorDirection, enumValues())

          declare {
            section("Category Axis")
          }

          configurableFont("Category axis font", categoryAxisLayer.style::tickFont) {
          }

        }
      }
    }
  }

}
