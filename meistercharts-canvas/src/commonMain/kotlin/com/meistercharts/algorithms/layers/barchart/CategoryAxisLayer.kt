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
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.CategoryLinesLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.textService
import com.meistercharts.color.Color
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.createCategoryLabelsProvider
import com.meistercharts.model.category.valueAt
import com.meistercharts.provider.SizedLabelsProvider
import it.neckar.geometry.Direction
import it.neckar.geometry.Orientation
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.trace
import it.neckar.open.provider.MultiProvider

/**
 * Paints the category axis
 */
class CategoryAxisLayer(
  override val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractAxisLayer() {

  constructor(
    labelsProvider: SizedLabelsProvider,
    layoutProvider: () -> EquisizedBoxLayout?,
    additionalConfiguration: Configuration.() -> Unit = {},
  ): this(Configuration(labelsProvider, layoutProvider), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

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
      logger.trace("Calculate painting variables - Window size: ${paintingContext.chartState.windowSize.format()}")

      reset()

      //Do not paint anything if there is no layout
      categoryLayout = (configuration.layoutProvider() ?: return)

      calculateTickFontMetrics(paintingContext, configuration)
      calculateTitle(paintingContext, configuration)
      calculateAxisStartEnd(paintingContext, configuration)

      calculateTickLabelsMaxWidth(configuration)

      calculateLocations(paintingContext, configuration)

      logger.trace("Finished Calculate painting variables - layout: $categoryLayout")
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val categoryLayout = paintingVariables.categoryLayout

    logger.trace { "Painting category axis - current category layout: $categoryLayout" }

    if (categoryLayout.isEmpty() || paintingVariables.axisLength == 0.0) {
      //Skip painting - no axis available
      return
    }

    configuration.axisLabelPainter.layout(categoryLayout, this.configuration.labelsProvider, this.configuration.labelVisibleCondition)
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
   * @param paintingContext the layer painting context
   * @param direction in which direction lies the axis line
   */
  override fun paintTicksWithLabelsVertically(paintingContext: LayerPaintingContext, direction: Direction) {
    val categoryLayout: EquisizedBoxLayout = paintingVariables.categoryLayout

    val chartSupport = paintingContext.chartSupport
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    gc.font(configuration.tickFont)
    gc.lineWidth = configuration.tickLineWidth
    gc.strokeStyle(configuration.lineColor()) //for the ticks
    gc.fillStyle(configuration.tickLabelColor()) //for the labels

    for (categoryIndexAsInt in 0 until configuration.labelsProvider.size(chartSupport.textService, chartSupport.i18nConfiguration)) {
      val categoryIndex = CategoryIndex(categoryIndexAsInt)

      @Window val currentY = chartCalculator.zoomed2windowY(categoryLayout.calculateCenter(BoxIndex(categoryIndexAsInt)))

      if (currentY < paintingVariables.axisStart || currentY > paintingVariables.axisEnd) {
        // behind passpartout
        continue
      }

      configuration.categoryLabelColor?.let {
        gc.fillStyle(it.valueAt(categoryIndex))
      }

      // the tick
      if (configuration.tickLength > 0.0 && configuration.tickLineWidth > 0.0) {
        when (direction) {
          Direction.CenterLeft -> gc.strokeLine(-configuration.tickLabelGap - configuration.tickLength, currentY, -configuration.tickLabelGap, currentY)
          Direction.CenterRight -> gc.strokeLine(configuration.tickLabelGap, currentY, configuration.tickLabelGap + configuration.tickLength, currentY)
          else -> throw IllegalArgumentException("Unsupported direction: $direction")
        }
      }

      //Calculate the label that is shown
      val label: String? = if (configuration.labelVisibleCondition.isLabelVisible(categoryIndex, categoryLayout.numberOfBoxes, categoryLayout.boxSize)) {
        configuration.labelsProvider.valueAt(categoryIndex.value, paintingContext.chartSupport.textService, paintingContext.i18nConfiguration)
      } else {
        null
      }

      paintingContext.gc.saved {
        configuration.axisLabelPainter.paint(
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
   * @param paintingContext the layer painting context
   * @param direction in which direction lies the axis line
   */
  override fun paintTicksWithLabelsHorizontally(paintingContext: LayerPaintingContext, direction: Direction) {
    logger.trace { "paintTicksWithLabelsHorizontally" }

    val categoryLayout: EquisizedBoxLayout = paintingVariables.categoryLayout

    val gc = paintingContext.gc
    val chartSupport = paintingContext.chartSupport
    val chartCalculator = paintingContext.chartCalculator

    gc.font(configuration.tickFont)
    gc.lineWidth = configuration.tickLineWidth
    gc.strokeStyle(configuration.lineColor()) //for the ticks
    gc.fillStyle(configuration.tickLabelColor()) //for the labels

    for (categoryIndexAsInt in 0 until configuration.labelsProvider.size(chartSupport.textService, chartSupport.i18nConfiguration)) {
      val categoryIndex: CategoryIndex = CategoryIndex(categoryIndexAsInt)

      @Window val currentX = chartCalculator.zoomed2windowX(categoryLayout.calculateCenter(BoxIndex(categoryIndexAsInt)))

      if (currentX < paintingVariables.axisStart || currentX > paintingVariables.axisEnd) {
        // behind passpartout
        logger.trace { "Skipping category $categoryIndex because it is outside of the axis" }
        continue
      }

      configuration.categoryLabelColor?.let {
        gc.fillStyle(it.valueAt(categoryIndex))
      }

      // the tick
      if (configuration.tickLength > 0.0 && configuration.tickLineWidth > 0.0) {
        when (direction) {
          Direction.BottomCenter -> gc.strokeLine(currentX, configuration.tickLabelGap + configuration.tickLength, currentX, configuration.tickLabelGap)
          Direction.TopCenter -> gc.strokeLine(currentX, -configuration.tickLabelGap, currentX, -configuration.tickLabelGap - configuration.tickLength)
          else -> throw IllegalArgumentException("Unsupported direction: $direction")
        }
      }

      val label: String? = if (configuration.labelVisibleCondition.isLabelVisible(categoryIndex, categoryLayout.numberOfBoxes, categoryLayout.boxSize)) {
        configuration.labelsProvider.valueAt(categoryIndex.value, paintingContext.chartSupport.textService, paintingContext.i18nConfiguration)
      } else {
        null
      }

      paintingContext.gc.saved {
        configuration.axisLabelPainter.paint(
          paintingContext = paintingContext,
          x = currentX,
          y = 0.0,
          width = categoryLayout.boxSize,
          height = configuration.size - configuration.axisLineWidth - configuration.tickLength - configuration.tickLabelGap - paintingVariables.spaceForTitleIncludingGap,
          tickDirection = direction,
          label = label,
          categoryIndex = categoryIndex,
          categoryAxisOrientation = Orientation.Horizontal,
        )
      }
    }
  }

  @ConfigurationDsl
  open class Configuration(
    /**
     * Provides the labels - for each category
     */
    var labelsProvider: SizedLabelsProvider,

    /**
     * Returns the layout of the segment
     */
    var layoutProvider: () -> EquisizedBoxLayout?,
  ) : AxisConfiguration() {
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

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer")
  }
}

/**
 * Creates a category axis layer that uses the layout from the given category layer
 */
fun CategoryLayer<*>.createAxisLayer(
  labelsProvider: SizedLabelsProvider = configuration.modelProvider().createCategoryLabelsProvider(),
  styleConfiguration: CategoryAxisLayer.Configuration.() -> Unit = {},
): CategoryAxisLayer {
  return CategoryAxisLayer(labelsProvider, { paintingVariables().layout }) {
    styleConfiguration()
  }
}


/**
 * Creates a category axis layer that uses the layout of this [CategoryLinesLayer]
 */
fun CategoryLinesLayer.createAxisLayer(labelsProvider: SizedLabelsProvider = configuration.categorySeriesModel.createCategoryLabelsProvider(), styleConfiguration: CategoryAxisLayer.Configuration.() -> Unit = {}): CategoryAxisLayer {
  return CategoryAxisLayer(labelsProvider, { paintingVariables().layout }) {
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

