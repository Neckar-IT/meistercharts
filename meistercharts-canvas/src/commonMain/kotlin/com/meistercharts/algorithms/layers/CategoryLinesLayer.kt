/**
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
package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.barchart.CategoryChartOrientation
import com.meistercharts.algorithms.layers.barchart.CategoryPaintingVariables
import com.meistercharts.algorithms.layers.crosswire.LabelPlacementStrategy
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.SeriesIndex
import com.meistercharts.algorithms.model.valueAt
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.whatsAt
import com.meistercharts.design.Theme
import com.meistercharts.model.Coordinates
import com.meistercharts.painter.CategoryLinePainter
import com.meistercharts.painter.CategoryPointPainter
import com.meistercharts.painter.CircleCategoryPointPainter
import com.meistercharts.painter.XyCategoryLinePainter
import com.meistercharts.whatsat.ResultElementType
import com.meistercharts.whatsat.WhatsAtResultElement
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.MultiProvider

/**
 * Category chart that uses lines to visualize the categories
 */
class CategoryLinesLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {},
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paintingVariables(): CategoryPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : CategoryPaintingVariables {
    /**
     * The current layout that has been calculated
     */
    override var layout: EquisizedBoxLayout = EquisizedBoxLayout.empty

    override fun calculate(paintingContext: LayerPaintingContext) {
      layout = style.layoutCalculator.calculateLayout(paintingContext, data.categorySeriesModel.numberOfCategories, style.orientation)
    }
  }

  override fun initialize(paintingContext: LayerPaintingContext) {
    super.initialize(paintingContext)

    paintingContext.chartSupport.whatsAt.registerResolverAsFirst { where, precision, chartSupport ->
      val chartCalculator = chartSupport.chartCalculator

      @ContentArea val contentAreaX = chartCalculator.window2contentAreaX(where.x)

      val layout = paintingVariables.layout

      return@registerResolverAsFirst buildList<WhatsAtResultElement<*>> {
        //Find the segment/category
        layout.boxIndexFor(contentAreaX)?.let { foundSegmentIndex ->

          val segmentCenterX = layout.calculateCenter(foundSegmentIndex)

          //Add the category itself
          add(
            WhatsAtResultElement(
              ResultElementType.category,
              label = "category $foundSegmentIndex",
              value = foundSegmentIndex.value,
              data = foundSegmentIndex
            )
          )

          val categoryIndex = CategoryIndex(foundSegmentIndex.value)

          //Find the exact point
          data.categorySeriesModel.numberOfSeries.fastFor { seriesIndexAsInt ->
            val seriesIndex = SeriesIndex(seriesIndexAsInt)
            @Domain val valueForPoint = data.categorySeriesModel.valueAt(categoryIndex, seriesIndex)
            if (valueForPoint.isNaN()) {
              return@fastFor
            }

            @Window val y = chartCalculator.domain2windowY(valueForPoint, style.valueRange)

            add(
              WhatsAtResultElement(
                ResultElementType.pointOfInterest,
                location = Coordinates(segmentCenterX, y),
                label = "Point $valueForPoint",
                value = valueForPoint,
                data = "A Point!"
              )
            )
          }
        }
      }
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    paintLines(paintingContext)
    paintPoints(paintingContext)
  }

  private fun paintLines(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    for (seriesIndexAsInt in 0 until data.categorySeriesModel.numberOfSeries) {
      val seriesIndex = SeriesIndex(seriesIndexAsInt)

      val linePainter = style.linePainters.valueAt(seriesIndex)
      gc.saved {
        style.lineStyles.valueAt(seriesIndexAsInt).apply(gc)
        linePainter.begin(gc)
        foreachCategory(chartCalculator, seriesIndex) { centerX, y, categoryIndex, value ->
          if (centerX.isFinite() && y.isFinite()) {
            linePainter.addCoordinate(gc, centerX, y, categoryIndex, seriesIndex, value)
          } else {
            //Finish without the last number and paint the remaining stuff later
            linePainter.finish(gc)
            linePainter.begin(gc)
          }
        }
        linePainter.finish(gc)
      }
    }
  }

  private fun paintPoints(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    for (seriesIndexAsInt in 0 until data.categorySeriesModel.numberOfSeries) {
      val seriesIndex = SeriesIndex(seriesIndexAsInt)

      style.pointPainters.valueAt(seriesIndex)?.let { categoryPointPainter ->
        foreachCategory(chartCalculator, seriesIndex) { centerX, y, categoryIndex, value ->
          if (centerX.isFinite() && y.isFinite()) {
            gc.saved {
              categoryPointPainter.paintPoint(gc, centerX, y, categoryIndex, seriesIndex, value)
            }
          }
        }
      }
    }
  }

  private inline fun foreachCategory(
    chartCalculator: ChartCalculator,
    seriesIndex: SeriesIndex,
    callback: (centerX: @Window Double, y: @Window Double, categoryIndex: CategoryIndex, value: @Domain Double) -> Unit,
  ) {
    val layout = paintingVariables.layout
    for (categoryIndexAsInt in 0 until data.categorySeriesModel.numberOfCategories) {
      //Translate to the start of the content area
      @Window val centerX = chartCalculator.zoomed2windowX(layout.calculateCenter(BoxIndex(categoryIndexAsInt)))

      val categoryIndex = CategoryIndex(categoryIndexAsInt)
      @Domain val value = data.categorySeriesModel.valueAt(categoryIndex, seriesIndex)
      if (value.isNaN()) {
        continue
      }

      @Window val y = chartCalculator.domainRelative2windowY(this.style.valueRange.toDomainRelative(value))
      callback(centerX, y, categoryIndex, value)
    }
  }

  class Data(
    /**
     * The model
     */
    var categorySeriesModel: CategorySeriesModel,
  )

  @ConfigurationDsl
  open class Style {
    /**
     * Provides the layout
     */
    var layoutCalculator: CategoryLayouter = DefaultCategoryLayouter()

    /**
     * The orientation of the categories lanes
     */
    var orientation: CategoryChartOrientation = CategoryChartOrientation.VerticalLeft

    /**
     * The [ValueRange] for the layer
     */
    var valueRange: @Domain ValueRange = ValueRange.default

    /**
     * Provides painters for points of a certain series
     */
    var pointPainters: MultiProvider<SeriesIndex, CategoryPointPainter?> = MultiProvider.always(CircleCategoryPointPainter(snapXValues = false, snapYValues = false))

    /**
     * Provides painters for lines of a certain series
     */
    var linePainters: MultiProvider<SeriesIndex, CategoryLinePainter> = MultiProvider.always(XyCategoryLinePainter(snapXValues = false, snapYValues = false))

    /**
     * Provides styles for lines of a certain series.
     * Please also set crossWireLabelBoxStyles in the corresponding cross wire layer.
     */
    var lineStyles: MultiProvider<SeriesIndex, LineStyle> = MultiProvider {
      LineStyle(color = Theme.chartColors().valueAt(it), lineWidth = 1.0)
    }

    /**
     * The index of the category that is active (e.g. mouse over).
     * This information can be used to highlight this category
     */
    var activeCategoryIndex: CategoryIndex? = null

    /**
     * Where to place the labels
     */
    var valueLabelPlacementStrategy: LabelPlacementStrategy = LabelPlacementStrategy.preferOnRightSide { 150.0 }
  }
}
