package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.layout.BoxIndex
import com.meistercharts.algorithms.layout.BoxLayoutCalculator
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.algorithms.layout.LayoutDirection
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.stripe.StripePainter
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.saved
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.DataSeriesIndexProvider
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.atMost
import com.meistercharts.history.fastForEachIndexed
import com.meistercharts.history.find
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.requireIsFinite
import com.meistercharts.history.impl.timestampEnd
import com.meistercharts.history.impl.timestampStart
import com.meistercharts.provider.TimeRangeProvider
import it.neckar.open.collections.fastForEach
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 * Base class for layers that paint stripes using the history
 */
abstract class AbstractHistoryStripeLayer<
  DataSeriesIndexProviderType : DataSeriesIndexProvider<DataSeriesIndexType>,
  DataSeriesIndexType : DataSeriesIndex,
  StripePainterType : StripePainter<DataSeriesIndexType, Value1Type, Value2Type, Value3Type, Value4Type>,
  Value1Type, Value2Type, Value3Type, Value4Type,
  >(
  /**
   * The configuration
   */
  val configuration: Configuration<DataSeriesIndexProviderType, DataSeriesIndexType, StripePainterType, Value1Type, Value2Type, Value3Type, Value4Type>,
) : AbstractLayer() {

  override val type: LayerType = LayerType.Content

  abstract override fun paintingVariables(): HistoryStripeLayerPaintingVariables<Value1Type, Value2Type, Value3Type, Value4Type>

  /**
   * Abstract base class for painting variables
   */
  abstract inner class AbstractHistoryStripeLayerPaintingVariables : HistoryStripeLayerPaintingVariables<Value1Type, Value2Type, Value3Type, Value4Type> {
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

    var historyConfiguration: HistoryConfiguration = HistoryConfiguration.empty

    override val activeInformation: HistoryStripeLayerPaintingVariables.ActiveInformation<Value1Type, Value2Type, Value3Type, Value4Type> = object : HistoryStripeLayerPaintingVariables.ActiveInformation<Value1Type, Value2Type, Value3Type, Value4Type> {
      override var value1: Value1Type = value1Default()
      override var value2: Value2Type = value2Default()
      override var value3: Value3Type = value3Default()
      override var value4: Value4Type = value4Default()

      /**
       * The geometrical center for the [Configuration.activeDataSeriesIndex] and [Configuration.activeTimeStamp].
       */
      override var geometricalCenter: @Window @MayBeNaN Double = Double.NaN
        private set(value) {
          field = value
        }

      override var geometricalCenterIfFinite: @Window @MayBeNaN Double
        @Deprecated("Not supported", level = DeprecationLevel.HIDDEN) get() {
          throw UnsupportedOperationException("Only set supported")
        }
        /**
         * Sets the value of [geometricalCenter] if the provided value is finite.
         */
        set(value) {
          if (value.isFinite()) {
            geometricalCenter = value
          }
        }

      override fun reset() {
        value1 = value1Default()
        value2 = value2Default()
        value3 = value3Default()
        value4 = value4Default()

        geometricalCenter = Double.NaN
      }
    }

    override fun calculate(paintingContext: LayerPaintingContext) {
      val chartSupport = paintingContext.chartSupport
      val chartCalculator = paintingContext.chartSupport.timeChartCalculator(configuration.contentAreaTimeRange())

      historyConfiguration = configuration.historyConfiguration()

      currentSamplingPeriod = PaintingPropertyKey.SamplingPeriod.retrieve(chartSupport)
      visibleTimeRange = PaintingPropertyKey.VisibleTimeRangeX.retrieve(chartSupport)
      minGapDistance = PaintingPropertyKey.MinGapDistance.retrieve(chartSupport)

      historyBuckets = configuration.historyStorage.query(visibleTimeRange, currentSamplingPeriod)

      //Y axis
      @Zoomed val availableSpace = chartCalculator.contentAreaRelative2zoomedY(1.0)

      stripesLayout = BoxLayoutCalculator.layout(
        availableSpace = availableSpace,
        numberOfBoxes = visibleIndices.size(),
        layoutDirection = configuration.layoutDirection,
        minBoxSize = configuration.stripeHeight,
        maxBoxSize = configuration.stripeHeight,
        gapSize = configuration.stripesDistance,
      )


      //Calculate the "active" value
      run {
        activeInformation.reset()
        configuration.activeDataSeriesIndex?.let { activeDataSeriesIndex ->
          @ms val activeTimeStamp = configuration.activeTimeStamp.requireIsFinite { "activeTimeStamp" }

          historyBuckets.find(activeTimeStamp) { bucket: HistoryBucket, timestampIndex: TimestampIndex ->
            activeInformation.value1 = bucket.chunk.getValue1(activeDataSeriesIndex, timestampIndex)
            activeInformation.value2 = bucket.chunk.getValue2(activeDataSeriesIndex, timestampIndex)
            activeInformation.value3 = bucket.chunk.getValue3(activeDataSeriesIndex, timestampIndex)
            activeInformation.value4 = bucket.chunk.getValue4(activeDataSeriesIndex, timestampIndex)
          }
        }
      }

      //Calculate the layout for the stripes
      visibleIndices.fastForEachIndexed { visibleIndexAsInt, visibleDataSeriesIndex ->
        val stripePainter: StripePainterType = configuration.stripePainters.valueAt(visibleDataSeriesIndex.value)

        val dataSeriesIndex = ReferenceEntryDataSeriesIndex(visibleIndexAsInt)
        val isActiveDataSeries = configuration.activeDataSeriesIndex == dataSeriesIndex

        //Is only set if the current series is the active series
        @MayBeNaN @ms val relevantActiveTimeStamp = if (isActiveDataSeries) configuration.activeTimeStamp else Double.NaN

        //The time of the last data point. Is used to identify gaps
        @ms var lastTime = Double.NaN //initialize with NaN to ensure first one is no gap

        stripePainter.layoutBegin(paintingContext, stripesLayout.boxSize, visibleDataSeriesIndex, historyConfiguration)

        historyBuckets.fastForEach { bucket ->
          val chunk = bucket.chunk
          if (chunk.isEmpty()) {
            return@fastForEach
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

            val value1 = chunk.getValue1(visibleDataSeriesIndex, timestampIndex)
            val value2 = chunk.getValue2(visibleDataSeriesIndex, timestampIndex)
            val value3 = chunk.getValue3(visibleDataSeriesIndex, timestampIndex)
            val value4 = chunk.getValue4(visibleDataSeriesIndex, timestampIndex)


            if (startTime > visibleTimeRange.end) {
              //Skip all data points that are no longer visible on this tile
              activeInformation.geometricalCenterIfFinite = stripePainter.layoutValueChange(
                paintingContext = paintingContext, startX = startX, endX = endX, startTime = startTime, endTime = endTime, activeTimeStamp = relevantActiveTimeStamp, newValue1 = value1, newValue2 = value2, newValue3 = value3, newValue4 = value4
              )
              break
            }

            //Check if there is a gap because the distance between data points is larger than the gap size
            @ms val distanceToLastDataPoint = startTime - lastTime
            if (distanceToLastDataPoint > minGapDistance) {
              //We have a gap -> finish the current line and begin a new one
              activeInformation.geometricalCenterIfFinite = stripePainter.layoutFinish(paintingContext)
              stripePainter.layoutBegin(paintingContext, stripesLayout.boxSize, visibleDataSeriesIndex, historyConfiguration)
            }


            //update the last time stuff
            lastTime = startTime

            activeInformation.geometricalCenterIfFinite = stripePainter.layoutValueChange(
              paintingContext = paintingContext, startX = startX, endX = endX, startTime = startTime, endTime = endTime, activeTimeStamp = relevantActiveTimeStamp, newValue1 = value1, newValue2 = value2, newValue3 = value3, newValue4 = value4
            )
          }
        }

        activeInformation.geometricalCenterIfFinite = stripePainter.layoutFinish(paintingContext)
      }
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

    val paintingVariables = paintingVariables()
    val stripesLayout = paintingVariables.stripesLayout

    //translate to content area origin
    gc.translate(0.0, chartCalculator.contentAreaRelative2windowY(0.0))

    //Iterate over all visible reference entry data series
    visibleIndices.fastForEachIndexed { visibleIndexAsInt, visibleDataSeriesIndex ->
      val stripePainter = configuration.stripePainters.valueAt(visibleDataSeriesIndex.value)

      val boxIndex = BoxIndex(visibleIndexAsInt)
      @Zoomed val startY = stripesLayout.calculateStart(boxIndex)

      gc.saved {
        //translate to the correct y location
        gc.translate(0.0, startY)

        gc.saved {
          stripePainter.paint(paintingContext)
        }
      }
    }

  }


  protected abstract fun value1Default(): Value1Type
  protected abstract fun value2Default(): Value2Type
  protected abstract fun value3Default(): Value3Type
  protected abstract fun value4Default(): Value4Type

  /**
   * Returns the number of data series
   */
  protected abstract fun dataSeriesCount(): Int

  /**
   * Extracts the value 1
   */
  abstract fun HistoryChunk.getValue1(visibleDataSeriesIndex: DataSeriesIndexType, timestampIndex: TimestampIndex): Value1Type
  abstract fun HistoryChunk.getValue2(visibleDataSeriesIndex: DataSeriesIndexType, timestampIndex: TimestampIndex): Value2Type
  abstract fun HistoryChunk.getValue3(visibleDataSeriesIndex: DataSeriesIndexType, timestampIndex: TimestampIndex): Value3Type
  abstract fun HistoryChunk.getValue4(visibleDataSeriesIndex: DataSeriesIndexType, timestampIndex: TimestampIndex): Value4Type


  /**
   * Contains the visible indices
   */
  val visibleIndices: DataSeriesIndexProvider<DataSeriesIndexType> = configuration::requestedVisibleIndices.atMost { dataSeriesCount() }

  abstract class Configuration<
    SeriesIndexProviderType : DataSeriesIndexProvider<DataSeriesIndexType>,
    DataSeriesIndexType : DataSeriesIndex,
    StripePainterType : StripePainter<DataSeriesIndexType, Value1Type, Value2Type, Value3Type, Value4Type>,
    Value1Type, Value2Type, Value3Type, Value4Type,
    >(
    /**
     * Where the history is stored
     */
    val historyStorage: HistoryStorage,

    /**
     * Provides the current history configuration
     */
    val historyConfiguration: () -> HistoryConfiguration,

    /**
     * The visible indices
     *
     * ATTENTION: This might contain indices that do *not* exist.
     * Therefore, it is necessary to check whether the data series for the given index does exist
     */
    var requestedVisibleIndices: SeriesIndexProviderType,

    /**
     * Provides the time range of the content area
     */
    val contentAreaTimeRange: @ContentArea TimeRangeProvider,

    /**
     * The enum bar painters that are used to paint the enums.
     */
    var stripePainters: MultiProvider<DataSeriesIndexType, StripePainterType>,
  ) {

    /**
     * The height of a stripe
     */
    var stripeHeight: @px Double = 22.0

    /**
     * The distance between two enum stripes
     */
    var stripesDistance: @px Double = 14.0

    /**
     * The layout direction
     */
    var layoutDirection: LayoutDirection = LayoutDirection.TopToBottom

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

    /**
     * The index of the data series that is highlighted (mouse over)
     */
    var activeDataSeriesIndex: DataSeriesIndexType? = null

    /**
     * The active time stamp (usually where the mouse is located).
     * Is set to [Double.NaN] if no values is set
     */
    var activeTimeStamp: @ms @MayBeNaN Double = Double.NaN

    /**
     * Provides the size for the background of the active data series.
     *
     * The result is used to paint the background for the active series.
     * If the returned size is small(er) than the data series size, the background is also smaller
     */
    var activeDataSeriesBackgroundSize: (dataSeriesHeight: @Zoomed Double) -> Double = { dataSeriesHeight -> dataSeriesHeight }
  }

  interface HistoryStripeLayerPaintingVariables<Value1Type, Value2Type, Value3Type, Value4Type> : PaintingVariables {
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


    /**
     * Contains the information about the active (usually related mouse over / tooltip) data
     */
    val activeInformation: ActiveInformation<Value1Type, Value2Type, Value3Type, Value4Type>


    /**
     * Used for tooltips
     */
    interface ActiveInformation<Value1Type, Value2Type, Value3Type, Value4Type> {
      /**
       * Resets all values
       */
      fun reset()

      /**
       * The geometrical center of the active segment
       */
      val geometricalCenter: @Window @MayBeNaN Double

      /**
       * Can be used to set the value - if it is not [Double.NaN]
       */
      var geometricalCenterIfFinite: @Window @MayBeNaN Double
        @Deprecated("Not supported", level = DeprecationLevel.HIDDEN) get

      var value1: Value1Type
      var value2: Value2Type
      var value3: Value3Type
      var value4: Value4Type
    }
  }
}
