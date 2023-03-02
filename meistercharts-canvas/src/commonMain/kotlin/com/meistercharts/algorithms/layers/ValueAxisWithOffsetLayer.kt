package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.axis.OffsetTickCalculator
import com.meistercharts.algorithms.layers.barchart.AbstractAxisLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import it.neckar.open.unit.number.Positive
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.design.Theme
import com.meistercharts.model.Direction
import com.meistercharts.model.Orientation
import com.meistercharts.model.Vicinity
import com.meistercharts.provider.ValueRangeProvider
import it.neckar.open.collections.emptyDoubleArray
import it.neckar.open.collections.fastMapDouble
import it.neckar.open.kotlin.lang.abs
import it.neckar.open.kotlin.lang.findMagnitude
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Paints a value axis with an additional offset to reduce the amount of horizontal space needed to display values
 */
class ValueAxisWithOffsetLayer constructor(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {},
) : AbstractAxisLayer() {

  constructor(
    title: String,
    valueRange: ValueRange,
    styleConfiguration: Style.() -> Unit = {},
  ) : this(
    Data(valueRangeProvider = { valueRange }),
    {
      titleProvider = { _, _ -> title }
      styleConfiguration()
    },
  )

  override val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paintingVariables(): ValueAxisWithOffsetPaintingVariables {
    return paintingVariables
  }

  //Painting variable - stores intermediate results required for painting
  private val paintingVariables = object : ValueAxisWithOffsetPaintingVariablesImpl() {

    override fun calculate(paintingContext: LayerPaintingContext) {
      reset()

      contentAreaValueRange = data.valueRangeProvider()

      calculateTickFontMetrics(paintingContext, style)


      calculateEstimatedTickFormatMaxLength(paintingContext, style)
      calculateTitle(paintingContext, style)

      calculateAxisStartEnd(paintingContext, style)

      calculateDomainStartEndValues(paintingContext, style)

      calculateTickLabelsMaxWidth(style)
      calculateLocations(paintingContext, style)

      storeTicks(calculateTickValues(paintingContext), paintingContext, style)
    }

    /**
     * Calculate the tick values that are painted
     */
    private fun calculateTickValues(paintingContext: LayerPaintingContext): @Domain DoubleArray {
      return when (style.orientation) {
        Orientation.Vertical -> calculateTickValuesValueRangeVertically(fontHeight = tickFontMetrics.totalHeight)
        Orientation.Horizontal -> calculateTickValuesValueRangeHorizontally(maxFormattedLabelWidth = estimatedTickFormatMaxLength)
      }
    }

    private fun calculateTickValuesValueRangeHorizontally(maxFormattedLabelWidth: @px Double): @Domain DoubleArray {
      //TODO implement me!
      return doubleArrayOf(10.0, 15.0)
    }

    private fun calculateTickValuesValueRangeVertically(fontHeight: @px Double): @Domain DoubleArray {
      //TODO implement me
      return doubleArrayOf(11.0, 16.0)
    }

    /**
     * Calculates the offset index for the given value.
     * The index
     */
    fun offsetIndexForValue(value: Double): OffsetIndex {
      return if (deltaMagnitude < integerDigits) {
        (value / 10.0.pow(integerDigits))
      } else {
        (value / offsetStep)
      }.toInt()
        .let { OffsetIndex(it) }
    }
  }

  ///**
  // * Painting strategy for the layer:
  // *
  // * Paint from left to right. Always translate the gc after each segment
  // */
  //override fun paint(paintingContext: LayerPaintingContext) {
  //  with(paintingContext.chartCalculator) {
  //    paintLeft(paintingContext)
  //  }
  //}

  private fun ChartCalculator.paintLeft(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.translate(style.margin.left, 0.0)
    //apply the margin

    //Paint the title (if there is one and remember the width that is required for the title + gap)
    paintTitle(paintingContext)

    gc.translate(paintingVariables.spaceForTitleIncludingGap, 0.0)
    //to the right of the title

    //Paint the offset area
    paintOffsetAreaVertical(paintingContext)

    //Paint depending on the tick orientation
    when (style.tickOrientation) {
      Vicinity.Outside -> {
        @px val maxTickValueWidth = calculateTickValueLabelWidth() - style.tickLength - style.tickLabelGap
        gc.translate(maxTickValueWidth, 0.0)
        //to the right side of the tick value labels
        paintTicksWithLabelsVertically(paintingContext, maxTickValueWidth, Direction.CenterRight)
        gc.translate(style.tickLabelGap + style.tickLength + style.axisLineWidth / 2.0, 0.0)
        //to the center of the axis
        paintAxis(gc)
      }

      Vicinity.Inside -> {
        gc.translate(style.offsetAreaSize, 0.0)
        //to the right of the offset area
        gc.translate(style.axisLineWidth / 2.0, 0.0)
        //to the *center* of the axis
        paintAxis(gc)
        gc.translate(style.axisLineWidth / 2.0, 0.0)
        //to the right side of the axis
        gc.translate(style.tickLabelGap + style.tickLength, 0.0)
        //to the left side of the label
        paintTicksWithLabelsVertically(paintingContext, calculateTickValueLabelWidth(), Direction.CenterLeft)
      }
    }
  }

  /**
   * Returns the max width for the tick value labels depending on the side of the axis
   */
  private fun calculateTickValueLabelWidth(): Double {
    return style.size - paintingVariables.spaceForTitleIncludingGap
  }

  /**
   * Calculate the tick values that are painted
   */
  private fun ChartCalculator.calculateTickValues(paintingContext: LayerPaintingContext): @Domain DoubleArray {
    return when (style.orientation) {
      Orientation.Vertical -> calculateTickValuesValueRangeVertically(paintingContext, paintingVariables.tickFontMetrics.totalHeight)
      Orientation.Horizontal -> throw UnsupportedOperationException("Not yet implemented for horizontal")
    }
  }

  /**
   * Calculates the ticks - only for the area that is visible!
   */
  private fun ChartCalculator.calculateTickValuesValueRangeVertically(paintingContext: LayerPaintingContext, fontHeight: @px Double): @Domain DoubleArray {
    //Calculate the height that is used for the axis
    //Only a part of the canvas height might be used for the axis
    @Window val relevantUpperWindow = paintingVariables.axisStart
    @Window val relevantLowerWindow = paintingVariables.axisEnd

    //The height of the axis
    @Zoomed val relevantHeight = relevantLowerWindow - relevantUpperWindow

    //If we are below or above the screen, the relevant height is negative
    if (relevantHeight <= 0) {
      paintingVariables.offsetTicks.ensureSize(0) //TODO check me!
      return emptyDoubleArray()
    }

    val valueRange = data.valueRangeProvider()
    @Domain val relevantUpperDomain = window2domainY(paintingVariables.axisStart, valueRange)
    @Domain val relevantLowerDomain = window2domainY(paintingVariables.axisEnd, valueRange)

    //Sort the values depending on the orientation
    paintingVariables.startDomainValue = min(relevantUpperDomain, relevantLowerDomain)
    paintingVariables.endDomainValue = max(relevantUpperDomain, relevantLowerDomain)

    var maxTickCount = (relevantHeight / (fontHeight * 2.0) + 0.5).roundToInt()

    var newTicks = style.ticks.getTicks(paintingVariables.startDomainValue, paintingVariables.endDomainValue, maxTickCount, 0.0, style.axisEndConfiguration)
    calculateOffsetTickValuesVertically(newTicks)

    //If there are least 3 different offsets, consider recalculating the ticks should the offsets not fit inside their areas
    if (paintingVariables.offsetTicks.size >= 3) {
      val gc = paintingContext.gc
      gc.font(style.offsetTickFont)

      val relevantUpperOffsetStepDomain = domain2windowY(paintingVariables.offsetTicks[1], valueRange)
      val relevantLowerOffsetStepDomain = domain2windowY(paintingVariables.offsetTicks[2], valueRange)
      //The height of an offset step
      val offsetStepHeight = relevantUpperOffsetStepDomain - relevantLowerOffsetStepDomain

      //The height of the longest offset label
      val offsetLabelHeight = gc.calculateTextWidth("+ FIXME°!!!") //TODO
      //val offsetLabelHeight = gc.calculateTextWidth("+ ${paintingVariables.offsetTicks.maxOf { it.abs() }.format(1)}")

      //If the longest offset label does not fit into the offsetStepHeight, recalculate
      if (offsetLabelHeight >= offsetStepHeight) {
        maxTickCount = (relevantHeight / offsetLabelHeight).roundToInt()
        newTicks = style.ticks.getTicks(paintingVariables.startDomainValue, paintingVariables.endDomainValue, maxTickCount, 0.0, style.axisEndConfiguration)
        calculateOffsetTickValuesVertically(newTicks)
      }
    }

    return newTicks
  }

  private fun calculateOffsetTickValuesVertically(newTicks: @Domain DoubleArray) {
    with(paintingVariables) {
      deltaMagnitude = if (startDomainValue == endDomainValue) 1 - style.spaceForDigits else (startDomainValue - endDomainValue).abs().findMagnitude()
      fractionDigits = (1 - deltaMagnitude).coerceIn(0, style.spaceForDigits - 1)
      integerDigits = style.spaceForDigits - fractionDigits

      val newOffsetTicks = newTicks.fastMapDouble { tick ->
        OffsetTickCalculator.offsetForNumber(tick, integerDigits).let {
          //We use this comparison as this function can return both 0.0 and -0.0 values and `distinct` does differentiate between those two values
          if (it == 0.0) 0.0 else it
        }
      }.distinct().toMutableList()
      //If there is more than one offset tick, add an additional offset tick that would be missing otherwise
      if (newOffsetTicks.size > 1) {
        offsetStep = (newOffsetTicks[1] - newOffsetTicks.first()).abs()
        if (offsetStep > 0.0) {
          newOffsetTicks.add(0, newOffsetTicks.first() - offsetStep)
        }
      } else {
        offsetStep = 10.0.pow(deltaMagnitude + 2)
        newOffsetTicks.add(0, newOffsetTicks.first() - offsetStep)
      }

      TODO()
      //offsetTicks = newOffsetTicks.toDoubleArray()
    }
  }

  /**
   * Paints the ticks and the labels.
   * Must be called with the graphics context translated to the *edge* of the tick value labels
   *
   * Supports both directions of painting!
   */
  private fun ChartCalculator.paintTicksWithLabelsVertically(paintingContext: LayerPaintingContext, @px maxTickValueLabelWidth: Double, tickLabelsAnchorDirection: Direction) {
    val gc = paintingContext.gc

    gc.fillStyle(style.tickLabelColor())
    gc.strokeStyle(style.lineColor())
    gc.font(style.tickFont)
    gc.lineWidth = style.tickLineWidth

    val valueRange = data.valueRangeProvider()

    TODO()
    //paintingVariables.tickDomainValues.fastForEachIndexed { index, tickValue ->
    //  if (paintingVariables.offsetTicks.size <= 1 || !paintingVariables.offsetTicks.fastAny { tickValue == it }) {
    //    @px val currentY = domain2windowY(tickValue, valueRange)
    //
    //    //The tick line
    //    if (style.tickLength > 0.0 && style.tickLineWidth > 0.0) {
    //      when (tickLabelsAnchorDirection) {
    //        Direction.CenterLeft -> gc.strokeLine(-style.tickLabelGap - style.tickLength, currentY, -style.tickLabelGap, currentY)
    //        Direction.CenterRight -> gc.strokeLine(style.tickLabelGap, currentY, style.tickLabelGap + style.tickLength, currentY)
    //        else -> throw IllegalArgumentException("Unsupported anchor direction: $tickLabelsAnchorDirection")
    //      }
    //    }
    //
    //    //The tick label
    //    val formattedTick = paintingVariables.ticksFormatted[index]
    //    gc.fillText(formattedTick, 0.0, currentY, tickLabelsAnchorDirection, 0.0, 0.0, maxTickValueLabelWidth, stringShortener = style.valueLabelStringShortener)
    //  }
    //}
  }

  /**
   * Paints the axis - respects the paint range from the style
   */
  private fun paintAxis(gc: CanvasRenderingContext) {
    gc.strokeStyle(style.lineColor())

    if (style.axisLineWidth == 0.0) {
      return
    }

    gc.lineWidth = style.axisLineWidth
    gc.strokeLine(0.0, paintingVariables.axisStart, 0.0, paintingVariables.axisEnd)
  }

  /**
   * Paints the offset area
   */
  private fun ChartCalculator.paintOffsetAreaVertical(paintingContext: LayerPaintingContext) {
    TODO()
    //val gc = paintingContext.gc
    //
    ////Check if there is at least one tick visible
    //
    //val ticksVisible = paintingVariables.offsetTicks.fastAny {
    //  it.betweenInclusive(paintingVariables.startDomainValue, paintingVariables.endDomainValue)
    //}
    //
    //val valueRange = data.valueRangeProvider()
    //
    //if (ticksVisible) {
    //  //First paint the areas
    //  paintingVariables.offsetTicks.fastForEachIndexed { index, value: Double ->
    //    //The tick location of the *current* tick
    //    @Window val minY = max(paintingVariables.axisStart, domain2windowY(max(value, paintingVariables.startDomainValue), valueRange))
    //
    //    //Calculate the "bounds" for the *visible* offset area
    //    @Window val maxY = min(
    //      paintingVariables.axisEnd, domain2windowY(
    //        min(
    //          if (index < paintingVariables.offsetTicks.size - 1) {
    //            //At most until the next tick
    //            //Calculate the tick location for the *next* tick
    //            val nextValue = paintingVariables.offsetTicks[index + 1]
    //            nextValue
    //          } else {
    //            paintingVariables.endDomainValue
    //          }, paintingVariables.endDomainValue
    //        ), valueRange
    //      )
    //    )
    //
    //    val adjustedValue = if (value.isPositiveOrZero()) {
    //      value
    //    } else {
    //      value + paintingVariables.offsetStep
    //    }
    //
    //    //Paint the background
    //    val offsetIndex = paintingVariables.offsetIndexForValue(adjustedValue)
    //    gc.fill(style.offsetAreaFills.valueAt(offsetIndex))
    //    gc.fillRectCoordinates(0.0, minY, style.offsetAreaSize, maxY)
    //
    //
    //    //Paint the label at the *right* side of the tick
    //    val formatted = if (value.isPositiveOrZero()) {
    //      "+ ${adjustedValue.format(0)}"
    //    } else {
    //      "- ${adjustedValue.abs().format(0)}"
    //    }
    //
    //    //We have enough space for the label, paint at center
    //    @Window val textCenter = minY + (maxY - minY) / 2.0
    //    gc.font(style.offsetTickFont)
    //    gc.fill(style.offsetTickLabelColor)
    //    gc.saved {
    //      gc.rotateDegrees(-90.0)
    //      val snappedY = paintingContext.snapConfiguration.snapYValue(textCenter)
    //      gc.fillText(formatted, -snappedY, style.offsetAreaSize / 2.0, Direction.Center, maxWidth = (maxY - minY).abs() - 2.0, stringShortener = style.valueLabelStringShortener)
    //    }
    //  }
    //
    //  //Now paint the ticks - *over* the previously painted areas
    //  paintingVariables.offsetTicks.fastForEach { value: Double ->
    //    //The tick location of the *current* tick
    //    @Window val tickLocationY = domain2windowY(value, valueRange)
    //    @Window val lineLength = when (style.tickOrientation) {
    //      Vicinity.Outside -> {
    //        calculateTickValueLabelWidth()
    //      }
    //
    //      Vicinity.Inside -> {
    //        style.offsetAreaSize
    //      }
    //    }
    //
    //    if (tickLocationY <= paintingVariables.axisEnd) {
    //      gc.lineWidth = style.tickLineWidth
    //      gc.stroke(style.lineColor())
    //      gc.strokeLine(
    //        0.0,
    //        tickLocationY,
    //        lineLength,
    //        tickLocationY,
    //      )
    //    }
    //  }
    //} else {
    //  //We do not have any tick visible
    //
    //  // Paint the last tick in the middle of the window
    //  val value = paintingVariables.offsetTicks.last()
    //  val formatted = (if (paintingVariables.startDomainValue.isPositiveOrZero()) "+ " else "- ") + value.abs().format(0)
    //
    //  //Paint the background
    //  val colorIndex = paintingVariables.offsetIndexForValue(value)
    //  gc.fill(style.offsetAreaFills.valueAt(colorIndex))
    //  gc.fillRectCoordinates(0.0, paintingVariables.axisStart, style.offsetAreaSize, paintingVariables.axisEnd)
    //
    //  gc.font(style.offsetTickFont)
    //  gc.fill(style.offsetTickLabelColor)
    //  gc.saved {
    //    gc.rotateDegrees(-90.0)
    //    val y = gc.height / 2.0
    //    gc.fillText(formatted, -y, style.offsetAreaSize / 2.0, Direction.Center)
    //  }
    //}
  }

  override fun paintTicksWithLabelsVertically(paintingContext: LayerPaintingContext, direction: Direction) {
    TODO("Not yet implemented")
  }

  override fun paintTicksWithLabelsHorizontally(paintingContext: LayerPaintingContext, direction: Direction) {
    TODO("Not yet implemented")
  }

  open class Data(
    /**
     * Provides the value range for the axis
     */
    var valueRangeProvider: ValueRangeProvider = { ValueRange.default },
  )

  open class Style : ValueAxisLayer.Style() {
    /**
     * The size of the offset area
     */
    var offsetAreaSize: @Positive Double = 30.0

    /**
     * The fills that are used for the offset areas.
     */
    var offsetAreaFills: MultiProvider<OffsetIndex, Color> = MultiProvider.Companion.modulo(Color.web("#DBE1E5"), Color.web("#F3F5F7"))

    /**
     * The colors of the offset area ticks
     */
    var offsetTickLabelColor: Color = Theme.axisTickColor()

    /**
     * The font for the offset ticks
     */
    var offsetTickFont: FontDescriptorFragment = Theme.offsetTickFont()

    /**
     * The maximum amount of digits each value tick is allowed to have before an offset is added
     *  For example, when set to 3, a value tick of 23.741 would be shortened to 741 with an offset of +23.000
     *  This also works for decimal numbers
     */
    var spaceForDigits: @Positive Int = 6
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
