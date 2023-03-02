package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.axis.AxisOrientationX
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.SeriesIndex
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.LayerSupport
import com.meistercharts.canvas.debug
import com.meistercharts.charts.BarChartGroupedGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableValueRange
import com.meistercharts.demo.section
import com.meistercharts.model.Direction
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

class BarValueLabelManualDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "Bar chart with value labels (manual placement)"

  //language=HTML
  override val description: String = """
    <h2>Bar chart with value labels</h2>
    """

  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      val gestalt = BarChartGroupedGestalt()
      gestalt.prepareSimpleBarChart()
      gestalt.style.valueRange = ValueRange.linear(-25.0, 25.0)

      var manuallyOverwrittenValue = 30.0

      val values = listOf(
        99.9999, //replaced by seriesValueAt2
        99.9999, //replaced by seriesValueAt2 (negative!)
        0.0,
        0.1,
        -0.1,
        0.5,
        -0.5,
        1.5,
        -1.5,
        10.0,
        -10.0,
        20.0,
        -20.0,
        24.0,
        -24.0,
        24.8,
        -24.8,
      )


      //values.size categories - each with exactly one value

      gestalt.configuration.categorySeriesModel = object : CategorySeriesModel {
        override val numberOfCategories: Int = values.size
        override val numberOfSeries: Int = 1
        override fun valueAt(categoryIndex: CategoryIndex, seriesIndex: SeriesIndex): Double {
          if (categoryIndex == CategoryIndex.zero) {
            return manuallyOverwrittenValue
          }
          if (categoryIndex == CategoryIndex.one) {
            return -manuallyOverwrittenValue
          }

          return values[categoryIndex.value]
        }

        override fun categoryNameAt(categoryIndex: CategoryIndex, textService: TextService, i18nConfiguration: I18nConfiguration): String {
          return "Cat: $categoryIndex"
        }
      }

      meistercharts {
        gestalt.configure(this)

        configure {
          configurableDouble("1st bar value", manuallyOverwrittenValue) {
            min = -10.0
            max = +30.0
            onChange {
              manuallyOverwrittenValue = it
              markAsDirty()
            }
          }

          configurableValueRange("Value-range", gestalt.style::valueRange)

          configurableInsetsSeparate("Content viewport margin", gestalt::contentViewportMargin) {}
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

          val anchorDirectionProvider = object : GroupedBarsPainter.ValueLabelAnchorDirectionProvider {
            var directionInBarToAnchor: Direction = Direction.TopCenter
            var anchorDirection: Direction = Direction.BottomLeft

            override fun directionInBarToAnchorPointHorizontal(
              axisOrientation: AxisOrientationX, barValue: Double, barLabel: String, barLabelWidth: Double, barSize: Double, anchorGapHorizontal: @Zoomed Double, anchorGapVertical: @Zoomed Double,
            ): Direction {
              return directionInBarToAnchor
            }

            override fun directionInBarToAnchorPointVertical(
              axisOrientation: AxisOrientationY, barValue: Double, barLabel: String, barLabelWidth: Double, barSize: Double, anchorGapHorizontal: @Zoomed Double, anchorGapVertical: @Zoomed Double,
            ): Direction {
              return directionInBarToAnchor
            }

            override fun anchorDirectionHorizontal(
              axisOrientation: AxisOrientationX,
              barValue: Double,
              barLabel: String,
              barLabelWidth: Double,
              barSize: Double,
              anchorX: Double,
              anchorY: Double,
              anchorGapHorizontal: Double,
              anchorGapVertical: Double,
              paintingContext: LayerPaintingContext
            ): Direction {
              return anchorDirection
            }

            override fun anchorDirectionVertical(
              axisOrientation: AxisOrientationY,
              barValue: Double,
              barLabel: String,
              barLabelWidth: Double,
              barSize: Double,
              anchorX: Double,
              anchorY: Double,
              anchorGapHorizontal: Double,
              anchorGapVertical: Double,
              paintingContext: LayerPaintingContext
            ): Direction {
              return anchorDirection
            }
          }
          gestalt.groupedBarsPainter.configuration.valueLabelAnchorDirectionProvider = anchorDirectionProvider

          section("Value labels")
          configurableEnum("direction in bar", anchorDirectionProvider::directionInBarToAnchor)
          configurableEnum("Anchor Direction", anchorDirectionProvider::anchorDirection)

          chartSupport.debug.set(DebugFeature.ShowAnchors, true)
        }
      }
    }
  }


  class Config(
    val callback: (layerSupport: LayerSupport, gestalt: BarChartGroupedGestalt) -> Unit,
  ) {
  }

}
