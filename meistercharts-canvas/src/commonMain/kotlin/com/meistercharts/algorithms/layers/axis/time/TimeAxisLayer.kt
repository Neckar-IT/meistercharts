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
package com.meistercharts.algorithms.layers.axis.time

import com.meistercharts.Meistercharts
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.layers.barchart.AbstractAxisLayer
import com.meistercharts.algorithms.layers.timeChartCalculator
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.axis.time.DistanceDays
import com.meistercharts.axis.time.DistanceHours
import com.meistercharts.axis.time.DistanceMillis
import com.meistercharts.axis.time.DistanceMinutes
import com.meistercharts.axis.time.DistanceMonths
import com.meistercharts.axis.time.DistanceSeconds
import com.meistercharts.axis.time.DistanceYears
import com.meistercharts.axis.time.GlobalTimeIndex
import com.meistercharts.axis.time.TimeTickDistance
import com.meistercharts.axis.time.TimeUnits
import com.meistercharts.axis.time.valueAt
import com.meistercharts.calc.TimeChartCalculator
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.text.CanvasStringShortener
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.design.Theme
import com.meistercharts.design.multiProvider
import com.meistercharts.font.FontDescriptorFragmentProvider
import com.meistercharts.model.Vicinity
import com.meistercharts.time.TimeRange
import it.neckar.datetime.minimal.TimeConstants
import it.neckar.geometry.Direction
import it.neckar.geometry.Orientation
import it.neckar.geometry.Side
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.fastContains
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.formatting.DateTimeFormat
import it.neckar.open.formatting.dateFormat
import it.neckar.open.formatting.dateTimeFormatIso8601
import it.neckar.open.formatting.secondMillisFormat
import it.neckar.open.formatting.timeFormat
import it.neckar.open.formatting.yearFormat
import it.neckar.open.annotations.AllocationCost
import it.neckar.open.annotations.Allocates
import it.neckar.open.annotations.Hot
import it.neckar.open.annotations.HotAllocation
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.betweenInclusive
import it.neckar.open.provider.MultiProvider
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
 * @property configuration - contains the timeRange the time range to be displayed at 100% zoom and with no translation
 * @param additionalConfiguration the style configuration that is applied
 */
class TimeAxisLayer(
  override val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractAxisLayer() {

  constructor(
    contentAreaTimeRange: TimeRange = TimeRange.oneMinuteSinceReference,
    additionalConfiguration: Configuration.() -> Unit = {},
  ) : this(Configuration(contentAreaTimeRange), additionalConfiguration)

  init {
    configuration.apply {
      side = Side.Bottom
    }.also(additionalConfiguration)
  }

  override val type: LayerType
    get() = LayerType.Content

  @Hot
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
    @Hot
    override fun calculate(paintingContext: LayerPaintingContext) {
      reset()

      contentAreaTimeRange = configuration.contentAreaTimeRange

      //TODO!!!! fix me somehow!
      calculateTickFontMetrics(paintingContext, configuration)
      calculateTitle(paintingContext, configuration)

      calculateAxisStartEnd(paintingContext, configuration)
      calculateDomainStartEndValues(paintingContext, configuration)

      calculateTickLabelsMaxWidth(configuration)
      calculateLocations(paintingContext, configuration)

      calculateTickValues(paintingContext, configuration)
    }

    /**
     * Calculate the tick values that are painted
     */
    @Hot
    private fun calculateTickValues(paintingContext: LayerPaintingContext, style: Configuration) {
      return when (style.orientation) {
        Orientation.Vertical -> throw UnsupportedOperationException("Only implemented for horizontal orientation at the moment!")
        Orientation.Horizontal -> calculateTickValuesValueRangeHorizontally(paintingContext, style)
      }
    }

    /**
     * Calculates the tick values
     */
    @Hot
    private fun calculateTickValuesValueRangeHorizontally(paintingContext: LayerPaintingContext, style: Configuration) {
      //Note: in order to know how many formatted ticks may be displayed along this axis
      //we need to know the text length of one formatted tick. However, to compute the
      //text length of one formatted tick we need to know the distance between two ticks
      //which in turn requires to know how many ticks are there. So either we need to compute
      //the layout at least twice or we simplify matters by assuming that a tick label is
      //about 100 pixels wide.
      val guessedTickCount = paintingContext.width / (100.0 * this@TimeAxisLayer.configuration.tickLabelWidthFactor)

      @ms val guessedIdealTickDistance = (endTimestamp - startTimestamp) / guessedTickCount

      if (guessedIdealTickDistance == 0.0) {
        //zoomed in way too far!
        offsetTickDistance = DistanceMillis.smallest
        offsetTickLabels.reset()

        tickDistance = DistanceMillis.smallest
        tickLabels.reset()
        return
      }

      /**
       * Calculate the offset ticks
       */
      @HotAllocation("Once per frame per layer - calculator factory allocation. Fix candidate: cache in ChartState (TODO exists at the factory)")
      val timeChartCalculator = paintingContext.chartSupport.timeChartCalculator(contentAreaTimeRange)

      //The distance between the offset ticks
      @HotAllocation("Once per frame per layer - allocates the selected TimeTickDistance instance")
      val newOffsetTickDistance = TimeTickDistance.forOffsets(endTimestamp - startTimestamp)
      offsetTickDistance = newOffsetTickDistance

      @HotAllocation("Once per frame per layer - allocates the offset tick list (a handful of entries). Fix candidate: fill-into variant on TimeTickDistance")
      val offsetTicks = newOffsetTickDistance.calculateTicks(startTimestamp, endTimestamp, paintingContext.timeZone)

      offsetTickLabels.prepare(offsetTicks.size)
      offsetTicks.fastForEachIndexed { index, value ->
        @HotAllocation("Once per offset tick per frame - formatted offset label string. Fix candidate: cache formatted labels per tick value; tick values only change on zoom/pan")
        val formatted = newOffsetTickDistance.formatAsOffset(value, paintingContext.i18nConfiguration)
        offsetTickLabels.set(index, value, formatted)
      }

      //The minimum tick distance that is supported
      @Domain @ms val minTickDistance = timeChartCalculator.zoomed2timeDeltaX(calculateTickValueLabelWidth())

      val smallestPossibleTickDistance = newOffsetTickDistance.smallestPossibleTickDistance()

      @HotAllocation("Once per frame per layer - allocates the selected TimeTickDistance instance")
      val timeTickDistance: TimeTickDistance = TimeTickDistance.forTicks(minTickDistance).coerceAtLeast(smallestPossibleTickDistance)

      @HotAllocation("Once per frame per layer - allocates the tick list, size = visible tick count. Fix candidate: fill-into variant on TimeTickDistance")
      val allTicks: @ms DoubleArrayList = timeTickDistance.calculateTicks(startTimestamp, endTimestamp, paintingContext.timeZone)


      val formatter: TimeAxisTickFormat = when (style.timestampsMode) {
        TimestampsMode.Absolute -> style.absoluteTimestampTickFormat
        TimestampsMode.Relative -> style.relativeTimestampsTickFormat
      }

      tickLabels.prepare(allTicks.size)

      allTicks.fastForEachIndexed { index, value ->
        if (offsetTicks.fastContains(value)) {
          tickLabels.set(index, Double.NaN, "-") //no value shall be painted, there is an offset tick already
        } else {
          @HotAllocation("Once per tick per frame - formatted tick label string. Fix candidate: cache formatted labels per tick value; tick values only change on zoom/pan")
          val formatted = formatter.format(value, timeTickDistance, paintingContext.i18nConfiguration)
          tickLabels.set(index, value, formatted)
        }
      }
    }
  }

  @Hot
  override fun paintTicksWithLabelsVertically(paintingContext: LayerPaintingContext, direction: Direction) {
    throw UnsupportedOperationException("Not implemented vertically!")
  }

  @Hot
  override fun paintTicksWithLabelsHorizontally(paintingContext: LayerPaintingContext, direction: Direction) {
    @HotAllocation("Once per frame per layer - calculator factory allocation. Fix candidate: cache in ChartState (TODO exists at the factory)")
    val timeChartCalculator = paintingContext.chartSupport.timeChartCalculator(paintingVariables.contentAreaTimeRange)

    //TODO why not in painting variables?
    val maxTickValueLabelWidth = calculateTickValueLabelWidth()
    val textAnchor = Direction.TopCenter

    val gc = paintingContext.gc
    gc.saved {
      gc.fillStyle(configuration.tickLabelColor())
      gc.strokeStyle(configuration.lineColor())
      gc.lineWidth = configuration.tickLineWidth
      @HotAllocation("Once per frame per axis - the fragment-to-descriptor conversion is cached")
      gc.font(configuration.tickFont())

      //Save the total height
      paintingVariables.spaceForTickLabels = gc.getFontMetrics().totalHeight

      paintingVariables.tickLabels.fastForEachIndexed { _, tickValue: @Time Double, tickValueLabel: String ->
        if (tickValue.isFinite().not()) {
          return@fastForEachIndexed
        }

        @Window val currentX = timeChartCalculator.time2windowX(tickValue)

        //The tick
        if (configuration.tickLength > 0.0 && configuration.tickLineWidth > 0.0) {
          when (textAnchor) {
            Direction.BottomCenter -> gc.strokeLine(currentX, configuration.tickLabelGap + configuration.tickLength, currentX, configuration.tickLabelGap)
            Direction.TopCenter -> gc.strokeLine(currentX, -configuration.tickLabelGap, currentX, -configuration.tickLabelGap - configuration.tickLength)
            else -> throw IllegalArgumentException("Unsupported anchor direction: $textAnchor")
          }
        }

        //Debug output the bounds
        val snappedX = paintingContext.snapConfiguration.snapXValue(currentX)
        @HotAllocation("Once per tick per frame - platform text rendering; tick count is bounded by axis length / label width")
        gc.fillText(tickValueLabel, snappedX, 0.0, textAnchor, 0.0, 0.0, maxTickValueLabelWidth, stringShortener = CanvasStringShortener.AllOrNothing)
      }
    }

    //Offset area
    gc.translate(0.0, paintingVariables.spaceForTickLabels + configuration.offsetAreaTickLabelGap)
    //to the top of the offset area
    timeChartCalculator.paintOffsetAreaHorizontal(paintingContext)
  }

  /**
   * Paints the offset area.
   * GC at the *top* of the offset area
   */
  @Hot
  private fun TimeChartCalculator.paintOffsetAreaHorizontal(paintingContext: LayerPaintingContext) {
    val timeZone = paintingContext.timeZone
    val gc = paintingContext.gc

    @ms val start = paintingVariables.startTimestamp
    @ms val end = paintingVariables.endTimestamp

    //Check if there is at least one tick visible
    val atLeastOneTickVisible = paintingVariables.offsetTickLabels.values.fastAny {
      it.betweenInclusive(start, end)
    }

    if (atLeastOneTickVisible) {
      //First paint the areas
      paintingVariables.offsetTickLabels.values.fastForEachIndexed { index, tickValue: @ms Double ->
        if (tickValue.isFinite().not()) {
          return@fastForEachIndexed
        }

        //The tick location of the *current* tick
        @Window val tickLocationX = time2windowX(tickValue)

        //Calculate the "bounds" for the *visible* offset area
        val minX: @Window Double = max(paintingVariables.axisStart, tickLocationX)

        val maxX: @Window Double = if (index < paintingVariables.offsetTickLabels.size - 1) {
          //At most until the next tick

          //Calculate the tick location for the *next* tick
          @ms val nextMillis = paintingVariables.offsetTickLabels.valueAt(index + 1)
          @Window val nextMillisLocation = time2windowX(nextMillis)

          min(gc.width, nextMillisLocation)
        } else {
          paintingVariables.axisEnd
        }

        //Paint the background
        @HotAllocation("Once per offset tick per frame - date decomposition to estimate the global index; offset tick count is a handful per frame")
        val estimatedIndex = paintingVariables.offsetTickDistance.calculateEstimatedIndex(tickValue, timeZone)
        gc.fill(configuration.offsetAreaFills.valueAt(estimatedIndex))
        gc.fillRectCoordinates(minX, 0.0, maxX, configuration.offsetAreaSize)

        @Zoomed val availableWidth = maxX - minX

        //Paint the label at the *right* side of the tick
        val formatted = paintingVariables.offsetTickLabels.formattedAt(index)
        val textWidth = gc.calculateTextWidth(formatted)

        if (availableWidth > textWidth) {
          //We have enough space for the label, paint at center
          @Window val textCenter = minX + (maxX - minX) / 2.0
          @HotAllocation("Once per offset tick per frame - the fragment-to-descriptor conversion is cached")
          gc.font(configuration.offsetTickFont())
          gc.fill(configuration.offsetTickLabelColor)
          val snappedX = paintingContext.snapConfiguration.snapXValue(textCenter)
          @HotAllocation("Once per offset tick per frame - platform text rendering; offset tick count is a handful per frame")
          gc.fillText(formatted, snappedX, configuration.offsetAreaSize / 2.0, Direction.Center)
        }
      }

      //Now paint the ticks - *over* the previously painted areas
      paintingVariables.offsetTickLabels.values.fastForEachIndexed { index, millis: @ms Double ->
        //The tick location of the *current* tick
        @Window val tickLocationX = time2windowX(millis)

        if (tickLocationX >= paintingVariables.axisStart && tickLocationX <= paintingVariables.axisEnd) {
          //paint the tick itself - if visible
          gc.lineWidth = configuration.tickLineWidth
          gc.stroke(configuration.lineColor())
          gc.strokeLine(
            tickLocationX,
            configuration.offsetAreaSize,
            tickLocationX,
            -configuration.offsetAreaTickLabelGap - configuration.tickLength - configuration.tickLabelGap - configuration.axisLineWidth - paintingVariables.spaceForTickLabels
          )
        }
      }
    } else {
      //We do not have any tick visible

      // Paint the last tick *before* the window in the middle of the window
      @ms val millis = paintingVariables.offsetTickLabels.values.lastOr(paintingVariables.startTimestamp) {
        it < paintingVariables.startTimestamp
      }

      //Paint the background
      @HotAllocation("Once per frame per layer (no visible offset tick) - date decomposition to estimate the global index")
      val estimatedIndex = paintingVariables.offsetTickDistance.calculateEstimatedIndex(millis, timeZone)
      gc.fill(configuration.offsetAreaFills.valueAt(estimatedIndex))
      gc.fillRectCoordinates(paintingVariables.axisStart, 0.0, paintingVariables.axisEnd, configuration.offsetAreaSize)

      val x = gc.width / 2.0
      @HotAllocation("Once per frame per layer (no visible offset tick) - the fragment-to-descriptor conversion is cached")
      gc.font(configuration.offsetTickFont())
      gc.fill(configuration.offsetTickLabelColor)
      @HotAllocation("Once per frame per layer (no visible offset tick) - formatted offset label string")
      val formattedOffset = paintingVariables.offsetTickDistance.formatAsOffset(millis, paintingContext.i18nConfiguration)
      @HotAllocation("Once per frame per layer (no visible offset tick) - platform text rendering")
      gc.fillText(formattedOffset, x, configuration.offsetAreaSize / 2.0, Direction.Center)
    }
  }

  /**
   * Returns the max width for the tick value labels depending on the side of the axis
   */
  @Hot
  private fun calculateTickValueLabelWidth(): @px Double {
    return when (configuration.side) {
      Side.Bottom -> paintingVariables.tickValueLabelMaxWidth
      else -> throw UnsupportedOperationException("${configuration.side} is not supported yet")
    }
  }

  enum class TimestampsMode {
    Absolute,
    Relative
  }

  /**
   * Style object for the time axis
   */
  @ConfigurationDsl
  class Configuration(
    /**
     * The time range that is spans the content area
     */
    var contentAreaTimeRange: TimeRange = TimeRange.oneMinuteSinceReference,
  ) : AxisConfiguration() {

    init {
      //Ensure tick orientation outside
      this.tickOrientation = Vicinity.Outside
    }

    /**
     * The factor that is used to calculate the required space. Use with care.
     */
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
    var offsetAreaFills: MultiProvider<GlobalTimeIndex, Color> = Theme.backgroundZebra.multiProvider()
    /**
     * The colors of the offset area ticks
     */
    var offsetTickLabelColor: ColorProvider = Theme.axisTickColor.provider()

    /**
     * The font for the offset ticks
     */
    var offsetTickFont: FontDescriptorFragmentProvider = Theme.offsetTickFont.provider()
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
  @Allocates(AllocationCost.Constant)
  fun format(tick: @ms Double, tickDistance: TimeTickDistance, i18nConfiguration: I18nConfiguration): String
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
    //Suppress leading zero-value units - start appending at the first non-zero unit
    var valueAppended = false
    for (timeUnit in timeUnitsDescending) {
      val timeUnitValue = (distanceAbsolute / timeUnit.span).toInt()
      if (timeUnitValue > 0 || valueAppended) {
        formattedDistance.append(timeUnitValue).append(' ').append(timeUnit.unit).append(' ')
        valueAppended = true
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
fun Layers.addTimeAxis(contentAreaTimeRange: TimeRange, additionalConfiguration: TimeAxisLayer.Configuration.() -> Unit = {}): TimeAxisLayer {
  return TimeAxisLayer(TimeAxisLayer.Configuration(contentAreaTimeRange), additionalConfiguration).also {
    addLayer(it)
  }
}
