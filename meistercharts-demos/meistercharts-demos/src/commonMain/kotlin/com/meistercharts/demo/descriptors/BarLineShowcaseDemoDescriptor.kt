package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.AxisStyle.PaintRange
import com.meistercharts.algorithms.layers.CategoryLanesLayer
import com.meistercharts.algorithms.layers.CategoryLinesLayer
import com.meistercharts.algorithms.layers.ConstantTicksProvider
import com.meistercharts.algorithms.layers.DomainRelativeGridLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter
import com.meistercharts.algorithms.layers.legend.LegendLayer
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.DefaultSeries
import com.meistercharts.algorithms.model.SeriesIndex
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.LinearGradient
import com.meistercharts.canvas.BorderRadius
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.paintable.SymbolAndTextKeyPaintable
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.style
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import com.meistercharts.model.Orientation
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import com.meistercharts.painter.CategoryPointPainter
import com.meistercharts.painter.CirclePointPainter
import com.meistercharts.painter.XyCategoryLinePainter
import com.meistercharts.provider.forTextKeys
import it.neckar.open.collections.fastMapDouble
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.asProvider1
import it.neckar.open.provider.DefaultDoublesProvider
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.SizedProvider2
import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.cached
import it.neckar.open.formatting.intFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.unit.other.px

/**
 */
class BarLineShowcaseDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Bar + Line Chart - Showcase"

  //language=HTML
  override val description: String = "## Bar and Line Chart"
  override val category: DemoCategory = DemoCategory.ShowCase

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        enableZoomAndTranslation = false

        val passpartout = Insets.of(75.0)
        zoomAndTranslationDefaults(FittingWithMargin(passpartout))

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize
          chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtBottom

          layers.addClearBackground()

          val valueRange = ValueRange.linear(0.0, 110.0)
          val values = listOf(59.8, 52.2, 65.7, 66.1, 78.9, 65.2, 68.5, 75.2, 91.3, 89.2, 98.7, 92.3)
          val valuesProvider = DefaultDoublesProvider(values)

          @px val minCategorySize = 15.0
          @px val maxCategorySize = 45.0
          @px val categoryGap = 28.0


          //Shows the category columns (complete height)
          layers.addLayer(CategoryLanesLayer(CategoryLanesLayer.Data(valuesProvider)) {
            this.valueRange = valueRange

            val defaultColumnFill = LinearGradient(Color.web("#ffffff"), Color.web("#eeeeee"))
            val lastColumnFill = LinearGradient(Color.web("#fffffb"), Color.web("#d7ccc8"))

            val defaultColumnBorderColor = Color.web("#eeeeee")
            val lastColumnBorderColor = Color.web("#d7ccc8")

            fill = MultiProvider { index ->
              if (index == valuesProvider.size() - 1) {
                lastColumnFill
              } else {
                defaultColumnFill
              }
            }

            stroke = MultiProvider { index ->
              if (index == valuesProvider.size() - 1) {
                lastColumnBorderColor
              } else {
                defaultColumnBorderColor
              }
            }

            this.centerLineStroke = MultiProvider.alwaysNull()
            layoutCalculator.style.minCategorySize = minCategorySize
            layoutCalculator.style.maxCategorySize = maxCategorySize
            layoutCalculator.style.gapSize = DoubleProvider { categoryGap }

            borderRadius = BorderRadius(5.0, 5.0, 0.0, 0.0)
          })

          //Draws the labels (for values and category axis)
          val categories = listOf(
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
          )
          val categoryModel = DefaultCategorySeriesModel(categories, listOf(DefaultSeries("mySeries", values)))
          val groupedBarsPainter = GroupedBarsPainter {
            this.valueRangeProvider = { valueRange }
            showBars = false
            valueLabelAnchorGapHorizontal = 15.0
            valueLabelAnchorGapVertical = 15.0
          }
          val categoryLayer = CategoryLayer<CategorySeriesModel>(CategoryLayer.Data<CategorySeriesModel> { categoryModel }) {
            categoryPainter = groupedBarsPainter
            orientation = CategoryChartOrientation.VerticalLeft
            layoutCalculator.style.minCategorySize = minCategorySize
            layoutCalculator.style.maxCategorySize = maxCategorySize
            layoutCalculator.style.gapSize = DoubleProvider { categoryGap }
          }
          layers.addLayer(categoryLayer)

          val categoryAxisLayer = CategoryAxisLayer(CategoryAxisLayer.Data(SizedProvider2.forTextKeys(categories.map { it.name }), layoutProvider = { categoryLayer.paintingVariables().layout })) {
            side = Side.Bottom
            size = passpartout.bottom
            tickLabelGap = 15.0
            hideAxisLine()
            hideTicks()
          }
          layers.addLayer(categoryAxisLayer)

          //The horizontal grid lines
          //Three layers added - one for each color
          val gridLinesPasspartout = passpartout.copy(left = passpartout.left - 10.0, right = passpartout.right - 10.0)
          layers.addLayer(DomainRelativeGridLayer(Orientation.Horizontal, valuesProvider = DoublesProvider.forValues(*doubleArrayOf(25.0).fastMapDouble { valueRange.toDomainRelative(it) })) {
            this.passpartout = gridLinesPasspartout
            lineStyles = LineStyle(color = Color("#ffdf80"), dashes = Dashes(4.0, 4.0), lineWidth = 1.0).asProvider1()
          })
          layers.addLayer(DomainRelativeGridLayer(Orientation.Horizontal, valuesProvider = DoublesProvider.forValues(*doubleArrayOf(0.0, 50.0).fastMapDouble { valueRange.toDomainRelative(it) })) {
            this.passpartout = gridLinesPasspartout
            lineStyles = LineStyle(color = Color("#d3d9de"), lineWidth = 1.0).asProvider1()
          })
          layers.addLayer(DomainRelativeGridLayer(Orientation.Horizontal, valuesProvider = DoublesProvider.forValues(*doubleArrayOf(75.0).fastMapDouble { valueRange.toDomainRelative(it) })) {
            this.passpartout = gridLinesPasspartout
            lineStyles = LineStyle(color = Color("#cde5cc"), dashes = Dashes(4.0, 4.0), lineWidth = 1.0).asProvider1()
          })
          layers.addLayer(DomainRelativeGridLayer(Orientation.Horizontal, valuesProvider = DoublesProvider.forValues(*doubleArrayOf(100.0).fastMapDouble { valueRange.toDomainRelative(it) })) {
            this.passpartout = gridLinesPasspartout
            lineStyles = LineStyle(color = Color("#cde5cc"), lineWidth = 1.0).asProvider1()
          })

          //The line connecting the dots + the dots
          val okPointFill = Color.web("#60ad5e")
          val okColor = Color.web("#005005")
          val errorColor = Color.web("#ffc107")

          @px val pointSize = 17.0

          val categoryPointPainter = object : CategoryPointPainter {
            val circlePointPainter = CirclePointPainter(snapXValues = false, snapYValues = false).apply {
              this.pointSize = pointSize
            }

            override fun paintPoint(gc: CanvasRenderingContext, x: Double, y: Double, categoryIndex: CategoryIndex, seriesIndex: SeriesIndex, value: Double) {
              if (value > 100.0 || value < 75.0) {
                circlePointPainter.fill = errorColor
                circlePointPainter.stroke = errorColor
              } else {
                circlePointPainter.fill = okPointFill
                circlePointPainter.stroke = okColor
              }
              circlePointPainter.paintPoint(gc, x, y)
            }
          }

          val xyCategoryLinePainter = XyCategoryLinePainter(snapXValues = false, snapYValues = false)

          val categoryLinesLayer = CategoryLinesLayer(CategoryLinesLayer.Data(categoryModel)) {
            this.valueRange = valueRange
            layoutCalculator.style.minCategorySize = minCategorySize
            layoutCalculator.style.maxCategorySize = maxCategorySize
            layoutCalculator.style.gapSize = DoubleProvider { categoryGap }

            pointPainters = MultiProvider.always(categoryPointPainter)
            linePainters = MultiProvider.always(xyCategoryLinePainter)
          }
          layers.addLayer(categoryLinesLayer)


          val ioEntry = SymbolAndTextKeyPaintable(
            CirclePointPainter(snapXValues = false, snapYValues = false).also {
              it.pointSize = pointSize
              it.fill = okPointFill
              it.stroke = okColor
            },
            TextKey.simple("Success")
          ) {
            textColor = Color.black
            gap = 7.0
          }

          val nioEntry = SymbolAndTextKeyPaintable(
            CirclePointPainter(snapXValues = false, snapYValues = false).also {
              it.pointSize = pointSize
              it.fill = errorColor
              it.stroke = errorColor
            },
            TextKey.simple("Failure")
          ) {
            textColor = Color.black
            gap = 7.0
          }

          //The legend at the top
          layers.addLayer(
            LegendLayer(SizedProvider.forList(listOf(ioEntry, nioEntry)), Orientation.Horizontal) {
              anchorDirection = Direction.TopCenter
              horizontalGap = 30.0
              verticalGap = 30.0
              entriesGap = 20.0
            })

          //The value axis
          layers.addLayer(
            ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { valueRange })) {
              paintRange = PaintRange.ContentArea
              size = passpartout.left - 10.0
              tickOrientation = Vicinity.Outside
              tickLabelColor = Color.black.asProvider()
              ticks = ConstantTicksProvider(doubleArrayOf(0.0, 25.0, 50.0, 75.0, 100.0))
              hideAxisLine()
              hideTicks()

              //Special format that adds a "%" at one value
              ticksFormat = object : NumberFormat {
                override fun format(value: Double, i18nConfiguration: I18nConfiguration): String {
                  if (value == 100.0) {
                    return "${intFormat.format(value, i18nConfiguration)}%"
                  }

                  return intFormat.format(value, i18nConfiguration)
                }

                override val precision: Double = intFormat.precision
              }.cached()
            }
          )
        }
      }
    }
  }
}
