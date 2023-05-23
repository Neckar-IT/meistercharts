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
package com.meistercharts.api.line

import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.HudElementIndex
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.debug.FramesPerSecondLayer
import com.meistercharts.algorithms.layers.debug.PaintPerformanceLayer
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.algorithms.painter.stripe.enums.RectangleEnumStripePainter
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.api.MeisterChartsApiLegacy
import com.meistercharts.api.StripeStyle
import com.meistercharts.api.TimeRange
import com.meistercharts.api.Zoom
import com.meistercharts.api.applyCrossWireStyle
import com.meistercharts.api.applyEnumAxisStyle
import com.meistercharts.api.applyThresholdStyles
import com.meistercharts.api.applyTimeAxisStyle
import com.meistercharts.api.applyTitleStyle
import com.meistercharts.api.applyValueAxisStyle
import com.meistercharts.api.toBoxStyles
import com.meistercharts.api.toColor
import com.meistercharts.api.toColors
import com.meistercharts.api.toFontDescriptorFragment
import com.meistercharts.api.toJs
import com.meistercharts.api.toModel
import com.meistercharts.api.toNumberFormat
import com.meistercharts.canvas.RoundingStrategy
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.timerSupport
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.charts.timeline.TimeLineChartWithToolbarGestalt
import com.meistercharts.charts.timeline.setUpDemo
import com.meistercharts.design.Theme
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexInt
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndexProvider
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.fastForEach
import com.meistercharts.js.MeisterChartJS
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import it.neckar.commons.kotlin.js.debug
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.ifDebug
import it.neckar.open.charting.api.sanitizing.sanitize
import it.neckar.open.collections.Cache
import it.neckar.open.collections.cache
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.provider.DoublesProvider1
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider2
import it.neckar.open.unit.si.s
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

/**
 * A time related line chart
 */
@JsExport
class TimeLineChart internal constructor(
  internal val gestalt: TimeLineChartWithToolbarGestalt,
  meisterCharts: MeisterChartJS,
) : MeisterChartsApiLegacy<TimeLineChartData, TimeLineChartStyle>(meisterCharts) {

  init {
    gestalt.timeLineChartGestalt.applySickDefaults()
  }

  /**
   * The window for the time range changed events.
   * Only one event is published for each window
   */
  private val visibleTimeRangeChangedEventWindow = 250.milliseconds

  /**
   * The last visible time range. Is used to compare if a notification bout the change is necessary
   */
  private var previousVisibleTimeRange: com.meistercharts.algorithms.TimeRange = com.meistercharts.algorithms.TimeRange(0.0, 0.0)

  /**
   * Notifies the observers about a time range change
   */
  private fun notifyVisibleTimeRangeChanged() {
    val currentVisibleTimeRange = meisterCharts.chartSupport.chartCalculator.visibleTimeRangeXinWindow(gestalt.timeLineChartGestalt.style.contentAreaTimeRange)
    if (currentVisibleTimeRange == previousVisibleTimeRange) {
      return
    }
    //We dispatch a CustomEvent of type "VisibleTimeRangeChanged" every time the translation along the x-axis changes
    previousVisibleTimeRange = currentVisibleTimeRange
    dispatchCustomEvent("VisibleTimeRangeChanged", currentVisibleTimeRange.toJs())
  }

  /**
   * The history store cache that is used to add the values
   */
  private val historyStorageCache = HistoryStorageCache(gestalt.timeLineChartGestalt.inMemoryStorage)

  init {
    //decrease number of repaints
    meisterCharts.chartSupport.translateOverTime.roundingStrategy = RoundingStrategy.round

    //Set the preferred refresh rate
    meisterCharts.chartSupport.targetRefreshRate = TargetRefreshRate.veryFast60

    meisterCharts.chartSupport.rootChartState.windowTranslationProperty.consume {
      scheduleTimeRangeChangedNotification()
    }
    meisterCharts.chartSupport.rootChartState.windowSizeProperty.consume {
      scheduleTimeRangeChangedNotification()
    }
    meisterCharts.chartSupport.rootChartState.contentAreaSizeProperty.consume {
      scheduleTimeRangeChangedNotification()
    }
    meisterCharts.chartSupport.rootChartState.zoomProperty.consume {
      scheduleTimeRangeChangedNotification()
    }
    gestalt.timeLineChartGestalt.style.contentAreaTimeRangeProperty.consume {
      scheduleTimeRangeChangedNotification()
    }

    //If the history configuration changes we need to clear the history because
    //managing different configurations in a single history is not supported yet.
    gestalt.timeLineChartGestalt.data.historyConfigurationProperty.consume {
      clearHistory()
    }

    //Add refresh listener as debug
    meisterCharts.layerSupport.layers.addLayer(PaintPerformanceLayer().visibleIf {
      meisterCharts.layerSupport.recordPaintStatistics
    })
    meisterCharts.layerSupport.layers.addLayer(FramesPerSecondLayer().visibleIf {
      meisterCharts.layerSupport.recordPaintStatistics
    })

    //Apply the defaults for all axis styles
    gestalt.timeLineChartGestalt.style.valueAxisStyleConfiguration = { style: ValueAxisLayer.Style, dataSeriesIndex: DecimalDataSeriesIndex ->
      style.applyTimeLineChartStyle()
    }
  }

  /**
   * Schedules a notification about a changed time range change
   */
  private fun scheduleTimeRangeChangedNotification() {
    meisterCharts.chartSupport.timerSupport.throttleLast(visibleTimeRangeChangedEventWindow, this) {
      notifyVisibleTimeRangeChanged()
    }
  }

  override fun setData(jsData: TimeLineChartData) {
    jsData.historySettings?.let {
      val expectedSamplingPeriod = it.expectedSamplingPeriod.toModel()
      gestalt.timeLineChartGestalt.data.minimumSamplingPeriod = expectedSamplingPeriod

      @s val guaranteedHistoryLength = it.guaranteedHistoryLength
      gestalt.timeLineChartGestalt.inMemoryStorage.let { storage ->
        val naturalHistoryBucketRange = expectedSamplingPeriod.toHistoryBucketRange()
        storage.naturalSamplingPeriod = naturalHistoryBucketRange.samplingPeriod
        storage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(guaranteedHistoryLength * 1000.0, naturalHistoryBucketRange)
      }

      //configure the gap calculator
      it.minGapSizeFactor?.let { factor ->
        gestalt.timeLineChartGestalt.data.historyGapCalculator = DefaultHistoryGapCalculator(factor)
      }
    }

    jsData.play?.let {
      meisterCharts.chartSupport.translateOverTime.animated = it
    }

    markAsDirty()
  }

  override fun setStyle(jsStyle: TimeLineChartStyle) {
    gestalt.timeLineChartGestalt.applyStyle(jsStyle)

    jsStyle.showToolbar?.let {
      gestalt.style.showToolbar = it
    }

    jsStyle.showMouseWheelModifierHint?.let {
      gestalt.style.showMouseWheelModifierHint = it
    }

    jsStyle.visibleTimeRange?.toModel()?.let {
      @DomainRelative val startDateRelative = gestalt.timeLineChartGestalt.style.contentAreaTimeRange.time2relative(it.start)
      @DomainRelative val endDateRelative = gestalt.timeLineChartGestalt.style.contentAreaTimeRange.time2relative(it.end)
      meisterCharts.chartSupport.zoomAndTranslationSupport.fitX(startDateRelative, endDateRelative)
    }

    jsStyle.crossWirePosition?.let {
      gestalt.timeLineChartGestalt.style.crossWirePositionX = it
    }

    gestalt.timeLineChartGestalt.crossWireLayerDecimalValues.style.applyCrossWireStyle(jsStyle.crossWireStyle)
    gestalt.timeLineChartGestalt.crossWireLayerEnumValues.style.applyCrossWireStyle(jsStyle.crossWireStyle)

    markAsDirty()
  }

  /**
   * Get the currently visible time range (in UTC)
   */
  @Suppress("unused")
  @JsName("getVisibleTimeRange")
  fun getVisibleTimeRange(): TimeRange {
    return with(meisterCharts.chartSupport) {
      chartCalculator.visibleTimeRangeXinWindow(gestalt.timeLineChartGestalt.style.contentAreaTimeRange).toJs()
    }
  }

  /**
   * Prepare the history to store the given data series
   */
  @Suppress("unused")
  @JsName("setDataSeries")
  fun setDataSeries(
    jsDecimalDataSeries: Array<DecimalDataSeries>,
    jsEnumDataSeries: Array<EnumDataSeries>,
  ) {
    gestalt.timeLineChartGestalt.data.historyConfiguration = TimeLineChartConverter.toHistoryConfiguration(jsDecimalDataSeries, jsEnumDataSeries)
  }

  /**
   * Adds a sample to the history stored by this chart
   */
  @Suppress("unused")
  @JsName("addSample")
  fun addSample(jsSample: Sample) {
    val historyConfiguration = gestalt.timeLineChartGestalt.data.historyConfiguration
    TimeLineChartConverter.toHistoryChunk(jsSample, historyConfiguration)?.let {
      gestalt.timeLineChartGestalt.inMemoryStorage.let { storage ->
        historyStorageCache.scheduleForStore(it, storage.naturalSamplingPeriod)
      }
    }
  }

  /**
   * Adds samples to the history stored by this chart
   */
  @Suppress("unused")
  @JsName("addSamples")
  fun addSamples(jsSamples: Array<Sample>) {
    if (jsSamples.isEmpty()) {
      return
    }
    val historyConfiguration = gestalt.timeLineChartGestalt.data.historyConfiguration
    TimeLineChartConverter.toHistoryChunk(jsSamples, historyConfiguration)?.let {
      gestalt.timeLineChartGestalt.inMemoryStorage.let { storage ->
        historyStorageCache.scheduleForStore(it, storage.naturalSamplingPeriod)
      }
    }
  }

  /**
   * Removes all samples added to the history
   */
  @Suppress("unused")
  fun clearHistory() {
    historyStorageCache.clear()
    gestalt.timeLineChartGestalt.inMemoryStorage.clear()
  }

  /**
   * Reset zoom and translation
   */
  @Suppress("unused")
  @JsName("resetView")
  fun resetView() {
    meisterCharts.chartSupport.zoomAndTranslationSupport.resetToDefaults()
  }

  /**
   * Returns the current zoom
   */
  @Suppress("unused")
  @JsName("getZoom")
  fun getZoom(): Zoom {
    meisterCharts.chartSupport.zoomAndTranslationSupport.chartState.zoom.let {
      return object : Zoom {
        override val scaleX: Double = it.scaleX
        override val scaleY: Double = it.scaleY
      }
    }
  }

  /**
   * Modifies the current Zoom.
   * @param zoom the new Zoom
   * @param zoomCenterX: a value in [0..1] that is the x-coordinate of the center of the zoom with 0 being the left edge of the window and 1 being the right edge of the window.
   * @param zoomCenterY: a value in [0..1] that is the y-coordinate of the center of the zoom with 0 being the top edge of the window and 1 being the bottom edge of the window.
   */
  @Suppress("unused")
  @JsName("modifyZoom")
  fun modifyZoom(zoom: Zoom?, zoomCenterX: @WindowRelative Double?, zoomCenterY: @WindowRelative Double?) {
    requireNotNull(zoom) { "no zoom provided" }
    zoom.toModel().let {
      with(meisterCharts.chartSupport) {
        @Window val centerX = chartCalculator.windowRelative2WindowX(zoomCenterX ?: 0.5)
        @Window val centerY = chartCalculator.windowRelative2WindowY(zoomCenterY ?: 0.5)
        zoomAndTranslationSupport.setZoom(it.scaleX, it.scaleY, Coordinates(centerX, centerY))
      }
    }
  }

  /**
   * Set up with nice data for a demo.
   */
  @Suppress("unused")
  fun setUpDemo() {
    gestalt.timeLineChartGestalt.setUpDemo().also {
      gestalt.timeLineChartGestalt.onDispose(it)
    }
  }

  companion object {
    internal val logger: Logger = LoggerFactory.getLogger("com.meistercharts.api.line.TimeLineChart")
  }
}

/**
 * Applies the default style for the timeline chart style
 */
private fun ValueAxisLayer.Style.applyTimeLineChartStyle() {
  side = Side.Left
  tickOrientation = Vicinity.Outside
  paintRange = AxisStyle.PaintRange.Continuous
}

private fun TimeLineChartGestalt.applyStyle(jsStyle: TimeLineChartStyle) {
  TimeLineChart.logger.ifDebug {
    console.debug("TimeLineChartGestalt.applyStyle", jsStyle)
  }

  jsStyle.crossWireFont?.toFontDescriptorFragment()?.let {
    crossWireLayerDecimalValues.style.applyCrossWireFont(it)
    crossWireLayerEnumValues.style.applyCrossWireFont(it)
  }

  jsStyle.crossWireDecimalsFormat?.let {
    this.style.crossWireDecimalFormat = TimeLineChartConverter.toCrossWireFormat(it)
  }

  jsStyle.crossWireDecimalsLabelTextColor?.toColor()?.let {
    this.style.crossWireDecimalsLabelTextColors = MultiProvider.always(it)
  }

  jsStyle.crossWireDecimalsLabelBoxStyles?.let { jsBoxStyles ->
    this.style.crossWireDecimalsLabelBoxStyles = toBoxStyles(jsBoxStyles)
    this.style.crossWireDecimalsLabelTextColors = toColors(jsBoxStyles)
  }

  jsStyle.crossWireEnumsLabelBoxStyles?.let { jsBoxStyles ->
    this.style.crossWireEnumsLabelBoxStyles = toBoxStyles(jsBoxStyles)
    this.style.crossWireEnumsLabelTextColors = toColors(jsBoxStyles)
  }

  jsStyle.visibleLines?.let { jsVisibleLines ->
    if (jsVisibleLines.size == 1 && jsVisibleLines[0] == -1) { //check for magic "-1" value
      this.style.showAllDecimalSeries()
    } else {
      val map = jsVisibleLines.map { DecimalDataSeriesIndex(it) }
      this.style.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.forList(map.toList())
    }
  }

  jsStyle.visibleValueAxes?.let { jsVisibleValueAxes ->
    if (jsVisibleValueAxes.size == 1 && jsVisibleValueAxes[0] == -1) {
      //Special handling: "-1" results in all value axis visible
      val styleConfigurationsSize = jsStyle.decimalDataSeriesStyles?.size ?: 0
      this.style.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { max(styleConfigurationsSize, this.data.historyConfiguration.decimalDataSeriesCount) }
    } else {
      this.style.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.forList(jsVisibleValueAxes.toList().map { DecimalDataSeriesIndex(it) })
    }
  }

  jsStyle.visibleEnumStripes?.let { jsVisibleEnumStripes ->
    if (jsVisibleEnumStripes.size == 1 && jsVisibleEnumStripes[0] == -1) { //check for magic "-1" value
      this.style.showAllEnumSeries()
    } else {
      val map = jsVisibleEnumStripes.map { EnumDataSeriesIndex(it) }
      this.style.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.forList(map.toList())
    }
  }

  jsStyle.valueAxesBackground?.toColor()?.let {
    this.style.valueAxesBackground = it
  }

  jsStyle.valueAxesGap?.let {
    this.multiValueAxisLayer.configuration.valueAxesGap = it
  }

  jsStyle.valueAxesStyle?.let { jsValueAxisStyle ->
    jsStyle.decimalDataSeriesStyles?.fastForEachIndexed { index: @DecimalDataSeriesIndexInt Int, jsDecimalDataSeriesStyle ->
      //The value axis layer for this decimal data series
      val valueAxisLayer = this.getValueAxisLayer(DecimalDataSeriesIndex(index))
      val topTitleLayer = this.getValueAxisTopTitleLayer(DecimalDataSeriesIndex(index))

      //Call this method *before* applying the (more specific) properties from the jsDecimalDataSeriesStyle
      valueAxisLayer.style.applyValueAxisStyle(jsValueAxisStyle)
      topTitleLayer.configuration.applyTitleStyle(jsValueAxisStyle)

      jsDecimalDataSeriesStyle.valueAxisTitle?.let { jsTitle ->
        valueAxisLayer.style.setTitle(jsTitle)
      }

      //Overwrites the default ticks format that might have been applied by applyValueAxisStyle
      jsDecimalDataSeriesStyle.ticksFormat?.toNumberFormat()?.let {
        valueAxisLayer.style.ticksFormat = it
      }
    }
  }

  jsStyle.enumAxisStyle?.let { jsEnumAxisStyle ->
    enumCategoryAxisLayer.style.applyEnumAxisStyle(jsEnumAxisStyle)
  }

  jsStyle.timeAxisStyle?.let { jsTimeAxisStyle ->
    this.timeAxisLayer.style.applyTimeAxisStyle(jsTimeAxisStyle)

    //Apply the size of the axis at the gestalt, too.
    //This is necessary to update clipping etc.
    jsTimeAxisStyle.axisSize?.let {
      this.style.timeAxisSize = it
    }
  }

  jsStyle.decimalDataSeriesStyles?.let { jsDecimalDataSeriesStyles: Array<DecimalDataSeriesStyle> ->
    this.style.lineValueRanges = TimeLineChartConverter.toValueRangeProvider(jsDecimalDataSeriesStyles)

    this.data.thresholdValueProvider = object : DoublesProvider1<DecimalDataSeriesIndex> {
      override fun size(param1: DecimalDataSeriesIndex): @HudElementIndex Int {
        val jsDataSeriesStyle: DecimalDataSeriesStyle = jsDecimalDataSeriesStyles.getOrNull(param1.value) ?: return 0
        return jsDataSeriesStyle.thresholds?.size ?: 0
      }

      override fun valueAt(index: @HudElementIndex Int, param1: DecimalDataSeriesIndex): Double {
        val threshold = jsDecimalDataSeriesStyles.getOrNull(param1.value)
          ?.thresholds
          ?.getOrNull(index) ?: throw IllegalStateException("no threshold found for $index")
        return threshold.value
      }
    }

    this.data.thresholdLabelProvider = object : MultiProvider2<HudElementIndex, List<String>, DecimalDataSeriesIndex, LayerPaintingContext> {
      override fun valueAt(index: @HudElementIndex Int, param1: DecimalDataSeriesIndex, param2: LayerPaintingContext): List<String> {
        val jsDataSeriesStyle = jsDecimalDataSeriesStyles.getOrNull(param1.value)
        val threshold = jsDataSeriesStyle?.thresholds?.getOrNull(index)

        val label = threshold?.label ?: return listOf("Threshold $param1 / $index")

        return splitLinesCache.getOrStore(label) {
          label.split("\n")
        }
      }
    }

    //Apply the styles for each threshold on the layer directly
    jsDecimalDataSeriesStyles.fastForEachIndexed { index: @DecimalDataSeriesIndexInt Int, jsDecimalDataSeriesStyle ->
      val dataSeriesIndex = DecimalDataSeriesIndex(index)

      this.thresholdsSupport.applyThresholdStyles(jsDecimalDataSeriesStyle.thresholds, dataSeriesIndex)
    }
  }

  jsStyle.lineStyles?.let { jsLineStyles ->
    this.style.lineStyles = TimeLineChartConverter.toLineStyles(jsLineStyles)
  }

  jsStyle.enumDataSeriesStyles?.let { jsEnumDataSeriesStyles ->
    val enumBarPainters = jsEnumDataSeriesStyles.map { jsEnumDataSeriesStyle ->
      RectangleEnumStripePainter {

        jsEnumDataSeriesStyle.stripeStyles?.let { jsStripeStyles ->
          //TODO support other fill types, too

          val fillColors = jsStripeStyles.mapIndexed { index: Int, jsStripeStyle: StripeStyle? ->
            jsStripeStyle?.backgroundColor?.toColor() ?: Theme.enumColors().valueAt(index)
          }

          fillProvider = { value: HistoryEnumOrdinal, _: HistoryEnum ->
            fillColors.getOrNull(value.value) ?: Theme.enumColors().valueAt(value.value)
          }
        }

        jsEnumDataSeriesStyle.aggregationMode?.let {
          aggregationMode = it.sanitize()
        }
      }
    }

    historyEnumLayer.configuration.stripePainters = MultiProvider.forListOr(enumBarPainters, RectangleEnumStripePainter())
  }

  jsStyle.enumStripeHeight?.let {
    historyEnumLayer.configuration.stripeHeight = it
  }

  jsStyle.enumsStripesDistance?.let {
    historyEnumLayer.configuration.stripesDistance = it
  }

  jsStyle.enumsBackgroundColor?.toColor()?.let {
    historyEnumLayer.configuration.background = it
  }


  //calculate the viewport top margin based on the visible axis
  var max = 0.0

  style.actualVisibleValueAxesIndices.fastForEach { decimalSeriesIndex ->
    val topForThisKey = valueAxisSupport.calculateContentViewportMarginTop(decimalSeriesIndex, chartSupport())
    max = max.coerceAtLeast(topForThisKey)
  }

  contentViewportMargin = contentViewportMargin.withTop(max)

  //Currently not supported for timeline chart
  //Apply the content viewport margin
  //jsStyle.contentViewportMargin?.let {
  //  contentViewportMargin = contentViewportMargin.withValues(it)
  //}
}

private val TimeLineChartGestalt.inMemoryStorage: InMemoryHistoryStorage
  get() {
    return data.historyStorage as InMemoryHistoryStorage
  }


/**
 * Cache that can be used to optimize splitting of strings into lines
 */
private val splitLinesCache: Cache<String, List<String>> = cache("split lines", 100)
