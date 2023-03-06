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
package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.layers.AxisPaintingVariables
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.CategoryLinesLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.createCategoryLabelsProvider
import com.meistercharts.algorithms.model.valueAt
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.textService
import com.meistercharts.model.Direction
import com.meistercharts.model.Orientation
import com.meistercharts.provider.SizedLabelsProvider
import it.neckar.open.provider.MultiProvider

/**
 * Paints the category axis
 */
class CategoryAxisLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {},
) : AbstractAxisLayer() {
  override val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paintingVariables(): AxisPaintingVariables {
    return paintingVariables
  }

  //Painting variable - stores intermediate results required for painting
  private val paintingVariables = object : CategoryAxisPaintingVariablesImpl() {
    /**
     * Calculates all painting variables
     */
    override fun calculate(paintingContext: LayerPaintingContext) {
      reset()

      //Do not paint anything if there is no layout
      categoryLayout = (data.layoutProvider() ?: return)

      calculateTickFontMetrics(paintingContext, style)
      calculateTitle(paintingContext, style)
      calculateAxisStartEnd(paintingContext, style)

      calculateTickLabelsMaxWidth(style)

      calculateLocations(paintingContext, style)
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val categoryLayout = paintingVariables.categoryLayout

    if (categoryLayout.isEmpty() || paintingVariables.axisLength == 0.0) {
      //Skip painting - no axis available
      return
    }

    style.axisLabelPainter.layout(categoryLayout, this.data.labelsProvider, this.style.labelVisibleCondition)
    super.paint(paintingContext)
  }

  /**
   * Returns the last layout that has been used.
   * Might return [EquisizedBoxLayout.empty] if no layout is provided
   */
  val layout: EquisizedBoxLayout
    get() {
      return paintingVariables.categoryLayout
    }

  /**
   * Paints the ticks and the categories on a vertical axis.
   * Must be called with the graphics context translated to the *edge* of the tick category labels
   *
   * Supports both directions of painting!
   * ```
   * ├
   * ├
   * ├
   * ├
   *```
   * @param direction in which direction lies the axis line
   */
  override fun paintTicksWithLabelsVertically(paintingContext: LayerPaintingContext, direction: Direction) {
    val categoryLayout: EquisizedBoxLayout = paintingVariables.categoryLayout

    val chartSupport = paintingContext.chartSupport
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    gc.font(style.tickFont)
    gc.lineWidth = style.tickLineWidth
    gc.strokeStyle(style.lineColor()) //for the ticks
    gc.fillStyle(style.tickLabelColor()) //for the labels

    for (categoryIndexAsInt in 0 until data.labelsProvider.size(chartSupport.textService, chartSupport.i18nConfiguration)) {
      val categoryIndex = CategoryIndex(categoryIndexAsInt)

      @Window val currentY = chartCalculator.zoomed2windowY(categoryLayout.calculateCenter(BoxIndex(categoryIndexAsInt)))

      if (currentY < paintingVariables.axisStart || currentY > paintingVariables.axisEnd) {
        // behind passpartout
        continue
      }

      style.categoryLabelColor?.let {
        gc.fillStyle(it.valueAt(categoryIndex))
      }

      // the tick
      if (style.tickLength > 0.0 && style.tickLineWidth > 0.0) {
        when (direction) {
          Direction.CenterLeft -> gc.strokeLine(-style.tickLabelGap - style.tickLength, currentY, -style.tickLabelGap, currentY)
          Direction.CenterRight -> gc.strokeLine(style.tickLabelGap, currentY, style.tickLabelGap + style.tickLength, currentY)
          else -> throw IllegalArgumentException("Unsupported direction: $direction")
        }
      }

      //Calculate the label that is shown
      val label: String? = if (style.labelVisibleCondition.isLabelVisible(categoryIndex, categoryLayout.numberOfBoxes, categoryLayout.boxSize)) {
        data.labelsProvider.valueAt(categoryIndex.value, paintingContext.chartSupport.textService, paintingContext.i18nConfiguration)
      } else {
        null
      }

      paintingContext.gc.saved {
        style.axisLabelPainter.paint(
          paintingContext = paintingContext,
          x = 0.0,
          y = currentY,
          width = paintingVariables.tickValueLabelMaxWidth,
          height = categoryLayout.boxSize,
          tickDirection = direction,
          label = label,
          categoryIndex = categoryIndex,
          categoryAxisOrientation = Orientation.Vertical,
        )
      }
    }
  }

  /**
   * Paints the ticks and the categories on a horizontal axis.
   * Must be called with the graphics context translated to the *edge* of the tick category labels
   *
   * Supports both directions of painting!
   * ```
   * ┬┬┬┬┬┬┬┬┬┬┬┬
   *```
   * @param direction in which direction lies the axis line
   */
  override fun paintTicksWithLabelsHorizontally(paintingContext: LayerPaintingContext, direction: Direction) {
    val categoryLayout: EquisizedBoxLayout = paintingVariables.categoryLayout

    val gc = paintingContext.gc
    val chartSupport = paintingContext.chartSupport
    val chartCalculator = paintingContext.chartCalculator

    gc.font(style.tickFont)
    gc.lineWidth = style.tickLineWidth
    gc.strokeStyle(style.lineColor()) //for the ticks
    gc.fillStyle(style.tickLabelColor()) //for the labels

    for (categoryIndexAsInt in 0 until data.labelsProvider.size(chartSupport.textService, chartSupport.i18nConfiguration)) {
      val categoryIndex: CategoryIndex = CategoryIndex(categoryIndexAsInt)

      @Window val currentX = chartCalculator.zoomed2windowX(categoryLayout.calculateCenter(BoxIndex(categoryIndexAsInt)))

      if (currentX < paintingVariables.axisStart || currentX > paintingVariables.axisEnd) {
        // behind passpartout
        continue
      }

      style.categoryLabelColor?.let {
        gc.fillStyle(it.valueAt(categoryIndex))
      }

      // the tick
      if (style.tickLength > 0.0 && style.tickLineWidth > 0.0) {
        when (direction) {
          Direction.BottomCenter -> gc.strokeLine(currentX, style.tickLabelGap + style.tickLength, currentX, style.tickLabelGap)
          Direction.TopCenter -> gc.strokeLine(currentX, -style.tickLabelGap, currentX, -style.tickLabelGap - style.tickLength)
          else -> throw IllegalArgumentException("Unsupported direction: $direction")
        }
      }

      val label: String? = if (style.labelVisibleCondition.isLabelVisible(categoryIndex, categoryLayout.numberOfBoxes, categoryLayout.boxSize)) {
        data.labelsProvider.valueAt(categoryIndex.value, paintingContext.chartSupport.textService, paintingContext.i18nConfiguration)
      } else {
        null
      }

      paintingContext.gc.saved {
        style.axisLabelPainter.paint(
          paintingContext = paintingContext,
          x = currentX,
          y = 0.0,
          width = categoryLayout.boxSize,
          height = style.size - style.axisLineWidth - style.tickLength - style.tickLabelGap - paintingVariables.spaceForTitleIncludingGap,
          tickDirection = direction,
          label = label,
          categoryIndex = categoryIndex,
          categoryAxisOrientation = Orientation.Horizontal,
        )
      }
    }
  }

  class Data(
    /**
     * Provides the labels - for each category
     */
    var labelsProvider: SizedLabelsProvider,

    /**
     * Returns the layout of the segment
     */
    var layoutProvider: () -> EquisizedBoxLayout?,
  )

  open class Style : AxisStyle() {
    /**
     * The painter that is used to paint the axis
     */
    var axisLabelPainter: CategoryAxisLabelPainter = DefaultCategoryAxisLabelPainter()

    /**
     * Determines whether a label is visible
     */
    var labelVisibleCondition: LabelVisibleCondition = LabelVisibleCondition.all

    /**
     * Provides the color for each tick label; this overrides [tickLabelColor] if set.
     */
    var categoryLabelColor: MultiProvider<CategoryIndex, Color>? = null
  }
}

/**
 * Creates a category axis layer that uses the layout from the given category layer
 */
fun CategoryLayer<*>.createAxisLayer(
  labelsProvider: SizedLabelsProvider = data.modelProvider().createCategoryLabelsProvider(),
  styleConfiguration: CategoryAxisLayer.Style.() -> Unit = {},
): CategoryAxisLayer {
  return CategoryAxisLayer(CategoryAxisLayer.Data(labelsProvider) { paintingVariables().layout }) {
    styleConfiguration()
  }
}


/**
 * Creates a category axis layer that uses the layout of this [CategoryLinesLayer]
 */
fun CategoryLinesLayer.createAxisLayer(labelsProvider: SizedLabelsProvider = data.categorySeriesModel.createCategoryLabelsProvider(), styleConfiguration: CategoryAxisLayer.Style.() -> Unit = {}): CategoryAxisLayer {
  return CategoryAxisLayer(CategoryAxisLayer.Data(labelsProvider) { paintingVariables().layout }) {
    styleConfiguration()
  }
}

/**
 * Can be used to only show some labels
 */
fun interface LabelVisibleCondition {
  /**
   * Returns true if the provided label index is visible
   */
  fun isLabelVisible(labelIndex: CategoryIndex, labelCount: Int, segmentSize: @Zoomed Double): Boolean


  companion object {
    /**
     * All labels are visible
     */
    val all: LabelVisibleCondition = LabelVisibleCondition { _, _, _ -> true }

    /**
     * Only every nth element is visible
     */
    fun modulo(
      factor: Int = 10,
      expectedResult: Int = factor - 1,
    ): LabelVisibleCondition {
      return LabelVisibleCondition { labelIndex, _, _ -> labelIndex.value % factor == expectedResult }
    }
  }
}

