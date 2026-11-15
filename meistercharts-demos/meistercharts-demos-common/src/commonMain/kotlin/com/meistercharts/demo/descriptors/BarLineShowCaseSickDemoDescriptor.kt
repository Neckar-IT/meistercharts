/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.range.ValueRange
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.zoom.FittingWithMargin
import com.meistercharts.algorithms.layers.AxisConfiguration.PaintRange
import com.meistercharts.algorithms.layers.CategoryLanesLayer
import com.meistercharts.algorithms.layers.CategoryLinesLayer
import com.meistercharts.algorithms.layers.axis.ConstantTicksProvider
import com.meistercharts.algorithms.layers.DomainRelativeGridLayer
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layers.barchart.GroupedBarsPainter
import com.meistercharts.algorithms.layers.legend.LegendLayer
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.model.category.Category
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.DefaultCategorySeriesModel
import com.meistercharts.model.category.DefaultSeries
import com.meistercharts.model.category.SeriesIndex
import com.meistercharts.color.Color
import com.meistercharts.color.LinearGradient
import com.meistercharts.model.BorderRadius
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.paintable.SymbolAndTextKeyPaintable
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.style
import it.neckar.geometry.Direction
import com.meistercharts.model.Insets
import it.neckar.geometry.Orientation
import it.neckar.geometry.Side
import com.meistercharts.model.Vicinity
import com.meistercharts.painter.CategoryPointPainter
import com.meistercharts.painter.CirclePointPainter
import com.meistercharts.painter.XyCategoryLinePainter
import com.meistercharts.provider.forTextKeys
import com.meistercharts.style.Palette
import it.neckar.open.collections.fastMapDouble
import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.cached
import it.neckar.open.formatting.intFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.asProvider1
import it.neckar.open.provider.DefaultDoublesProvider
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.SizedProvider2
import it.neckar.open.provider.asSizedProvider
import it.neckar.open.unit.other.px
import it.neckar.open.annotations.Hot

/**
 */
class BarLineShowCaseSickDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("8e185660-cc11-4fc8-9a5c-d391f896d5e6")
  override val name: String = "Bar + Line Chart - SICK"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable


  //language=HTML
  override val description: String = "A combined bar and line chart in the SICK design"
  override val category: DemoCategory = DemoCategory.Automation

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        enableZoomAndTranslation = false

        val passpartout = Insets.of(75.0)
        zoomAndTranslationDefaults(FittingWithMargin(passpartout))

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize
          chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtBottom

          layers.addClearBackground()

          val valueRange = ValueRange.linear(0.0, 107.0)
          val values = listOf(95.0, 96.0, 98.0, 77.0, 87.0, 91.0, 57.0)
          val valuesProvider = DefaultDoublesProvider(values)

          @px val minCategorySize = 50.0
          @px val maxCategorySize = 100.0
          @px val categoryGap = 25.0

          //Shows the category columns (complete height)
          layers.addLayer(CategoryLanesLayer(valuesProvider) {
            this.valueRange = valueRange

            val defaultColumnFill = LinearGradient(Color.web("#fbfdfd"), Color.web("#f6f8f9"))
            val lastColumnFill = LinearGradient(Color.web("#fefefe"), Color.web("#e5f1f8"))

            val defaultColumnStroke = Color.web("#e5e9eb")
            val lastColumnStroke = Color.web("#cde9f8")

            fill = MultiProvider { index ->
              if (index == valuesProvider.size() - 1) {
                lastColumnFill
              } else {
                defaultColumnFill
              }
            }

            stroke = MultiProvider { index ->
              if (index == valuesProvider.size() - 1) {
                lastColumnStroke
              } else {
                defaultColumnStroke
              }
            }

            centerLineStroke = MultiProvider.always(Color.web("#e5f1f8").asProvider())
            layoutCalculator.style.minCategorySize = minCategorySize
            layoutCalculator.style.maxCategorySize = maxCategorySize
            layoutCalculator.style.gapSize = DoubleProvider { categoryGap }

            borderRadius = BorderRadius(7.0, 7.0, 0.0, 0.0)
          })

          //Draws the labels (for values and category axis)
          val categories = listOf(
            Category(TextKey.simple("12.")),
            Category(TextKey.simple("13.")),
            Category(TextKey.simple("14.")),
            Category(TextKey.simple("15.")),
            Category(TextKey.simple("16.")),
            Category(TextKey.simple("17.")),
            Category(TextKey.simple("18."))
          )
          val categoryModel = DefaultCategorySeriesModel(categories, listOf(DefaultSeries("mySeries", values)))
          val groupedBarsPainter = GroupedBarsPainter {
            this.valueRangeProvider = { valueRange }
            showBars = false
            valueLabelAnchorGapHorizontal = 20.0
            valueLabelAnchorGapVertical = 20.0
          }
          val categoryLayer = CategoryLayer(CategoryLayer.Configuration<CategorySeriesModel>() { categoryModel }) {
            categoryPainter = groupedBarsPainter
            orientation = CategoryChartOrientation.VerticalLeft
            layoutCalculator.style.minCategorySize = minCategorySize
            layoutCalculator.style.maxCategorySize = maxCategorySize
            layoutCalculator.style.gapSize = DoubleProvider { categoryGap }
          }
          layers.addLayer(categoryLayer)

          val categoryAxisLayer = CategoryAxisLayer(SizedProvider2.forTextKeys(categories.map { it.name }), layoutProvider = { categoryLayer.paintingVariables().layout }) {
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
          layers.addLayer(DomainRelativeGridLayer(Orientation.Horizontal, valuesProvider = DoublesProvider.forValues(*doubleArrayOf(0.0, 50.0).fastMapDouble { valueRange.toDomainRelative(it) })) {
            this.passpartout = gridLinesPasspartout
            lineStyles = LineStyle(color = Color("#eff1f3").asProvider(), lineWidth = 1.0).asProvider1()
          })
          layers.addLayer(DomainRelativeGridLayer(Orientation.Horizontal, valuesProvider = DoublesProvider.forValues(*doubleArrayOf(90.0).fastMapDouble { valueRange.toDomainRelative(it) })) {
            this.passpartout = gridLinesPasspartout
            lineStyles = LineStyle(color = Color("#f58d99").asProvider(), dashes = Dashes(4.0, 4.0)).asProvider1()
          })

          layers.addLayer(DomainRelativeGridLayer(Orientation.Horizontal, valuesProvider = DoublesProvider.forValues(*doubleArrayOf(100.0).fastMapDouble { valueRange.toDomainRelative(it) })) {
            this.passpartout = gridLinesPasspartout
            lineStyles = LineStyle(color = Color("#c5e2a8").asProvider(), lineWidth = 1.0).asProvider1()
          })

          //The line connecting the dots + the dots
          val okPointFill = Color.web("#eff7e7").asProvider()
          val okColor = Palette.stateSuperior
          val errorColor = Palette.stateError

          @px val pointSize = 17.0

          val categoryPointPainter = object : CategoryPointPainter {
            val circlePointPainter = CirclePointPainter(snapXValues = false, snapYValues = false).apply {
              this.pointSize = pointSize
            }

            @Hot
            override fun paintPoint(gc: CanvasRenderingContext, x: Double, y: Double, categoryIndex: CategoryIndex, seriesIndex: SeriesIndex, value: Double) {
              if (value > 100.0 || value < 90.0) {
                circlePointPainter.fill = errorColor
                circlePointPainter.stroke = errorColor
              } else {
                circlePointPainter.fill = okPointFill
                circlePointPainter.stroke = okColor
              }
              circlePointPainter.paintPoint(gc, x, y)
            }
          }

          val categoryLinePainter = XyCategoryLinePainter(snapXValues = false, snapYValues = false)

          val categoryLinesLayer = CategoryLinesLayer(categoryModel) {
            this.valueRange = valueRange
            layoutCalculator.style.minCategorySize = minCategorySize
            layoutCalculator.style.maxCategorySize = maxCategorySize
            layoutCalculator.style.gapSize = DoubleProvider { categoryGap }

            pointPainters = MultiProvider.always(categoryPointPainter)

            linePainters = MultiProvider.always(categoryLinePainter)
          }
          layers.addLayer(categoryLinesLayer)


          val ioEntry = SymbolAndTextKeyPaintable(
            CirclePointPainter(snapXValues = false, snapYValues = false).also {
              it.pointSize = pointSize
              it.fill = okPointFill
              it.stroke = okColor
            },
            TextKey("IO", "IO")
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
            TextKey("NIO", "NIO")
          ) {
            textColor = Color.black
            gap = 7.0
          }

          //The legend at the top
          layers.addLayer(
            LegendLayer(listOf(ioEntry, nioEntry).asSizedProvider(), Orientation.Horizontal) {
              anchorDirection = Direction.TopCenter
              horizontalGap = 30.0
              verticalGap = 30.0
              entriesGap = 20.0
            })

          //The value axis
          layers.addLayer(
            ValueAxisLayer(ValueAxisLayer.Configuration(valueRangeProvider = { valueRange })) {
              paintRange = PaintRange.ContentArea
              size = passpartout.left - 10.0
              tickOrientation = Vicinity.Outside
              tickLabelColor = Color.black
              ticks = ConstantTicksProvider(doubleArrayOf(0.0, 50.0, 90.0, 100.0))
              hideAxisLine()
              hideTicks()

              //Special format that adds a "%" at one value
              ticksFormat = object : NumberFormat {
                override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
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
