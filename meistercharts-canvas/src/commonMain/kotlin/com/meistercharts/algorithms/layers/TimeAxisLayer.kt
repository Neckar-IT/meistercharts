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

import com.meistercharts.Meistercharts
import com.meistercharts.algorithms.layers.barchart.AbstractAxisLayer
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.axis.DistanceDays
import com.meistercharts.axis.DistanceHours
import com.meistercharts.axis.DistanceMillis
import com.meistercharts.axis.DistanceMinutes
import com.meistercharts.axis.DistanceMonths
import com.meistercharts.axis.DistanceSeconds
import com.meistercharts.axis.DistanceYears
import com.meistercharts.axis.GlobalTimeIndex
import com.meistercharts.axis.TimeTickDistance
import com.meistercharts.axis.TimeUnits
import com.meistercharts.axis.valueAt
import com.meistercharts.calc.TimeChartCalculator
import com.meistercharts.canvas.text.CanvasStringShortener
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.saved
import com.meistercharts.color.Color
import com.meistercharts.design.Theme
import com.meistercharts.model.Direction
import com.meistercharts.model.Orientation
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import com.meistercharts.time.TimeRange
import it.neckar.open.collections.fastContains
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.formatting.DateTimeFormat
import it.neckar.open.formatting.dateFormat
import it.neckar.open.formatting.dateTimeFormatIso8601
import it.neckar.open.formatting.secondMillisFormat
import it.neckar.open.formatting.timeFormat
import it.neckar.open.formatting.yearFormat
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.betweenInclusive
import it.neckar.open.provider.MultiProvider
import it.neckar.open.time.TimeConstants
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.number.Positive
import it.neckar.open.unit.other.px
import it.neckar.open.unit.quantity.Time
import it.neckar.open.unit.si.ms
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Paints a time axis at the bottom of the canvas
 * @property data - contains the timeRange the time range to be displayed at 100% zoom and with no translation
 * @param styleConfiguration the style configuration that is applied
 */
class TimeAxisLayer(
  val data: Data = Data(),
  styleConfiguration: Style.() -> Unit = {},
) : AbstractAxisLayer() {

  override val style: Style = Style().apply {
    side = Side.Bottom
  }.also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paintingVariables(): TimeAxisPaintingVariables {
    return paintingVariables
  }

  //Painting variable - stores intermediate results required for painting
  private val paintingVariables = object : TimeAxisPaintingVariablesImpl() {

    //TODO replace me
    var spaceForTickLabels: Double = Double.NaN

    /**
     * Clears all variables
     */
    override fun calculate(paintingContext: LayerPaintingContext) {
      reset()

      contentAreaTimeRange = data.contentAreaTimeRange

      //TODO!!!! fix me somehow!
      calculateTickFontMetrics(paintingContext, style)
      calculateTitle(paintingContext, style)

      calculateAxisStartEnd(paintingContext, style)
      calculateDomainStartEndValues(paintingContext, style)

      calculateTickLabelsMaxWidth(style)
      calculateLocations(paintingContext, style)

      calculateTickValues(paintingContext, style)
    }

    /**
     * Calculate the tick values that are painted
     */
    private fun calculateTickValues(paintingContext: LayerPaintingContext, style: Style) {
      return when (style.orientation) {
        Orientation.Vertical -> throw UnsupportedOperationException("Only implemented for horizontal orientation at the moment!")
        Orientation.Horizontal -> calculateTickValuesValueRangeHorizontally(paintingContext, style)
      }
    }

    /**
     * Calculates the tick values
     */
    private fun calculateTickValuesValueRangeHorizontally(paintingContext: LayerPaintingContext, style: Style) {
      //Note: in order to know how many formatted ticks may be displayed along this axis
      //we need to know the text length of one formatted tick. However, to compute the
      //text length of one formatted tick we need to know the distance between two ticks
      //which in turn requires to know how many ticks are there. So either we need to compute
      //the layout at least twice or we simplify matters by assuming that a tick label is
      //about 100 pixels wide.
      val guessedTickCount = paintingContext.width / (100.0 * this@TimeAxisLayer.style.tickLabelWidthFactor)

      @ms val guessedIdealTickDistance = (endTimestamp - startTimestamp) / guessedTickCount

      if (guessedIdealTickDistance == 0.0) {
        //zoomed in way too far!
        offsetTickDistance = DistanceMillis(1.0)
        offsetTickDomainValues.reset()

        tickDomainValues.reset()
        ticksFormatted.reset()
        return
      }

      /**
       * Calculate the offset ticks
       */
      val timeChartCalculator = paintingContext.chartSupport.timeChartCalculator(contentAreaTimeRange)

      //The distance between the offset ticks
      offsetTickDistance = TimeTickDistance.forOffsets(endTimestamp - startTimestamp)
      val offsetTicks = offsetTickDistance.calculateTicks(startTimestamp, endTimestamp, paintingContext.timeZone, false)

      offsetTickDomainValues.ensureSize(offsetTicks.size)
      offsetTicksFormatted.ensureSize(offsetTicks.size)
      offsetTicks.fastForEachIndexed { index, value ->
        offsetTickDomainValues[index] = value
        offsetTicksFormatted[index] = offsetTickDistance.formatAsOffset(value, paintingContext.i18nConfiguration)
      }

      //The minimum tick distance that is supported
      @Domain @ms val minTickDistance = timeChartCalculator.zoomed2timeDeltaX(calculateTickValueLabelWidth())

      val smallestPossibleTickDistance = offsetTickDistance.smallestPossibleTickDistance()
      val timeTickDistance: TimeTickDistance = TimeTickDistance.forTicks(minTickDistance).coerceAtLeast(smallestPossibleTickDistance)

      val allTicks: @Time @ms DoubleArray = timeTickDistance.calculateTicks(startTimestamp, endTimestamp, paintingContext.timeZone, true)


      val formatter = when (style.timestampsMode) {
        TimestampsMode.Absolute -> style.absoluteTimestampTickFormat
        TimestampsMode.Relative -> style.relativeTimestampsTickFormat
      }

      tickDomainValues.ensureSize(allTicks.size)
      ticksFormatted.ensureSize(allTicks.size)

      allTicks.fastForEachIndexed { index, value ->
        if (offsetTicks.fastContains(value)) {
          tickDomainValues[index] = Double.NaN
          ticksFormatted[index] = "-" //no value shall be painted
        } else {
          tickDomainValues[index] = value
          ticksFormatted[index] = formatter.format(value, timeTickDistance, paintingContext.i18nConfiguration)
        }
      }
    }
  }

  override fun paintTicksWithLabelsVertically(paintingContext: LayerPaintingContext, direction: Direction) {
    throw UnsupportedOperationException("Not implemented vertically!")
  }

  override fun paintTicksWithLabelsHorizontally(paintingContext: LayerPaintingContext, direction: Direction) {
    val timeChartCalculator = paintingContext.chartSupport.timeChartCalculator(paintingVariables.contentAreaTimeRange)

    //TODO why not in painting variables?
    val maxTickValueLabelWidth = calculateTickValueLabelWidth()
    val textAnchor = Direction.TopCenter

    val gc = paintingContext.gc
    gc.saved {
      gc.fillStyle(style.tickLabelColor())
      gc.strokeStyle(style.lineColor())
      gc.lineWidth = style.tickLineWidth
      gc.font(style.tickFont)

      //Save the total height
      paintingVariables.spaceForTickLabels = gc.getFontMetrics().totalHeight

      paintingVariables.tickDomainValues.fastForEachIndexed { index, tickValue: @Time Double ->
        if (tickValue.isFinite().not()) {
          return@fastForEachIndexed
        }

        @Window val currentX = timeChartCalculator.time2windowX(tickValue)

        //The tick
        if (style.tickLength > 0.0 && style.tickLineWidth > 0.0) {
          when (textAnchor) {
            Direction.BottomCenter -> gc.strokeLine(currentX, style.tickLabelGap + style.tickLength, currentX, style.tickLabelGap)
            Direction.TopCenter -> gc.strokeLine(currentX, -style.tickLabelGap, currentX, -style.tickLabelGap - style.tickLength)
            else -> throw IllegalArgumentException("Unsupported anchor direction: $textAnchor")
          }
        }

        val tickValueLabel = paintingVariables.ticksFormatted[index]

        //Debug output the bounds
        val snappedX = paintingContext.snapConfiguration.snapXValue(currentX)
        gc.fillText(tickValueLabel, snappedX, 0.0, textAnchor, 0.0, 0.0, maxTickValueLabelWidth, stringShortener = CanvasStringShortener.AllOrNothing)
      }
    }

    //Offset area
    gc.translate(0.0, paintingVariables.spaceForTickLabels + style.offsetAreaTickLabelGap)
    //to the top of the offset area
    timeChartCalculator.paintOffsetAreaHorizontal(paintingContext)
  }

  /**
   * Paints the offset area.
   * GC at the *top* of the offset area
   */
  private fun TimeChartCalculator.paintOffsetAreaHorizontal(paintingContext: LayerPaintingContext) {
    val timeZone = paintingContext.timeZone
    val gc = paintingContext.gc

    @ms val start = paintingVariables.startTimestamp
    @ms val end = paintingVariables.endTimestamp

    //Check if there is at least one tick visible
    val atLeastOneTickVisible = paintingVariables.offsetTickDomainValues.fastAny {
      it.betweenInclusive(start, end)
    }

    if (atLeastOneTickVisible) {
      //First paint the areas
      paintingVariables.offsetTickDomainValues.fastForEachIndexed { index, tickValue: @ms Double ->
        if (tickValue.isFinite().not()) {
          return@fastForEachIndexed
        }

        //The tick location of the *current* tick
        @Window val tickLocationX = time2windowX(tickValue)

        //Calculate the "bounds" for the *visible* offset area
        val minX: @Window Double = max(paintingVariables.axisStart, tickLocationX)

        val maxX: @Window Double = if (index < paintingVariables.offsetTickDomainValues.size - 1) {
          //At most until the next tick

          //Calculate the tick location for the *next* tick
          @ms val nextMillis = paintingVariables.offsetTickDomainValues[index + 1]
          @Window val nextMillisLocation = time2windowX(nextMillis)

          min(gc.width, nextMillisLocation)
        } else {
          paintingVariables.axisEnd
        }

        //Paint the background
        val estimatedIndex = paintingVariables.offsetTickDistance.calculateEstimatedIndex(tickValue, timeZone)
        gc.fill(style.offsetAreaFills.valueAt(estimatedIndex))
        gc.fillRectCoordinates(minX, 0.0, maxX, style.offsetAreaSize)

        @Zoomed val availableWidth = maxX - minX

        //Paint the label at the *right* side of the tick
        val formatted = paintingVariables.offsetTicksFormatted.get(index)
        val textWidth = gc.calculateTextWidth(formatted)

        if (availableWidth > textWidth) {
          //We have enough space for the label, paint at center
          @Window val textCenter = minX + (maxX - minX) / 2.0
          gc.font(style.offsetTickFont)
          gc.fill(style.offsetTickLabelColor)
          val snappedX = paintingContext.snapConfiguration.snapXValue(textCenter)
          gc.fillText(formatted, snappedX, style.offsetAreaSize / 2.0, Direction.Center)
        }
      }

      //Now paint the ticks - *over* the previously painted areas
      paintingVariables.offsetTickDomainValues.fastForEachIndexed { index, millis: @ms Double ->
        //The tick location of the *current* tick
        @Window val tickLocationX = time2windowX(millis)

        if (tickLocationX >= paintingVariables.axisStart && tickLocationX <= paintingVariables.axisEnd) {
          //paint the tick itself - if visible
          gc.lineWidth = style.tickLineWidth
          gc.stroke(style.lineColor())
          gc.strokeLine(
            tickLocationX,
            style.offsetAreaSize,
            tickLocationX,
            -style.offsetAreaTickLabelGap - style.tickLength - style.tickLabelGap - style.axisLineWidth - paintingVariables.spaceForTickLabels
          )
        }
      }
    } else {
      //We do not have any tick visible

      // Paint the last tick in the middle of the window
      @ms val millis = paintingVariables.offsetTickDomainValues.lastOr(paintingVariables.startTimestamp)

      //Paint the background
      gc.fill(style.offsetAreaFills.valueAt(paintingVariables.offsetTickDistance.calculateEstimatedIndex(millis, timeZone)))
      gc.fillRectCoordinates(paintingVariables.axisStart, 0.0, paintingVariables.axisEnd, style.offsetAreaSize)

      val x = gc.width / 2.0
      gc.font(style.offsetTickFont)
      gc.fill(style.offsetTickLabelColor)
      gc.fillText(paintingVariables.offsetTickDistance.formatAsOffset(millis, paintingContext.i18nConfiguration), x, style.offsetAreaSize / 2.0, Direction.Center)
    }
  }

  /**
   * Returns the max width for the tick value labels depending on the side of the axis
   */
  private fun calculateTickValueLabelWidth(): @px Double {
    return when (style.side) {
      Side.Bottom -> paintingVariables.tickValueLabelMaxWidth
      else -> throw UnsupportedOperationException("${style.side} is not supported yet")
    }
  }

  enum class TimestampsMode {
    Absolute,
    Relative
  }

  class Data(
    /**
     * The time range that is spans the content area
     */
    var contentAreaTimeRange: TimeRange = TimeRange.oneMinuteSinceReference,
  )

  /**
   * Style object for the time axis
   */
  class Style : AxisStyle() {

    init {
      //Ensure tick orientation outside
      this.tickOrientation = Vicinity.Outside
    }

    /**
     * The factor that is used to calculate the required space. Use with care.
     */
    @Deprecated("required???")
    var tickLabelWidthFactor: Double = 1.3

    /**
     * The mode to be used for the timestamps
     */
    var timestampsMode: TimestampsMode = TimestampsMode.Absolute

    /**
     * Is used to format the ticks with absolute timestamps
     */
    var absoluteTimestampTickFormat: TimeAxisTickFormat = OffsetAwareTickFormat

    /**
     * Is used to format the ticks with relative timestamps
     */
    var relativeTimestampsTickFormat: RelativeTickFormat = RelativeToNowTickFormat

    /**
     * If set to true an offset is painted in an own area
     */
    @Deprecated("offset is always used")
    var useOffset: Boolean = true

    /**
     * The size of the offset area
     */
    var offsetAreaSize: @Positive Double = 30.0

    /**
     * The gap between the tick label and the offset area
     */
    var offsetAreaTickLabelGap: @px Double = 7.0

    /**
     * The fills that are used for the offset areas.
     */
    var offsetAreaFills: MultiProvider<GlobalTimeIndex, Color> = MultiProvider.modulo(Color.web("#DBE1E5"), Color.web("#F3F5F7"))

    /**
     * The colors of the offset area ticks
     */
    var offsetTickLabelColor: Color = Theme.axisTickColor()

    /**
     * The font for the offset ticks
     */
    var offsetTickFont: FontDescriptorFragment = Theme.offsetTickFont()
  }
}

/**
 * Formats ticks of a time axis
 */
interface TimeAxisTickFormat {
  /**
   * Computes the maximum length of a formatted tick value for the given [tickDistance]
   */
  fun computeMaxTickLength(paintingContext: LayerPaintingContext, tickDistance: TimeTickDistance): @px Double

  /**
   * Formats a tick
   * @param tick the timestamp assigned to the tick
   * @param tickDistance the distance between two ticks
   * @param i18nConfiguration the locale to be used to format the tick
   */
  fun format(tick: @ms Double, tickDistance: TimeTickDistance, i18nConfiguration: I18nConfiguration): String
}

/**
 * Formats ticks in accordance to their distance to each other
 */
@Deprecated("No longer used in production")
object TickDistanceAwareTickFormat : TimeAxisTickFormat {
  /**
   * Computes the [DateTimeFormat] to be used for the given timestamp
   */
  @Suppress("UNUSED_PARAMETER")
  private fun computeDateFormat(tickDistance: TimeTickDistance): DateTimeFormat {
    throw UnsupportedOperationException("Not implemented anymore!")
    //return when (tickDistance) {
    //  is DistanceMillis -> dateTimeFormatWithMillis
    //  is DistanceSeconds -> dateTimeFormatWithMillis
    //  is DistanceMinutes -> dateFormat
    //  is DistanceHours -> dateTimeFormat
    //  is DistanceDays -> dateTimeFormat
    //  is DistanceMonths -> dateFormat
    //  is DistanceYears -> dateFormat
    //}
  }

  override fun format(tick: @ms Double, tickDistance: TimeTickDistance, i18nConfiguration: I18nConfiguration): String {
    return computeDateFormat(tickDistance).format(tick, i18nConfiguration)
  }

  override fun computeMaxTickLength(paintingContext: LayerPaintingContext, tickDistance: TimeTickDistance): @px Double {
    return paintingContext.gc.calculateTextWidth(
      format(
        1605705138944.777, //an arbitrary timestamp with a milliseconds part not equal to 0
        tickDistance,
        paintingContext.i18nConfiguration
      )
    )
  }
}

/**
 * Only formats the part that is not visualized by the offset
 */
object OffsetAwareTickFormat : TimeAxisTickFormat {
  /**
   * Computes the [DateTimeFormat] to be used for the given timestamp
   */
  private fun computeDateFormat(tickDistance: TimeTickDistance): DateTimeFormat {
    return when (tickDistance) {
      is DistanceMillis -> secondMillisFormat
      is DistanceSeconds -> timeFormat
      is DistanceMinutes -> timeFormat
      is DistanceHours -> timeFormat
      is DistanceDays -> dateFormat
      is DistanceMonths -> dateFormat
      is DistanceYears -> yearFormat
    }
  }

  override fun format(tick: Double, tickDistance: TimeTickDistance, i18nConfiguration: I18nConfiguration): String {
    return computeDateFormat(tickDistance).format(tick, i18nConfiguration)
  }

  override fun computeMaxTickLength(paintingContext: LayerPaintingContext, tickDistance: TimeTickDistance): @px Double {
    return paintingContext.gc.calculateTextWidth(
      format(
        1605705138944.777, //an arbitrary timestamp with a milliseconds part not equal to 0
        tickDistance,
        paintingContext.i18nConfiguration
      )
    )
  }
}

/**
 * Formats any tick in accordance to the ISO 8601 format.
 */
object Iso8601TickFormat : TimeAxisTickFormat {
  override fun format(tick: Double, tickDistance: TimeTickDistance, i18nConfiguration: I18nConfiguration): String {
    return dateTimeFormatIso8601.format(tick, i18nConfiguration)
  }

  override fun computeMaxTickLength(paintingContext: LayerPaintingContext, tickDistance: TimeTickDistance): @px Double {
    return paintingContext.gc.calculateTextWidth(
      format(
        TimeConstants.referenceTimestamp,
        tickDistance,
        paintingContext.i18nConfiguration
      )
    )
  }
}

/**
 * Formats ticks that represent a distance to a certain timestamp
 */
interface RelativeTickFormat : TimeAxisTickFormat {
}

/**
 * Formats ticks relative to their distance from [nowMillis]
 */
object RelativeToNowTickFormat : RelativeTickFormat {
  private val timeUnitsDescending = listOf(
    TimeUnits.Year,
    TimeUnits.Month,
    TimeUnits.Day,
    TimeUnits.Hour,
    TimeUnits.Minute,
    TimeUnits.Second,
    TimeUnits.Millisecond
  )

  private val maxLengthFormattedTick: String = "-999 ${TimeUnits.Year.unit} 99 ${TimeUnits.Month.unit} 99 ${TimeUnits.Day.unit} 99 ${TimeUnits.Hour.unit} 99 ${TimeUnits.Minute.unit} 99 ${TimeUnits.Second.unit} 999 ${TimeUnits.Millisecond.unit}"

  override fun format(tick: Double, tickDistance: TimeTickDistance, i18nConfiguration: I18nConfiguration): String {
    @Domain @ms val now = Meistercharts.renderLoop.currentFrameTimestamp
    @Domain @ms val distance = now - tick
    @Domain @ms var distanceAbsolute = abs(distance)
    val sign = if (distance < 0) "+" else "-"
    val formattedDistance = StringBuilder().append(sign)
    for (timeUnit in timeUnitsDescending) {
      val timeUnitValue = (distanceAbsolute / timeUnit.span).toInt()
      if (timeUnitValue > 0 || formattedDistance.isNotEmpty()) {
        formattedDistance.append(timeUnitValue).append(' ').append(timeUnit.unit).append(' ')
      }
      distanceAbsolute -= timeUnitValue * timeUnit.span
    }
    return formattedDistance.toString().trim()
  }

  override fun computeMaxTickLength(paintingContext: LayerPaintingContext, tickDistance: TimeTickDistance): Double {
    return paintingContext.gc.calculateTextWidth(maxLengthFormattedTick)
  }
}

/**
 * Adds a time axis layer
 */
fun Layers.addTimeAxis(contentAreaTimeRange: TimeRange, styleConfiguration: TimeAxisLayer.Style.() -> Unit = {}): TimeAxisLayer {
  return TimeAxisLayer(TimeAxisLayer.Data(contentAreaTimeRange), styleConfiguration).also {
    addLayer(it)
  }
}
