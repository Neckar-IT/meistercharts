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
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.BorderRadius
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.style
import com.meistercharts.model.Insets
import com.meistercharts.model.Orientation
import com.meistercharts.model.Side
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.i18n.TextKey
import com.meistercharts.style.Palette

class BarChartStackedSickDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Bar Chart Stacked - SICK"

  //language=HTML
  override val description: String = "## Bar Chart Stacked"
  override val category: DemoCategory = DemoCategory.Automation

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      meistercharts {
        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.of(120.0, 100.0, 100.0, 100.0))
        }

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          //layers.addLayer(ContentAreaDebugLayer())

          val categories = listOf(
            Category(TextKey.simple("LMS 151")),
            Category(TextKey.simple("LMS 511")),
            Category(TextKey.simple("OS 3")),
            Category(TextKey.simple("TDC-E")),
            Category(TextKey.simple("Weather station")),
            Category(TextKey.simple("Test"))
          )
          val series = listOf(
            DefaultSeries("OK", listOf(34.0, 47.0, 19.0, 5.0, 0.0, 0.0)),
            DefaultSeries("Warning", listOf(7.0, 10.0, 5.0, 1.0, 0.0, 0.0)),
            DefaultSeries("Error", listOf(7.0, 5.0, 3.0, 0.0, 0.0, 0.0)),
            DefaultSeries("Unknown", listOf(0.0, 0.0, 0.0, 0.0, 2.0, 0.0)),
            DefaultSeries("Zeroes", listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
          )
          val model = com.meistercharts.algorithms.model.DefaultCategorySeriesModel(categories, series)

          val stackedBarsPainter = StackedBarsPainter().apply {
            stackedBarPaintable.data.valueRange = CategoryChartDemoHelper.calculateValueRangeForStackedBars(model)
            stackedBarPaintable.style.colorsProvider = MultiProvider.forListModulo(
              listOf(
                Palette.stateSuperior,
                Palette.stateWarning,
                Palette.stateError,
                Palette.stateOffline,
                Palette.stateNormal
              )
            )
            stackedBarPaintable.style.segmentsGap = 4.0
            stackedBarPaintable.style.segmentRadii = BorderRadius.of(1.0)
            stackedBarPaintable.style.showRemainderAsSegment = true
            stackedBarPaintable.style.remainderSegmentBackgroundColor = Color.transparent
            stackedBarPaintable.style.remainderSegmentBorderColor = Color("#C5CACC")
            stackedBarPaintable.style.remainderSegmentBorderLineWidth = 2.0
          }

          val categoryAxisDefaultPainter = DefaultCategoryAxisLabelPainter()
          val categoryAxisPainters = listOf(categoryAxisDefaultPainter, DebugCategoryAxisLabelPainter())

          val categoryLayer = CategoryLayer<CategorySeriesModel>(CategoryLayer.Data<CategorySeriesModel> { model }) {
            categoryPainter = stackedBarsPainter
          }
          layers.addLayer(categoryLayer)

          val categoryAxisLayer = categoryLayer.createAxisLayer {
            axisLabelPainter = categoryAxisDefaultPainter
            size = 102.0
            axisLineWidth = 2.0
            lineColor = Color("#C5CACC").asProvider()
            hideTicks()
          }
          layers.addLayer(categoryAxisLayer)

          configurableEnum("Orientation", categoryLayer.style::orientation, CategoryChartOrientation.values()) {
            onChange {
              categoryAxisLayer.style.side = when (it.categoryOrientation) {
                Orientation.Vertical -> Side.Bottom
                Orientation.Horizontal -> Side.Left
              }
              markAsDirty()
            }
          }

          configurableDouble("Min category size", categoryLayer.style.layoutCalculator.style::minCategorySize, {
            max = 1000.0
          })

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
            section("Grouped Bars Painter")
          }

          configurableDouble("Bar gap", stackedBarsPainter.stackedBarPaintable.style::segmentsGap) {
            min = -10.0
            max = 100.0
          }

          configurableBoolean("Show value labels", stackedBarsPainter.stackedBarPaintable.style::showValueLabels) {
          }

          configurableFont("Value labels font", stackedBarsPainter.stackedBarPaintable.style::valueLabelFont) {
          }

          configurableColorPickerNullable("Value labels color", stackedBarsPainter.stackedBarPaintable.style::valueLabelColor) {
          }

          configurableDouble("Value labels gap Horizontal", stackedBarsPainter.stackedBarPaintable.style::valueLabelGapHorizontal) {
            max = 100.0
          }
          configurableDouble("Value labels gap Vertical", stackedBarsPainter.stackedBarPaintable.style::valueLabelGapVertical) {
            max = 100.0
          }

          configurableEnum("Value labels anchor direction", stackedBarsPainter.stackedBarPaintable.style::valueLabelAnchorDirection, enumValues()) {
          }

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
