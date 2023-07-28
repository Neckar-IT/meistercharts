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

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.barchart.CategorySeriesModelColorsProvider.Companion.onlySeriesColorsProvider
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.BoxLayoutCalculator
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Snapped
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.AxisOrientationX
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.calc.ChartCalculator
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.text.CanvasStringShortener
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.LocationType
import com.meistercharts.canvas.forTranslationX
import com.meistercharts.canvas.forTranslationY
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.snapPhysicalTranslation
import com.meistercharts.charts.OverflowIndicatorPainter
import com.meistercharts.color.Color
import com.meistercharts.design.Theme
import it.neckar.geometry.Direction
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Orientation
import it.neckar.geometry.VerticalAlignment
import com.meistercharts.model.category.CategoryIndex
import com.meistercharts.model.category.CategorySeriesModel
import com.meistercharts.model.category.SeriesIndex
import com.meistercharts.model.category.valuesAt
import com.meistercharts.provider.BoxProvider1
import com.meistercharts.provider.ValueRangeProvider
import com.meistercharts.range.ValueRange
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.kotlin.lang.isNegative
import it.neckar.open.provider.fastForEachIndexed
import it.neckar.open.unit.other.px
import kotlin.math.max
import kotlin.math.min

/**
 * Paints bars that belong to a category as a group
 */
@Suppress("DuplicatedCode")
class GroupedBarsPainter(
  additionalConfiguration: Configuration.() -> Unit = {},
) : CategoryPainter<CategorySeriesModel> {

  val configuration: Configuration = Configuration().also(additionalConfiguration)

  override fun paintingVariables(): CategoryPainterPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : CategoryPainterPaintingVariables {
    /**
     * The layout for the groups
     */
    @Zoomed
    var layout: EquisizedBoxLayout = EquisizedBoxLayout.empty

    override val actualSize: @Zoomed Double
      get() = layout.usedSpace
  }

  override fun layout(paintingContext: LayerPaintingContext, categorySize: Double, categoryModel: CategorySeriesModel, categoryOrientation: Orientation) {
    paintingVariables.layout = configuration.layoutProvider(categorySize, categoryModel, categoryOrientation)
  }

  private val outOfBoundsIndicatorsSelection = OverflowIndicatorPainter.MutableOverflowIndicatorsSelection()

  /**
   * ```
   *  ┃     ┃
   *  ┃   ┃ ┃
   *  ┃ ┃ ┃ ┃
   *```
   */
  override fun paintCategoryVertical(
    paintingContext: LayerPaintingContext,
    categoryWidth: @Zoomed Double, categoryIndex: CategoryIndex,
    isLast: Boolean,
    categoryModel: CategorySeriesModel,
  ) {
    val valuesProvider = categoryModel.valuesAt(categoryIndex) //TODO necessary?

    if (valuesProvider.isEmpty()) {
      return
    }
    if (!configuration.showBars && !configuration.showValueLabel) {
      return
    }

    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator
    val snapConfiguration = paintingContext.snapConfiguration

    // We are currently translated to the center of the category. However, the bars
    // should be painted beginning at the left edge of the category. This offset ensures that.
    @Zoomed val offsetX = categoryWidth / 2.0

    with(chartCalculator) {
      val layout = paintingVariables.layout

      val valueRange = configuration.valueRangeProvider()

      //The start value for the bar
      val valueRangeBase = valueRange.base()
      @Window @px val barStartUnbound = domain2windowY(valueRangeBase, valueRange)
      @Window @px val barStart = coerceInViewportY(barStartUnbound)
      @Snapped val boxSizeSnapped = snapConfiguration.snapXSize(layout.boxSize)

      valuesProvider.fastForEachIndexed { index, barValue: @Domain Double ->
        @Window @px val barEndUnbound = domain2windowY(barValue, valueRange)

        outOfBoundsIndicatorsSelection.reset()
        outOfBoundsIndicatorsSelection.updateVertical(barStartUnbound, chartCalculator)
        outOfBoundsIndicatorsSelection.updateVertical(barEndUnbound, chartCalculator)

        //if the bar is invisible - because out of the value range, skip painting
        val barCompletelyOutOfValueRange = valueRange.start >= 0.0 && barValue <= valueRange.start //The bar value is lower than the displayed value range
          || valueRange.end <= 0.0 && barValue >= valueRange.end //The bar value is larger than the displayed value range

        @Window @px val barEnd = coerceInViewportY(barEndUnbound)
        @Zoomed val barHeight = snapConfiguration.snapYSize(barEnd - barStart)

        //The center of the bar
        @Window val center = layout.calculateCenter(BoxIndex(index)) - offsetX
        @Window val left = center - boxSizeSnapped / 2.0

        val barColor = configuration.colorsProvider.color(categoryIndex, SeriesIndex(index))
        if (configuration.showBars) {
          if (barCompletelyOutOfValueRange.not()) {
            gc.fill(barColor)

            gc.saved {
              gc.translate(left, barStart)
              if (snapConfiguration.snapX) {
                gc.snapPhysicalTranslation()
              }

              gc.fillRect(
                x = 0.0,
                y = 0.0,
                width = boxSizeSnapped,
                height = barHeight,
                xType = LocationType.Origin
              )
            }
          }

          gc.saved {
            gc.translate(center, 0.0)
            configuration.overflowIndicatorPainter?.paintIndicators(outOfBoundsIndicatorsSelection, paintingContext)
          }
        }

        if (configuration.showValueLabel) {
          //This instantiates a lambda! Which is ok and can not be avoided
          gc.delayed { gc ->
            gc.font(configuration.valueLabelFont)

            val valueText: String = configuration.valueLabelFormat.format(barValue, paintingContext.i18nConfiguration)
            val textWidth = gc.calculateTextWidth(valueText)

            val directionToAnchorPoint = configuration.valueLabelAnchorDirectionProvider.directionInBarToAnchorPointVertical(
              axisOrientation = chartState.axisOrientationY,
              barValue = barValue,
              barLabel = valueText,
              barLabelWidth = textWidth,
              barSize = barHeight,
              anchorGapHorizontal = configuration.valueLabelAnchorGapHorizontal,
              anchorGapVertical = configuration.valueLabelAnchorGapVertical,
            )

            @Window val anchorY = when (directionToAnchorPoint.verticalAlignment) {
              VerticalAlignment.Top -> min(barEnd, barStart)
              VerticalAlignment.Center -> (barEnd + barStart) / 2.0
              VerticalAlignment.Baseline -> (barEnd + barStart) / 2.0
              VerticalAlignment.Bottom -> max(barEnd, barStart)
            }

            @Window val anchorX = when (directionToAnchorPoint.horizontalAlignment) {
              HorizontalAlignment.Left -> left
              HorizontalAlignment.Right -> center + boxSizeSnapped / 2.0
              HorizontalAlignment.Center -> center
            }

            if (gc.debug[DebugFeature.ShowAnchors]) {
              gc.paintMark(anchorX, anchorY)
            }

            val anchorDirection: Direction = configuration.valueLabelAnchorDirectionProvider.anchorDirectionVertical(
              axisOrientation = chartState.axisOrientationY,
              barValue = barValue,
              barLabel = valueText,
              barLabelWidth = textWidth,
              anchorX = anchorX,
              anchorY = anchorY,
              anchorGapHorizontal = configuration.valueLabelAnchorGapHorizontal,
              anchorGapVertical = configuration.valueLabelAnchorGapVertical,
              paintingContext = paintingContext,
              barSize = barHeight,
            )
            paintBarValueLabel(paintingContext, gc, valueText, anchorX, anchorY, anchorDirection, barColor)
          }
        }
      }
    }

    gc.paintDelayed()
  }

  /**
   * ```
   * ━━━
   * ━━
   * ━━━━━━━
   * ━━━━
   *```
   */
  override fun paintCategoryHorizontal(
    paintingContext: LayerPaintingContext,
    categoryHeight: @Zoomed Double,
    categoryIndex: CategoryIndex,
    isLast: Boolean,
    categoryModel: CategorySeriesModel,
  ) {
    val valuesProvider = categoryModel.valuesAt(categoryIndex) //TODO necessary?

    if (valuesProvider.isEmpty()) {
      return
    }
    if (!configuration.showBars && !configuration.showValueLabel) {
      return
    }

    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator
    val snapConfiguration = paintingContext.snapConfiguration

    // We are currently translated to the center of the category. However, the bars
    // should be painted beginning at the top edge of the category. This offset ensures that.
    val offsetY = categoryHeight / 2.0

    with(chartCalculator) {
      val layout = paintingVariables.layout

      val valueRange = configuration.valueRangeProvider()

      //The start value for the bar (maybe in the middle of the value range, e.g. for -5;10)
      val valueRangeBase = valueRange.base()

      @Window @px val barStartUnbound = domain2windowX(valueRangeBase, valueRange)
      @Window @px val barStart = coerceInViewportX(barStartUnbound)
      @Snapped val boxSizeSnapped = snapConfiguration.snapYSize(layout.boxSize)

      valuesProvider.fastForEachIndexed { index, barValue: @Domain Double ->
        @Window @px val barEndUnbound = domain2windowX(barValue, valueRange)
        outOfBoundsIndicatorsSelection.reset()
        outOfBoundsIndicatorsSelection.updateHorizontal(barStartUnbound, chartCalculator)
        outOfBoundsIndicatorsSelection.updateHorizontal(barEndUnbound, chartCalculator)

        //The center of the bar
        @Window val center = layout.calculateCenter(BoxIndex(index)) - offsetY

        //if the bar is invisible - because out of the value range, skip painting
        val barCompletelyOutOfValueRange = valueRange.start >= 0.0 && barValue <= valueRange.start //The bar value is lower than the displayed value range
          || valueRange.end <= 0.0 && barValue >= valueRange.end //The bar value is larger than the displayed value range

        @Window @px val barEnd = coerceInViewportX(barEndUnbound)
        @Zoomed val barWidth = snapConfiguration.snapYSize(barEnd - barStart)

        @Window val top = center - boxSizeSnapped / 2.0

        val barColor = configuration.colorsProvider.color(categoryIndex, SeriesIndex(index))
        if (configuration.showBars) {
          if (barCompletelyOutOfValueRange.not()) {
            gc.fill(barColor)
            gc.saved {
              gc.translate(barStart, top)
              gc.snapPhysicalTranslation(snapConfiguration)

              gc.fillRect(
                x = 0.0, y = 0.0, width = barWidth, height = boxSizeSnapped, yType = LocationType.Origin
              )
            }
          }

          gc.saved {
            gc.translate(0.0, center)
            configuration.overflowIndicatorPainter?.paintIndicators(outOfBoundsIndicatorsSelection, paintingContext)
          }
        }

        if (configuration.showValueLabel) {
          gc.delayed { gc ->
            gc.font(configuration.valueLabelFont)

            val valueText: String = configuration.valueLabelFormat.format(barValue, paintingContext.i18nConfiguration)
            val textWidth = gc.calculateTextWidth(valueText)

            val directionToAnchorPoint = configuration.valueLabelAnchorDirectionProvider.directionInBarToAnchorPointHorizontal(
              axisOrientation = chartState.axisOrientationX,
              barValue = barValue,
              barLabel = valueText,
              barLabelWidth = textWidth,
              barSize = barWidth,
              anchorGapHorizontal = configuration.valueLabelAnchorGapHorizontal,
              anchorGapVertical = configuration.valueLabelAnchorGapVertical,
            )

            @Window val anchorY = when (directionToAnchorPoint.verticalAlignment) {
              VerticalAlignment.Top -> top
              VerticalAlignment.Center -> center
              VerticalAlignment.Baseline -> center
              VerticalAlignment.Bottom -> center + boxSizeSnapped / 2.0
            }

            @Window val anchorX = when (directionToAnchorPoint.horizontalAlignment) {
              HorizontalAlignment.Left -> min(barEnd, barStart)
              HorizontalAlignment.Center -> (barStart + barEnd) / 2.0
              HorizontalAlignment.Right -> max(barEnd, barStart)
            }

            if (gc.debug[DebugFeature.ShowAnchors]) {
              gc.paintMark(anchorX, anchorY)
            }

            val anchorDirection: Direction = configuration.valueLabelAnchorDirectionProvider.anchorDirectionHorizontal(
              axisOrientation = chartState.axisOrientationX,
              barValue = barValue,
              barLabel = valueText,
              barLabelWidth = textWidth,
              anchorX = anchorX,
              anchorY = anchorY,
              anchorGapHorizontal = configuration.valueLabelAnchorGapHorizontal,
              anchorGapVertical = configuration.valueLabelAnchorGapVertical,
              paintingContext = paintingContext,
              barSize = barWidth,
            )
            paintBarValueLabel(paintingContext, gc, valueText, anchorX, anchorY, anchorDirection, barColor)
          }
        }
      }
    }

    gc.paintDelayed()
  }

  private fun paintBarValueLabel(
    paintingContext: LayerPaintingContext,
    gc: CanvasRenderingContext,
    valueText: String,
    anchorX: @Window Double,
    anchorY: @Window Double,
    anchorDirection: Direction,
    barColor: Color,
  ) {

    val maxWidth: Double? = null
    val maxHeight: Double? = null

    configuration.valueLabelStrokeColor?.let { strokeColor ->
      gc.stroke(strokeColor)
      gc.lineWidth = 1.0
      gc.strokeText(
        text = valueText,
        x = anchorX,
        y = anchorY,
        anchorDirection = anchorDirection,
        gapHorizontal = configuration.valueLabelAnchorGapHorizontal,
        gapVertical = configuration.valueLabelAnchorGapVertical,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        stringShortener = configuration.valueLabelStringShortener
      )
    }

    val chartCalculator = paintingContext.chartCalculator

    val valueLabelBox = configuration.valueLabelBoxProvider

    val boxX: @Window Double = paintingContext.gc.forTranslationX(valueLabelBox.getX(chartCalculator))
    val boxY: @Window Double = paintingContext.gc.forTranslationY(valueLabelBox.getY(chartCalculator))
    val boxWidth: @Zoomed Double = valueLabelBox.getWidth(chartCalculator)
    val boxHeight: @Zoomed Double = valueLabelBox.getHeight(chartCalculator)

    gc.fill(configuration.valueLabelColor ?: barColor)
    gc.fillTextWithin(
      text = valueText,
      x = anchorX,
      y = anchorY,
      anchorDirection = anchorDirection,
      gapHorizontal = configuration.valueLabelAnchorGapHorizontal,
      gapVertical = configuration.valueLabelAnchorGapVertical,
      boxX = boxX,
      boxY = boxY,
      boxWidth = boxWidth,
      boxHeight = boxHeight,
      stringShortener = configuration.valueLabelStringShortener
    )
  }

  @ConfigurationDsl
  open class Configuration {
    /**
     * The value range to be used for all categories painted by the painter
     */
    var valueRangeProvider: ValueRangeProvider = { ValueRange.default }

    /**
     * Provides the color for a bar.
     */
    var colorsProvider: CategorySeriesModelColorsProvider = onlySeriesColorsProvider(Theme.chartColors())

    /**
     * Calculates the layout for the bars
     */
    var layoutProvider: (categorySize: @px Double, categoryModel: CategorySeriesModel, categoryOrientation: Orientation) -> EquisizedBoxLayout = { categorySize, categoryModel, _ ->
      BoxLayoutCalculator.layout(
        availableSpace = categorySize,
        numberOfBoxes = categoryModel.numberOfSeries,
        layoutDirection = LayoutDirection.CenterHorizontal,
        minBoxSize = minBarSize,
        maxBoxSize = maxBarSize,
        gapSize = barGap
      )
    }

    /**
     * Font used for all elements
     */
    var valueLabelFont: FontDescriptorFragment = FontDescriptorFragment.empty

    /**
     * Whether to show value labels
     */
    var showValueLabel: Boolean = true

    /**
     * The format to be used for the value labels
     */
    var valueLabelFormat: CachedNumberFormat = decimalFormat

    /**
     * The color to be used for the value labels; set to null to use the color of the corresponding bar
     */
    var valueLabelColor: Color? = null

    /**
     * The color to be used to stroke the value-labels
     */
    var valueLabelStrokeColor: Color? = null

    /**
     * The gap for the value label anchor
     */
    var valueLabelAnchorGapHorizontal: @Zoomed Double = 2.0

    var valueLabelAnchorGapVertical: @Zoomed Double = 5.0

    /**
     * In which direction to paint the label.
     */
    var valueLabelAnchorDirectionProvider: ValueLabelAnchorDirectionProvider = AdvancedBarsValueLabelAnchorDirectionProvider

    /**
     * The string shortener that is used for the value labels
     */
    var valueLabelStringShortener: CanvasStringShortener = CanvasStringShortener.AllOrNothing

    /**
     * Provides the box the value labels are placed within
     */
    var valueLabelBoxProvider: @Window BoxProvider1<ChartCalculator> = ContentAreaBoxProvider

    /**
     * Whether to paint bars or not
     */
    var showBars: Boolean = true

    /**
     * The gap between two consecutive bars within a category
     */
    var barGap: @Zoomed Double = 1.0

    /**
     * The min bar size for each single bar
     */
    var minBarSize: @Zoomed Double = 0.0
      private set

    /**
     * The max bar size for each single bar
     *
     * Null implies an unlimited size
     */
    var maxBarSize: @Zoomed Double? = 14.0
      private set

    /**
     * Sets the minimum and maximum bar size for each single bar
     *
     * Null implies an unlimited size
     */
    fun setBarSizeRange(newMinBarSize: @Zoomed Double, newMaxBarSize: @Zoomed Double?) {
      require(newMaxBarSize == null || newMaxBarSize >= newMinBarSize) { "maxBarSize <$newMaxBarSize> must be greater than or equal to minBarSize <$newMinBarSize>" }
      this.minBarSize = newMinBarSize
      this.maxBarSize = newMaxBarSize
    }

    /**
     * Used to paint the overflow indicators
     */
    var overflowIndicatorPainter: OverflowIndicatorPainter? = OverflowIndicatorPainter()
  }

  /**
   * Provides the anchor direction for a value label for a bar.
   *
   * First [directionInBarToAnchorPointHorizontal]/[directionInBarToAnchorPointVertical] will be called. The returned direction
   * is then used to calculate the anchor point
   *
   * Then [anchorDirectionHorizontal]/[anchorDirectionVertical] is called. The returned direction is then used to place the value label
   *
   */
  interface ValueLabelAnchorDirectionProvider {
    /**
     * Returns the direction within the bar - that will be used to calculate the anchor point.
     * This method is called for horizontal bars
     *
     * E.g. TopRight: The anchor will be at the top right edge of the bar
     */
    fun directionInBarToAnchorPointHorizontal(
      /**
       * The axis orientation
       */
      axisOrientation: AxisOrientationX,
      /**
       * The domain value for the bar
       */
      barValue: @Domain Double,
      /**
       * The label for the bar
       */
      barLabel: String,

      /**
       * The barLabel width
       */
      barLabelWidth: @Zoomed Double,
      /**
       * The size of the bar.
       */
      barSize: @Zoomed Double,

      anchorGapHorizontal: @Zoomed Double,

      anchorGapVertical: @Zoomed Double,

      ): Direction

    /**
     * Returns the direction within the bar - that will be used to calculate the anchor point.
     * This method is called for vertical bars
     *
     * E.g. TopRight: The anchor will be at the top right edge of the bar
     */
    fun directionInBarToAnchorPointVertical(
      /**
       * The axis orientation
       */
      axisOrientation: AxisOrientationY,
      /**
       * The domain value for the bar
       */
      barValue: @Domain Double,
      /**
       * The label for the bar
       */
      barLabel: String,

      /**
       * The barLabel width
       */
      barLabelWidth: @Zoomed Double,
      /**
       * The size of the bar.
       */
      barSize: @Zoomed Double,

      anchorGapHorizontal: @Zoomed Double,

      anchorGapVertical: @Zoomed Double,

      ): Direction

    /**
     * Returns the anchor direction - for the given anchor point
     */
    fun anchorDirectionHorizontal(
      /**
       * The axis orientation
       */
      axisOrientation: AxisOrientationX,
      /**
       * The domain value for the bar
       */
      barValue: @Domain Double,
      /**
       * The label for the bar
       */
      barLabel: String,

      /**
       * The barLabel width
       */
      barLabelWidth: @Zoomed Double,

      /**
       * The size of the bar.
       */
      barSize: @Zoomed Double,
      /**
       * The anchor location (center of the end of the bar)
       */
      anchorX: @Window Double,
      /**
       * The anchor location (center of the end of the bar)
       */
      anchorY: @Window Double,
      /**
       * The anchor gap
       */
      anchorGapHorizontal: @Zoomed Double,

      anchorGapVertical: @Zoomed Double,
      paintingContext: LayerPaintingContext,
    ): Direction

    /**
     * Returns the anchor direction - for the given anchor point
     */
    fun anchorDirectionVertical(
      axisOrientation: AxisOrientationY,
      /**
       * The domain value for the bar
       */
      barValue: @Domain Double,
      /**
       * The label for the bar
       */
      barLabel: String,

      /**
       * The barLabel width
       */
      barLabelWidth: @Zoomed Double,

      /**
       * The size of the bar.
       */
      barSize: @Zoomed Double,
      /**
       * The anchor location (center of the end of the bar)
       */
      anchorX: @Window Double,
      /**
       * The anchor location (center of the end of the bar)
       */
      anchorY: @Window Double,
      /**
       * The anchor gap
       */
      anchorGapHorizontal: @Zoomed Double,

      anchorGapVertical: @Zoomed Double,
      paintingContext: LayerPaintingContext,
    ): Direction

    companion object {
      /**
       * Returns true if the bar points to the left.
       * Either:
       * * because negative value
       * * or the axis is inverted
       */
      fun barPointsLeft(barValue: Double, axisOrientation: AxisOrientationX): Boolean = barValue.isNegative() xor axisOrientation.axisInverted

      /**
       * Returns true if the bar points to the left.
       * Either:
       * * because negative value
       * * or the axis is inverted
       */
      fun barPointsUp(barValue: Double, axisOrientation: AxisOrientationY): Boolean = barValue.isNegative() xor axisOrientation.axisInverted
    }
  }
}


