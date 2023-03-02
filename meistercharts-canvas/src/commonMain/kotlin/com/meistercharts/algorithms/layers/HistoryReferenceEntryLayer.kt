package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.BoxLayoutCalculator
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.stripe.refentry.RectangleReferenceEntryStripePainter
import com.meistercharts.algorithms.painter.stripe.refentry.ReferenceEntryStripePainter
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.saved
import com.meistercharts.provider.TimeRangeProvider
import it.neckar.open.collections.fastForEach
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDataSeriesIndexProvider
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.fastForEachIndexed
import com.meistercharts.history.impl.timestampEnd
import com.meistercharts.history.impl.timestampStart

/**
 * Paints the history reference entries
 */
class HistoryReferenceEntryLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  init {
    this.configuration.also(additionalConfiguration)
  }

  override val type: LayerType = LayerType.Content

  override fun paintingVariables(): HistoryReferenceEntryPaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : HistoryReferenceEntryPaintingVariables {
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
        numberOfBoxes = configuration.visibleIndices.size(),
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
      @Window val y0 = chartCalculator.contentAreaRelative2windowY(0.0)
      @Window val y1 = chartCalculator.contentAreaRelative2windowY(1.0)

      val x0 = chartCalculator.contentViewportMinX()
      val x1 = chartCalculator.contentViewportMaxX()

      gc.fill(background)
      gc.fillRectCoordinates(x0, y0, x1, y1)
    }

    //Translate to viewport start
    gc.translate(0.0, chartCalculator.contentViewportMinY()) //Translate to top of viewport

    val visibleTimeRange = paintingVariables.visibleTimeRange
    @ms val minGapDistance = paintingVariables.minGapDistance

    val stripesLayout = paintingVariables.stripesLayout

    //translate to content area origin
    gc.translate(0.0, chartCalculator.contentAreaRelative2windowY(0.0))

    val historyConfiguration = configuration.historyConfiguration()

    //Iterate over all visible reference entry data series
    configuration.visibleIndices.fastForEachIndexed { index, visibleDataSeriesIndex ->
      val stripePainter = configuration.stripePainter.valueAt(visibleDataSeriesIndex.value)

      val boxIndex = BoxIndex(index)
      @Zoomed val startY = stripesLayout.calculateStart(boxIndex)
      @Zoomed val endY = stripesLayout.calculateEnd(boxIndex)

      gc.saved {
        //translate to the correct y location
        gc.translate(0.0, startY)

        //The time of the last data point. Is used to identify gaps
        @ms var lastTime = Double.NaN //initialize with NaN to ensure first one is no gap

        stripePainter.begin(paintingContext, stripesLayout.boxSize, visibleDataSeriesIndex, historyConfiguration)

        paintingVariables.historyBuckets.fastForEach { bucket ->
          val chunk = bucket.chunk
          if (chunk.isEmpty()) {
            return@fastForEach
          }

          if (gc.debug[DebugFeature.ShowBounds]) {
            gc.stroke(Color.blue2)
            @Window val chunkStartX = chartCalculator.time2windowX(chunk.firstTimeStamp())
            @Window val chunkEndX = chartCalculator.time2windowX(chunk.lastTimeStamp())
            gc.strokeLine(chunkStartX, -9.0, chunkStartX, 9.0) //already translated
            gc.strokeLine(chunkStartX, -9.0, chunkEndX, -9.0)
            gc.strokeLine(chunkEndX, -9.0, chunkEndX, 9.0) //already translated
          }

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
            @MayBeNoValueOrPending val referenceEntryId = chunk.getReferenceEntryId(visibleDataSeriesIndex, timestampIndex)
            @MayBeNoValueOrPending val count = chunk.getReferenceEntryIdsCount(visibleDataSeriesIndex, timestampIndex)
            val referenceEntryData = chunk.getReferenceEntryData(visibleDataSeriesIndex, referenceEntryId)

            if (startTime > visibleTimeRange.end) {
              //Skip all data points that are no longer visible on this tile
              stripePainter.valueChange(paintingContext, startX, endX, referenceEntryId, count, referenceEntryData)
              break
            }

            //Check if there is a gap because the distance between data points is larger than the gap size
            @ms val distanceToLastDataPoint = startTime - lastTime
            if (distanceToLastDataPoint > minGapDistance) {
              //We have a gap -> finish the current line and begin a new one
              stripePainter.finish(paintingContext)
              stripePainter.begin(paintingContext, stripesLayout.boxSize, visibleDataSeriesIndex, historyConfiguration)
            }


            //update the last time stuff
            lastTime = startTime

            stripePainter.valueChange(paintingContext, startX, endX, referenceEntryId, count, referenceEntryData)

            if (gc.debug[DebugFeature.ShowBounds]) {
              gc.stroke(Color.blue)
              gc.strokeLine(startX, -5.0, startX, 5.0) //already translated
            }
          }
        }
        stripePainter.finish(paintingContext)
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
     * The visible indices - must only include indices that *do* exist in the history configuration.
     * The [com.meistercharts.charts.refs.DiscreteTimelineChartGestalt] handles these cases.
     */
    var visibleIndices: ReferenceEntryDataSeriesIndexProvider,

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
    var layoutDirection: LayoutDirection = LayoutDirection.TopToBottom

    /**
     * The painters that are used to paint the entries.
     */
    var stripePainter: MultiProvider<ReferenceEntryDataSeriesIndex, ReferenceEntryStripePainter> = MultiProvider.always(RectangleReferenceEntryStripePainter())

    /**
     * The distance between two stripes
     */
    var stripesDistance: @px Double = 14.0

    /**
     * The background color
     */
    var background: Color? = null

    /**
     * Calculates the height for the given number of values
     */
    fun calculateTotalHeight(stripesCount: Int): @px Double {
      if (stripesCount == 0) {
        return 0.0
      }

      return stripeHeight * stripesCount + stripesDistance * (stripesCount - 1)
    }
  }

  interface HistoryReferenceEntryPaintingVariables : PaintingVariables {
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

}
