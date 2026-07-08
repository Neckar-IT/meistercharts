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
package com.meistercharts.algorithms.layers.axis

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.barchart.AbstractAxisLayer
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.font
import com.meistercharts.canvas.saved
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.design.Theme
import com.meistercharts.font.FontDescriptorFragmentProvider
import com.meistercharts.provider.ValueRangeProvider
import com.meistercharts.range.ValueRange
import it.neckar.geometry.Direction
import it.neckar.geometry.Orientation
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.formatting.format
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.number.Positive
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmInline
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Paints a value axis with an additional offset to reduce the amount of horizontal space needed to display values.
 *
 * Large values are split into an *offset* (painted once per coloured offset band) and a *remainder* (the tick label).
 * Example (with [Configuration.spaceForDigits] == 3): a tick value of `23741` is painted as the label `741` inside an
 * offset band that displays `+23,000`.
 */
class ValueAxisWithOffsetLayer(
  override val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractAxisLayer() {

  constructor(
    title: String,
    valueRange: ValueRange,
    additionalConfiguration: Configuration.() -> Unit = {},
  ) : this(
    Configuration(valueRangeProvider = { valueRange }),
    {
      titleProvider = { _, _ -> title }
      additionalConfiguration()
    },
  )

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType
    get() = LayerType.Content

  override fun paintingVariables(): ValueAxisWithOffsetPaintingVariables {
    return paintingVariables
  }

  //Painting variable - stores intermediate results required for painting
  private val paintingVariables = object : ValueAxisWithOffsetPaintingVariablesImpl() {
    override fun calculate(paintingContext: LayerPaintingContext) {
      reset()

      contentAreaValueRange = configuration.valueRangeProvider()

      calculateTickFontMetrics(paintingContext, configuration)

      calculateEstimatedTickFormatMaxLength(paintingContext, configuration)
      calculateTitle(paintingContext, configuration)

      calculateAxisStartEnd(paintingContext, configuration)

      calculateDomainStartEndValues(paintingContext, configuration)

      calculateTickLabelsMaxWidth(configuration)
      calculateLocations(paintingContext, configuration)

      calculateOffsetMagnitudes()

      calculateTickValues(paintingContext)
      storeOffsetTicks(tickValuesScratch, paintingContext)
      calculateOffsetBandTicks()
    }

    /**
     * Calculates [deltaMagnitude], [integerDigits], [fractionDigits] and [offsetStep] from the visible domain range.
     */
    private fun calculateOffsetMagnitudes() {
      @Domain val delta = (endDomainValue - startDomainValue).absoluteValue

      deltaMagnitude = if (delta <= 0.0 || delta.isFinite().not()) {
        0
      } else {
        //+1e-9 to avoid log10(1000) rounding down to 2.9999...
        floor(log10(delta) + 1e-9).toInt()
      }

      offsetStep = 10.0.pow(configuration.spaceForDigits)

      //As close to spaceForDigits as possible - only fewer integer digits when zoomed in
      integerDigits = configuration.spaceForDigits.coerceAtMost((deltaMagnitude + 1).coerceAtLeast(1))
      //Only above 0 when zoomed in below integer resolution
      fractionDigits = (1 - deltaMagnitude).coerceIn(0, MaxFractionDigits)
    }

    /**
     * Stores the tick domain values and their formatted *remainder* labels (value minus the band offset).
     */
    private fun storeOffsetTicks(tickValues: @Domain DoubleArrayList, paintingContext: LayerPaintingContext) {
      tickLabels.resize(tickValues.size)

      //decimalFormat delegates to the global DecimalFormatsCache - same instance (and string cache) for the same fractionDigits
      val remainderFormat = decimalFormat(fractionDigits)

      for (index in 0 until tickValues.size) {
        val value = tickValues[index]
        @Domain val remainder = value - offsetForValue(value)
        tickLabels.set(index, value, remainderFormat.format(remainder, paintingContext.i18nConfiguration))
      }
    }

    /**
     * Calculates the offset band boundaries within the visible range and stores them in [ValueAxisWithOffsetPaintingVariablesImpl.offsetTickLabels].
     */
    private fun calculateOffsetBandTicks() {
      if (startDomainValue.isFinite().not() || endDomainValue.isFinite().not()) {
        offsetTickLabels.resize(0)
        return
      }

      @Domain val bandSize = currentBandSize()
      if (bandSize <= 0.0) {
        offsetTickLabels.resize(0)
        return
      }

      val startIndex = offsetIndexForValue(startDomainValue).value
      val endIndex = offsetIndexForValue(endDomainValue).value

      val count = endIndex - startIndex + 1
      if (count <= 0 || count > MaxOffsetBandCount) {
        offsetTickLabels.resize(0)
        return
      }

      offsetTickLabels.resize(count)
      for (i in 0 until count) {
        @Domain val offsetValue = (startIndex + i) * bandSize
        offsetTickLabels.set(i, offsetValue, formatOffset(offsetValue))
      }
    }

    /**
     * Calculate the tick values that are painted (into [tickValuesScratch]) - only for the visible range.
     */
    private fun calculateTickValues(paintingContext: LayerPaintingContext) {
      when (configuration.orientation) {
        Orientation.Vertical -> calculateTickValuesValueRangeVertically(fontHeight = tickFontMetrics.totalHeight)
        Orientation.Horizontal -> calculateTickValuesValueRangeHorizontally(maxFormattedLabelWidth = estimatedTickFormatMaxLength)
      }
    }

    private fun calculateTickValuesValueRangeHorizontally(maxFormattedLabelWidth: @px Double) {
      if (axisLength <= 0.0 || maxFormattedLabelWidth <= 0.0) {
        tickValuesScratch.clear()
        return
      }

      val maxTickCount = (axisLength / maxFormattedLabelWidth + 0.5).roundToInt()
      configuration.ticks.fillTicks(startDomainValue, endDomainValue, maxTickCount, 0.0, configuration.axisEndConfiguration, tickValuesScratch)
    }

    private fun calculateTickValuesValueRangeVertically(fontHeight: @px Double) {
      if (axisLength <= 0.0 || fontHeight <= 0.0) {
        tickValuesScratch.clear()
        return
      }

      val maxTickCount = (axisLength / (fontHeight * 2.0) + 0.5).roundToInt()
      configuration.ticks.fillTicks(startDomainValue, endDomainValue, maxTickCount, 0.0, configuration.axisEndConfiguration, tickValuesScratch)
    }
  }

  /**
   * The band size that is used to factor out the offset - depends on the current zoom level.
   */
  private fun currentBandSize(): @Domain Double {
    return if (paintingVariables.deltaMagnitude < paintingVariables.integerDigits) {
      10.0.pow(paintingVariables.integerDigits)
    } else {
      paintingVariables.offsetStep
    }
  }

  /**
   * Calculates the offset index for the given value.
   */
  fun offsetIndexForValue(value: @Domain Double): OffsetIndex {
    return OffsetIndex((value / currentBandSize()).toInt())
  }

  /**
   * The offset (the factored-out part) for the given value.
   */
  private fun offsetForValue(value: @Domain Double): @Domain Double {
    return offsetIndexForValue(value).value * currentBandSize()
  }

  override fun paintTicksWithLabelsVertically(paintingContext: LayerPaintingContext, direction: Direction) {
    val chartCalculator = paintingContext.chartCalculator
    val gc = paintingContext.gc

    val valueRange = configuration.valueRangeProvider()

    gc.fillStyle(configuration.tickLabelColor())
    gc.strokeStyle(configuration.lineColor())
    gc.font(configuration.tickFont())
    gc.lineWidth = configuration.tickLineWidth

    paintingVariables.tickLabels.fastForEachIndexed { _, tickValue, formattedTick ->
      @px @Window val currentY = chartCalculator.domain2windowY(tickValue, valueRange)

      if (configuration.tickLength > 0.0 && configuration.tickLineWidth > 0.0) {
        when (direction) {
          Direction.CenterLeft -> gc.strokeLine(-configuration.tickLabelGap - configuration.tickLength, currentY, -configuration.tickLabelGap, currentY)
          Direction.CenterRight -> gc.strokeLine(configuration.tickLabelGap, currentY, configuration.tickLabelGap + configuration.tickLength, currentY)
          else -> throw IllegalArgumentException("Unsupported anchor direction: $direction")
        }
      }

      if (formattedTick.isNotEmpty()) {
        gc.fillText(
          text = formattedTick,
          x = 0.0,
          y = currentY,
          anchorDirection = direction,
          gapHorizontal = 0.0,
          gapVertical = 0.0,
          maxWidth = paintingVariables.tickValueLabelMaxWidth,
          maxHeight = null,
        )
      }
    }

    paintOffsetAreaVertically(paintingContext, direction, valueRange)
  }

  override fun paintTicksWithLabelsHorizontally(paintingContext: LayerPaintingContext, direction: Direction) {
    val chartCalculator = paintingContext.chartCalculator
    val gc = paintingContext.gc

    val valueRange = configuration.valueRangeProvider()

    gc.fillStyle(configuration.tickLabelColor())
    gc.strokeStyle(configuration.lineColor())
    gc.lineWidth = configuration.tickLineWidth
    gc.font(configuration.tickFont())

    paintingVariables.tickLabels.fastForEachIndexed { _, tickValue, tickValueLabel ->
      @px @Window val currentX = chartCalculator.domain2windowX(tickValue, valueRange)

      if (configuration.tickLength > 0.0 && configuration.tickLineWidth > 0.0) {
        when (direction) {
          Direction.BottomCenter -> gc.strokeLine(currentX, configuration.tickLabelGap + configuration.tickLength, currentX, configuration.tickLabelGap)
          Direction.TopCenter -> gc.strokeLine(currentX, -configuration.tickLabelGap, currentX, -configuration.tickLabelGap - configuration.tickLength)
          else -> throw IllegalArgumentException("Unsupported anchor direction: $direction")
        }
      }

      if (tickValueLabel.isNotEmpty()) {
        gc.fillText(
          text = tickValueLabel,
          x = currentX,
          y = 0.0,
          anchorDirection = direction,
          gapHorizontal = 0.0,
          gapVertical = 0.0,
          maxWidth = paintingVariables.tickValueLabelMaxWidth,
        )
      }
    }

    paintOffsetAreaHorizontally(paintingContext, direction, valueRange)
  }

  /**
   * Paints the coloured offset bands and their offset labels for a vertical axis.
   * The offset area is painted beyond the tick value labels (away from the origin).
   */
  private fun paintOffsetAreaVertically(paintingContext: LayerPaintingContext, direction: Direction, valueRange: ValueRange) {
    val offsetTickLabels = paintingVariables.offsetTickLabels
    if (offsetTickLabels.size <= 0) {
      return
    }

    val chartCalculator = paintingContext.chartCalculator
    val gc = paintingContext.gc

    @px val gap = configuration.offsetAreaTickLabelGap
    @px val size = configuration.offsetAreaSize
    @px val labelsWidth = paintingVariables.tickValueLabelMaxWidth

    @px val areaLeft = when (direction) {
      Direction.CenterRight -> -(labelsWidth + gap + size)
      Direction.CenterLeft -> labelsWidth + gap
      else -> throw IllegalArgumentException("Unsupported anchor direction: $direction")
    }

    @Window val axisMin = min(paintingVariables.axisStart, paintingVariables.axisEnd)
    @Window val axisMax = max(paintingVariables.axisStart, paintingVariables.axisEnd)

    gc.font(configuration.offsetTickFont())

    offsetTickLabels.fastForEachIndexed { index, offsetValue: @Domain Double, formatted: String ->
      @Domain val bandLowerValue = offsetValue
      @Domain val bandUpperValue = if (index < offsetTickLabels.size - 1) offsetTickLabels.valueAt(index + 1) else offsetValue + currentBandSize()

      @px @Window val yAtLower = chartCalculator.domain2windowY(bandLowerValue, valueRange)
      @px @Window val yAtUpper = chartCalculator.domain2windowY(bandUpperValue, valueRange)

      @px @Window val bandTop = max(axisMin, min(yAtLower, yAtUpper))
      @px @Window val bandBottom = min(axisMax, max(yAtLower, yAtUpper))

      if (bandBottom <= bandTop) {
        return@fastForEachIndexed
      }

      gc.fill(configuration.offsetAreaFills.valueAt(offsetIndexForValue(offsetValue)))
      gc.fillRectCoordinates(areaLeft, bandTop, areaLeft + size, bandBottom)

      @Zoomed val availableHeight = bandBottom - bandTop
      @px val textWidth = gc.calculateTextWidth(formatted)

      if (availableHeight > textWidth) {
        gc.saved {
          gc.translate(areaLeft + size / 2.0, bandTop + availableHeight / 2.0)
          gc.rotateDegrees(-90.0)
          gc.fillStyle(configuration.offsetTickLabelColor())
          gc.fillText(formatted, 0.0, 0.0, Direction.Center, maxWidth = availableHeight)
        }
      }
    }
  }

  /**
   * Paints the coloured offset bands and their offset labels for a horizontal axis.
   * The offset area is painted beyond the tick value labels (away from the origin).
   */
  private fun paintOffsetAreaHorizontally(paintingContext: LayerPaintingContext, direction: Direction, valueRange: ValueRange) {
    val offsetTickLabels = paintingVariables.offsetTickLabels
    if (offsetTickLabels.size <= 0) {
      return
    }

    val chartCalculator = paintingContext.chartCalculator
    val gc = paintingContext.gc

    @px val gap = configuration.offsetAreaTickLabelGap
    @px val size = configuration.offsetAreaSize
    @px val labelsHeight = paintingVariables.tickFontMetrics.totalHeight

    @px val areaTop = when (direction) {
      Direction.TopCenter -> labelsHeight + gap
      Direction.BottomCenter -> -(labelsHeight + gap + size)
      else -> throw IllegalArgumentException("Unsupported anchor direction: $direction")
    }

    @Window val axisMin = min(paintingVariables.axisStart, paintingVariables.axisEnd)
    @Window val axisMax = max(paintingVariables.axisStart, paintingVariables.axisEnd)

    gc.font(configuration.offsetTickFont())

    offsetTickLabels.fastForEachIndexed { index, offsetValue: @Domain Double, formatted: String ->
      @Domain val bandLowerValue = offsetValue
      @Domain val bandUpperValue = if (index < offsetTickLabels.size - 1) offsetTickLabels.valueAt(index + 1) else offsetValue + currentBandSize()

      @px @Window val xAtLower = chartCalculator.domain2windowX(bandLowerValue, valueRange)
      @px @Window val xAtUpper = chartCalculator.domain2windowX(bandUpperValue, valueRange)

      @px @Window val bandLeft = max(axisMin, min(xAtLower, xAtUpper))
      @px @Window val bandRight = min(axisMax, max(xAtLower, xAtUpper))

      if (bandRight <= bandLeft) {
        return@fastForEachIndexed
      }

      gc.fill(configuration.offsetAreaFills.valueAt(offsetIndexForValue(offsetValue)))
      gc.fillRectCoordinates(bandLeft, areaTop, bandRight, areaTop + size)

      @Zoomed val availableWidth = bandRight - bandLeft
      @px val textWidth = gc.calculateTextWidth(formatted)

      if (availableWidth > textWidth) {
        gc.fillStyle(configuration.offsetTickLabelColor())
        gc.fillText(formatted, bandLeft + availableWidth / 2.0, areaTop + size / 2.0, Direction.Center, maxWidth = availableWidth)
      }
    }
  }

  /**
   * Formats an offset value with an explicit sign, e.g. `+23,000`.
   */
  private fun formatOffset(offsetValue: @Domain Double): String {
    val formatted = offsetValue.format(numberOfDecimals = paintingVariables.fractionDigits)
    return if (offsetValue > 0.0) "+$formatted" else formatted
  }

  @ConfigurationDsl
  open class Configuration(
    /**
     * Provides the value range for the axis
     */
    override var valueRangeProvider: ValueRangeProvider = { ValueRange.default },
  ) : ValueAxisLayer.Configuration() {
    /**
     * The size of the offset area
     */
    var offsetAreaSize: @Positive Double = 30.0

    /**
     * The gap between the tick value labels and the offset area
     */
    var offsetAreaTickLabelGap: @px Double = 7.0

    /**
     * The fills that are used for the offset areas.
     */
    var offsetAreaFills: MultiProvider<OffsetIndex, Color> = MultiProvider.Companion.modulo(Color.web("#DBE1E5"), Color.web("#F3F5F7"))

    /**
     * The colors of the offset area ticks
     */
    var offsetTickLabelColor: ColorProvider = Theme.axisTickColor.provider()

    /**
     * The font for the offset ticks
     */
    var offsetTickFont: FontDescriptorFragmentProvider = Theme.offsetTickFont.provider()

    /**
     * The maximum amount of digits each value tick is allowed to have before an offset is added
     *  For example, when set to 3, a value tick of 23.741 would be shortened to 741 with an offset of +23.000
     *  This also works for decimal numbers
     */
    var spaceForDigits: @Positive Int = 6
  }

  companion object {
    /**
     * The maximum number of fraction digits that is used for the tick labels when zoomed in.
     */
    private const val MaxFractionDigits: Int = 10

    /**
     * The maximum number of offset bands that are painted. Guards against pathological zoom-out.
     */
    private const val MaxOffsetBandCount: Int = 1000
  }
}

/**
 * Represents the "global" offset index
 */
@JvmInline
value class OffsetIndex(val value: Int)


inline fun <T> MultiProvider<OffsetIndex, T>.valueAt(index: OffsetIndex): T {
  return this.valueAt(index.value)
}
