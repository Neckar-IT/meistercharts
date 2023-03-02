package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.algorithms.layout.BoxLayoutCalculator
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.stripe.enums.EnumStripePainter
import com.meistercharts.algorithms.painter.stripe.enums.RectangleEnumStripePainter
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.saved
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndexProvider
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.fastForEachIndexed
import com.meistercharts.history.impl.timestampEnd
import com.meistercharts.history.impl.timestampStart
import com.meistercharts.history.valueAt
import com.meistercharts.provider.TimeRangeProvider
import it.neckar.open.collections.fastForEach
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 * Paints the history enumerations
 */
class HistoryEnumLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  init {
    this.configuration.also(additionalConfiguration)
  }

  override val type: LayerType = LayerType.Content

  override fun paintingVariables(): HistoryEnumPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : HistoryEnumPaintingVariables {
    override var historyBuckets: List<HistoryBucket> = emptyList()
    var minGapDistance: @ms Double = Double.NaN

    /**
     * The time range that is currently visible
     */
    override var visibleTimeRange: TimeRange = TimeRange.oneMinuteSinceReference
    var currentSamplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis

    /**
     * The vertical layout for the stripes
     */
    override var stripesLayout: EquisizedBoxLayout = EquisizedBoxLayout.empty

    override fun calculate(paintingContext: LayerPaintingContext) {
      val chartCalculator = paintingContext.chartCalculator
      val chartSupport = paintingContext.chartSupport


      currentSamplingPeriod = PaintingPropertyKey.SamplingPeriod.retrieve(chartSupport)
      visibleTimeRange = PaintingPropertyKey.VisibleTimeRangeX.retrieve(chartSupport)
      minGapDistance = PaintingPropertyKey.MinGapDistance.retrieve(chartSupport)

      historyBuckets = configuration.historyStorage.query(visibleTimeRange, currentSamplingPeriod)

      //Y axis
      @Zoomed val availableSpace = chartCalculator.contentAreaRelative2zoomedY(1.0)

      stripesLayout = BoxLayoutCalculator.layout(
        availableSpace = availableSpace,
        numberOfBoxes = configuration.visibleIndices.size().coerceAtMost(configuration.historyConfiguration().enumDataSeriesCount),
        layoutDirection = configuration.layoutDirection,
        minBoxSize = configuration.stripeHeight,
        maxBoxSize = configuration.stripeHeight,
        gapSize = configuration.stripesDistance,
      )
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartSupport.timeChartCalculator(configuration.contentAreaTimeRange())

    //Fill the background
    configuration.background?.let { background ->
      val y0 = chartCalculator.contentAreaRelative2windowY(0.0)
      val y1 = chartCalculator.contentAreaRelative2windowY(1.0)

      gc.fill(background)
      gc.fillRectCoordinates(0.0, y0, gc.width, y1)
    }

    if (gc.debug[DebugFeature.ShowBounds]) {
      val y0 = chartCalculator.contentAreaRelative2windowY(0.0)
      val y1 = chartCalculator.contentAreaRelative2windowY(1.0)

      gc.fill(Color.lightsalmon)
      gc.fill(Color.red)

      gc.fillRectCoordinates(0.0, y0, gc.width, y1)
    }

    val visibleTimeRange = paintingVariables.visibleTimeRange
    val minGapDistance = paintingVariables.minGapDistance

    val stripesLayout = paintingVariables.stripesLayout

    //translate to content area origin
    gc.translate(0.0, chartCalculator.contentAreaRelative2windowY(0.0))

    //Iterate over all visible enum data series
    configuration.visibleIndices.fastForEachIndexed(maxSize = configuration.historyConfiguration().enumDataSeriesCount) { i, visibleDataSeriesIndex ->
      val enumStripePainter = configuration.enumStripePainter.valueAt(visibleDataSeriesIndex)

      val boxIndex = BoxIndex(i)
      @Zoomed val startY = stripesLayout.calculateStart(boxIndex)
      @Zoomed val endY = stripesLayout.calculateEnd(boxIndex)

      if (gc.debug[DebugFeature.ShowBounds]) {
        //Show areas
        gc.fill(Color.lightblue)
        gc.fillRect(0.0, startY, gc.width, endY - startY)
      }

      gc.saved {
        //translate to the correct y location
        gc.translate(0.0, startY)

        //The time of the last data point. Is used to identify gaps
        @ms var lastTime = Double.NaN //initialize with NaN to ensure first one is no gap

        val historyConfiguration = configuration.historyConfiguration()
        enumStripePainter.begin(paintingContext, stripesLayout.boxSize, visibleDataSeriesIndex, historyConfiguration)

        paintingVariables.historyBuckets.fastForEach { bucket ->
          val chunk = bucket.chunk

          for (timestampIndexAsInt in 0 until chunk.timeStampsCount) {
            val timestampIndex = TimestampIndex(timestampIndexAsInt)
            //The start time is the time when the value "starts" (has been measured)
            @ms val startTime = chunk.timestampStart(timestampIndex, bucket.samplingPeriod)
            //The end time is the time of the *next* measurement
            @ms val endTime = chunk.timestampEnd(timestampIndex, bucket.samplingPeriod)

            if (endTime < visibleTimeRange.start) {
              //Skip all data points that are not visible on this tile yet
              continue
            }

            @Window val startX = chartCalculator.time2windowX(startTime)
            @Window val endX = chartCalculator.time2windowX(endTime)
            @MayBeNoValueOrPending val historyEnumSet = chunk.getEnumValue(visibleDataSeriesIndex, timestampIndex)
            @MayBeNoValueOrPending val enumOrdinalMostTime = chunk.getEnumOrdinalMostTime(visibleDataSeriesIndex, timestampIndex)

            if (startTime > visibleTimeRange.end) {
              //Skip all data points that are no longer visible on this tile
              enumStripePainter.valueChange(paintingContext, startX, endX, historyEnumSet, enumOrdinalMostTime, Unit)
              break
            }

            //Check if there is a gap because the distance between data points is larger than the gap size
            @ms val distanceToLastDataPoint = startTime - lastTime
            if (distanceToLastDataPoint > minGapDistance) {
              //We have a gap -> finish the current line and begin a new one
              enumStripePainter.finish(paintingContext)
              enumStripePainter.begin(paintingContext, stripesLayout.boxSize, visibleDataSeriesIndex, historyConfiguration)
            }


            //update the last time stuff
            lastTime = startTime

            enumStripePainter.valueChange(paintingContext, startX, endX, historyEnumSet, enumOrdinalMostTime, Unit)
          }
        }
        enumStripePainter.finish(paintingContext)
      }
    }
  }

  class Configuration(
    /**
     * Where the history is stored
     */
    val historyStorage: HistoryStorage,

    val historyConfiguration: () -> HistoryConfiguration,

    /**
     * The visible indices
     *
     * ATTENTION: This might contain indices that do *not* exist.
     * Therefore, it is necessary to check whether the data series for the given index does exist
     */
    var visibleIndices: EnumDataSeriesIndexProvider,

    /**
     * Provides the time range of the content area
     */
    val contentAreaTimeRange: @ContentArea TimeRangeProvider,
  ) {

    /**
     * The height of a stripe
     */
    var stripeHeight: @px Double = 22.0

    /**
     * The layout direction
     */
    var layoutDirection: LayoutDirection = LayoutDirection.BottomToTop

    /**
     * The enum bar painters that are used to paint the enums.
     */
    var enumStripePainter: MultiProvider<EnumDataSeriesIndex, EnumStripePainter> = MultiProvider.always(RectangleEnumStripePainter())

    /**
     * The distance between two enum stripes
     */
    var stripesDistance: @px Double = 14.0

    /**
     * The background color
     */
    var background: Color? = null

    /**
     * Calculates the height for the given number of enum values
     */
    fun calculateTotalHeight(stripesCount: Int): @px Double {
      if (stripesCount == 0) {
        return 0.0
      }

      return stripeHeight * stripesCount + stripesDistance * (stripesCount - 1)
    }
  }
}

interface HistoryEnumPaintingVariables : PaintingVariables {
  /**
   * The history bucket that have been used to display the strips
   */
  var historyBuckets: List<HistoryBucket>

  /**
   * The time range that is currently visible
   */
  var visibleTimeRange: TimeRange

  /**
   * The vertical layout for the stripes
   */
  var stripesLayout: EquisizedBoxLayout

}
