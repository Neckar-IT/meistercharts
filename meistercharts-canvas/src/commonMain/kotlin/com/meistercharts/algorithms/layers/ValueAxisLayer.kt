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

import com.meistercharts.algorithms.layers.ValueAxisLayer.Companion.linear
import com.meistercharts.algorithms.layers.ValueAxisLayer.Companion.logarithmic
import com.meistercharts.algorithms.layers.ValueAxisLayer.Style
import com.meistercharts.algorithms.layers.barchart.AbstractAxisLayer
import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.text.CanvasStringShortener
import com.meistercharts.canvas.layout.cache.DoubleCache
import it.neckar.geometry.Direction
import it.neckar.geometry.HorizontalAlignment
import it.neckar.geometry.Orientation
import com.meistercharts.model.Vicinity
import com.meistercharts.provider.ValueRangeProvider
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.LogarithmicValueRange
import com.meistercharts.range.ValueRange
import it.neckar.open.collections.emptyDoubleArray
import it.neckar.open.formatting.decimalFormat1digit
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmOverloads
import kotlin.math.roundToInt

/**
 * Paints a value axis.
 * Supports two directions: [Orientation.Vertical] (on the left) and [Orientation.Horizontal] (at the bottom).
 * For each direction the ticks can be
 * - inside (towards the content - right/top of the axis line)
 * - outside (between the title and the axis line)
 *
 * The [Style.size] is especially important for [Vicinity.Inside]: In this case the [Style.size] describes the *distance* between
 * the outer side and the axis line.
 * This can be very useful when placing the axis on the edge of the content area.
 *
 * Attention: The margin is added to the outside of the value axis. Therefore, the position of the axis can be calculated as follows: [Style.margin] + [Style.size]
 */
class ValueAxisLayer
/**
 * Consider using the factory methods [logarithmic] / [linear]
 */
@JvmOverloads constructor(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {},
) : AbstractAxisLayer() {

  /**
   * Consider using the factory methods [logarithmic] / [linear]
   */
  @JvmOverloads
  constructor(title: String, valueRange: ValueRange, styleConfiguration: Style.() -> Unit = {}) : this(
    Data(
      valueRangeProvider = { valueRange }
    ),
    {
      titleProvider = { _, _ -> title }
      styleConfiguration()
    },
  )

  override val axisConfiguration: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paintingVariables(): ValueAxisPaintingVariables {
    return paintingVariables
  }

  //Painting variable - stores intermediate results required for painting
  private val paintingVariables = object : ValueAxisPaintingVariablesImpl() {
    /**
     * Recalculates all painting variables
     */
    override fun calculate(paintingContext: LayerPaintingContext) {
      reset()

      contentAreaValueRange = data.valueRangeProvider()

      calculateTickFontMetrics(paintingContext, axisConfiguration)

      calculateEstimatedTickFormatMaxLength(paintingContext, axisConfiguration)
      calculateTitle(paintingContext, axisConfiguration)

      calculateAxisStartEnd(paintingContext, axisConfiguration)
      calculateDomainStartEndValues(paintingContext, axisConfiguration)

      calculateTickLabelsMaxWidth(axisConfiguration)
      calculateLocations(paintingContext, axisConfiguration)

      storeTicks(calculateTickValues(paintingContext), paintingContext, axisConfiguration)
    }

    /**
     * Calculate the tick values that are painted
     */
    private fun calculateTickValues(paintingContext: LayerPaintingContext): @Domain DoubleArray {
      return when (axisConfiguration.orientation) {
        Orientation.Vertical -> calculateTickValuesValueRangeVertically(fontHeight = tickFontMetrics.totalHeight)
        Orientation.Horizontal -> calculateTickValuesValueRangeHorizontally(maxFormattedLabelWidth = estimatedTickFormatMaxLength)
      }
    }
  }

  /**
   * Returns the last painted tick values
   */
  val tickDomainValues: @Domain DoubleCache
    get() = paintingVariables.tickDomainValues

  override fun paintTicksWithLabelsVertically(paintingContext: LayerPaintingContext, direction: Direction) {
    val chartCalculator = paintingContext.chartCalculator
    val gc = paintingContext.gc

    gc.fillStyle(axisConfiguration.tickLabelColor())
    gc.strokeStyle(axisConfiguration.lineColor())
    gc.font(axisConfiguration.tickFont)
    gc.lineWidth = axisConfiguration.tickLineWidth

    val valueRange = data.valueRangeProvider()

    paintingVariables.tickDomainValues.fastForEachIndexed { index, tickValue ->
      @px val currentY = chartCalculator.domain2windowY(tickValue, valueRange)

      //The tick line
      if (axisConfiguration.tickLength > 0.0 && axisConfiguration.tickLineWidth > 0.0) {
        when (direction) {
          Direction.CenterLeft -> gc.strokeLine(-axisConfiguration.tickLabelGap - axisConfiguration.tickLength, currentY, -axisConfiguration.tickLabelGap, currentY)
          Direction.CenterRight -> gc.strokeLine(axisConfiguration.tickLabelGap, currentY, axisConfiguration.tickLabelGap + axisConfiguration.tickLength, currentY)
          else -> throw IllegalArgumentException("Unsupported anchor direction: $direction")
        }
      }

      //The tick label
      val formattedTick = paintingVariables.ticksFormatted[index]
      if (formattedTick.isNotEmpty()) {
        gc.fillText(
          text = formattedTick,
          x = 0.0,
          y = currentY,
          anchorDirection = direction,
          gapHorizontal = 0.0,
          gapVertical = 0.0,
          maxWidth = paintingVariables.tickValueLabelMaxWidth,
          maxHeight = null, //do not limit the max height for now. Should be kept inside the window(?)
          stringShortener = axisConfiguration.valueLabelStringShortener
        )
      }
    }
  }

  override fun paintTicksWithLabelsHorizontally(paintingContext: LayerPaintingContext, direction: Direction) {
    val chartCalculator = paintingContext.chartCalculator
    val gc = paintingContext.gc

    gc.fillStyle(axisConfiguration.tickLabelColor())
    gc.strokeStyle(axisConfiguration.lineColor())
    gc.lineWidth = axisConfiguration.tickLineWidth
    gc.font(axisConfiguration.tickFont)

    val valueRange = data.valueRangeProvider()

    paintingVariables.tickDomainValues.fastForEachIndexed { index, tickValue ->
      @px val currentX = chartCalculator.domain2windowX(tickValue, valueRange)

      //The tick line
      if (axisConfiguration.tickLength > 0.0 && axisConfiguration.tickLineWidth > 0.0) {
        when (direction) {
          Direction.BottomCenter -> gc.strokeLine(currentX, axisConfiguration.tickLabelGap + axisConfiguration.tickLength, currentX, axisConfiguration.tickLabelGap)
          Direction.TopCenter -> gc.strokeLine(currentX, -axisConfiguration.tickLabelGap, currentX, -axisConfiguration.tickLabelGap - axisConfiguration.tickLength)
          else -> throw IllegalArgumentException("Unsupported anchor direction: $direction")
        }
      }

      //The tick label
      val tickValueLabel = paintingVariables.ticksFormatted[index]
      if (tickValueLabel.isNotEmpty()) {
        //Calculate available space in window
        val width = paintingContext.width


        //Calculate the max width for the label
        @px val remainingSpaceToRight = width - currentX

        @Suppress("UnnecessaryVariable")
        @px val remainingSpaceToLeft = currentX

        @px val labelMaxWidth = when (direction.horizontalAlignment) {
          HorizontalAlignment.Left -> paintingVariables.tickValueLabelMaxWidth.coerceAtMost(remainingSpaceToLeft) //only limited to the left
          HorizontalAlignment.Center -> paintingVariables.tickValueLabelMaxWidth.coerceAtMost(remainingSpaceToRight * 2).coerceAtMost(remainingSpaceToLeft * 2)
          HorizontalAlignment.Right -> paintingVariables.tickValueLabelMaxWidth.coerceAtMost(remainingSpaceToRight)
        }

        gc.fillText(
          text = tickValueLabel,
          x = currentX,
          y = 0.0,
          anchorDirection = direction,
          gapHorizontal = 0.0,
          gapVertical = 0.0,
          maxWidth = labelMaxWidth,
          stringShortener = axisConfiguration.valueLabelStringShortener
        )
      }
    }
  }

  /**
   * Calculates the ticks - only for the area that is visible!
   */
  private fun calculateTickValuesValueRangeVertically(fontHeight: @px Double): @Domain DoubleArray {
    //If we are below or above the screen, the relevant height is negative
    if (paintingVariables.axisLength <= 0) {
      return emptyDoubleArray()
    }

    val maxTickCount = (paintingVariables.axisLength / (fontHeight * 2.0) + 0.5).roundToInt()
    return axisConfiguration.ticks.getTicks(paintingVariables.startDomainValue, paintingVariables.endDomainValue, maxTickCount, 0.0, axisConfiguration.axisEndConfiguration)
  }

  /**
   * Calculates the ticks - only for the area that is visible!
   */
  private fun calculateTickValuesValueRangeHorizontally(maxFormattedLabelWidth: @px Double): @Domain DoubleArray {
    //If we are below or above the screen, the relevant height is negative
    if (paintingVariables.axisLength <= 0) {
      return emptyDoubleArray()
    }

    val maxTickCount = (paintingVariables.axisLength / maxFormattedLabelWidth + 0.5).roundToInt()
    return axisConfiguration.ticks.getTicks(paintingVariables.startDomainValue, paintingVariables.endDomainValue, maxTickCount, 0.0, axisConfiguration.axisEndConfiguration)
  }

  companion object {
    /**
     * Creates a linear value axis
     */
    fun linear(title: String, valueRange: LinearValueRange, styleConfiguration: Style.() -> Unit = {}): ValueAxisLayer {
      return ValueAxisLayer(title, valueRange) {
        //Configure the ticks for linear
        applyLinearScale()
        styleConfiguration()
      }
    }

    /**
     * Creates a logarithmic value axis
     */
    fun logarithmic(title: String, valueRange: LogarithmicValueRange, styleConfiguration: Style.() -> Unit = {}): ValueAxisLayer {
      return ValueAxisLayer(title, valueRange) {
        //Configure the ticks for logarithmic
        applyLogarithmicScale()
        styleConfiguration()
      }
    }
  }

  open class Data(
    /**
     * Provides the value range for the axis
     */
    var valueRangeProvider: ValueRangeProvider = { ValueRange.default }
  )

  /**
   * The style for the value axis
   */
  open class Style : AxisConfiguration() {
    /**
     * Provider that returns the ticks
     */
    var ticks: TickProvider = TickProvider.linear

    /**
     * Configures the style for a logarithmic value range
     *
     * This changes the tick provider, tick format and axis-end configuration
     */
    fun applyLogarithmicScale() {
      setTicksToLogarithmicScale()
      ticksFormat = decimalFormat1digit
    }

    /**
     * Optimizes the ticks for a logarithmic scale
     */
    fun setTicksToLogarithmicScale() {
      ticks = TickProvider.logarithmic
    }

    /**
     * Configures the style for a linear value range
     *
     * This changes the tick provider, tick format and axis-end configuration
     */
    fun applyLinearScale() {
      setTicksToLinearScale()
      ticksFormat = decimalFormat2digits
    }

    /**
     * Optimizes the ticks for a linear scale
     */
    fun setTicksToLinearScale() {
      ticks = TickProvider.linear
    }

    /**
     * The string shortener that is used for the labels
     */
    var valueLabelStringShortener: CanvasStringShortener = CanvasStringShortener.AllOrNothing
  }


}
