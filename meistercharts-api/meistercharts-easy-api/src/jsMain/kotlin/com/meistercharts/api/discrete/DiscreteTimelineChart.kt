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
package com.meistercharts.api.discrete

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.debug.PaintPerformanceLayer
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.api.MeisterChartsApi
import com.meistercharts.api.TimeRange
import com.meistercharts.api.Zoom
import com.meistercharts.api.toJs
import com.meistercharts.api.toModel
import com.meistercharts.canvas.RoundingStrategy
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.timerSupport
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.refs.DiscreteTimelineChartGestalt
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.js.MeisterChartJS
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Distance
import it.neckar.commons.kotlin.js.debug
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.logging.ifDebug
import it.neckar.open.formatting.formatUtc
import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.si.ms
import kotlin.time.Duration.Companion.milliseconds

/**
 * Timeline chart that visualizes discrete timelines.
 */
@JsExport
class DiscreteTimelineChart internal constructor(
  /**
   * The gestalt that is configured
   */
  internal val gestalt: DiscreteTimelineChartGestalt,

  /**
   * The meister charts object. Can be used to call markAsDirty and dispose
   */
  meisterChart: MeisterChartJS,
) : MeisterChartsApi<DiscreteTimelineChartConfiguration>(meisterChart) {

  /**
   * The history-storage cache that is used to add the values
   */
  private val historyStorageCache = HistoryStorageCache(gestalt.inMemoryStorage)

  init {
    gestalt.applySickDefaults()

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
    gestalt.configuration.contentAreaTimeRangeProperty.consume {
      scheduleTimeRangeChangedNotification()
    }

    //Add refresh listener as debug
    meisterCharts.layerSupport.layers.addLayer(PaintPerformanceLayer().visibleIf {
      meisterCharts.layerSupport.recordPaintStatistics
    })

  }

  override fun setConfiguration(jsConfiguration: DiscreteTimelineChartConfiguration) {
    gestalt.applyConfiguration(jsConfiguration)
    markAsDirty()
  }

  /**
   * Clears the current history and sets the history of this chart.
   */
  @Suppress("unused") //called from JS
  fun setHistory(data: DiscreteTimelineChartData) {
    logger.ifDebug {
      console.debug("setHistory", data)
    }

    val inMemoryHistoryStorage = gestalt.inMemoryHistoryStorage
    inMemoryHistoryStorage.clear() //clear history

    val historyConfiguration = gestalt.configuration.historyConfiguration()

    if (historyConfiguration.referenceEntryDataSeriesCount == 0) {
      logger.debug("Skip setting history since the history configuration is empty")
      return
    }

    val pair: Pair<HistoryChunk, SamplingPeriod> = data.toChunk(historyConfiguration) ?: return
    val chunk = pair.first
    val samplingPeriod = pair.second

    gestalt.configuration.minimumSamplingPeriod = samplingPeriod
    historyStorageCache.scheduleForStore(chunk, samplingPeriod)

    logger.debug { "Sampling period: $samplingPeriod" }
    logger.ifDebug { console.debug("Chunk", chunk.dump()) }

    //Set the visible time range
    gestalt.chartSupport().let { chartSupport ->
      val chartCalculator = chartSupport.chartCalculator
      val contentAreaTimeRangeX: com.meistercharts.algorithms.TimeRange = gestalt.configuration.contentAreaTimeRange

      @TimeRelative val startRelative = contentAreaTimeRangeX.time2relative(chunk.firstTimestamp)
      @TimeRelative val endRelative = contentAreaTimeRangeX.time2relative(chunk.lastTimestamp)

      logger.debug("start/end: ${chunk.firstTimestamp.formatUtc()} - ${chunk.lastTimestamp.formatUtc()}")
      logger.debug("relative start/end: $startRelative - $endRelative")

      //Set the values immediately + set the defaults (for resize operations)
      chartSupport.zoomAndTranslationSupport.fitX(startRelative, endRelative)
      chartSupport.zoomAndTranslationSupport.zoomAndTranslationDefaults = object : ZoomAndTranslationDefaults {
        override fun defaultZoom(chartCalculator: ChartCalculator): com.meistercharts.model.Zoom {
          val targetNumberOfSamples = chartCalculator.chartState.windowWidth / DiscreteTimelineChartGestalt.MinDistanceBetweenDataPoints //how many samples should be visible
          @ms val visibleDuration = samplingPeriod.distance * targetNumberOfSamples * 0.9 //ensure we are below
          @ms val visibleTimeRangeStart = chunk.lastTimestamp - visibleDuration
          @TimeRelative val visibleTimeRangeStartRelative = contentAreaTimeRangeX.time2relative(visibleTimeRangeStart)

          val zoomX = chartSupport.zoomAndTranslationSupport.calculateFitZoomX(visibleTimeRangeStartRelative, endRelative)
          return com.meistercharts.model.Zoom(zoomX, 1.0)
        }

        override fun defaultTranslation(chartCalculator: ChartCalculator): Distance {
          val targetNumberOfSamples = chartCalculator.chartState.windowWidth / DiscreteTimelineChartGestalt.MinDistanceBetweenDataPoints //how many samples should be visible
          @ms val visibleDuration = samplingPeriod.distance * targetNumberOfSamples * 0.9 //ensure we are below
          @ms val visibleTimeRangeStart = chunk.lastTimestamp - visibleDuration
          @TimeRelative val startToShowRelative = contentAreaTimeRangeX.time2relative(visibleTimeRangeStart)

          val translationX = chartSupport.zoomAndTranslationSupport.calculateFitWindowTranslationX(startToShowRelative)
          return Distance(translationX, gestalt.contentViewportMargin.top)
        }
      }

      logger.debug("zoom state: ${chartSupport.rootChartState.zoom}, ${chartSupport.rootChartState.windowTranslation}")

      chartSupport.zoomAndTranslationSupport.zoomAndTranslationModifier = object : ZoomAndTranslationModifier {
        override fun modifyTranslation(translation: @Zoomed Distance, calculator: ChartCalculator): @Zoomed Distance {
          @TimeRelative val startRelative = contentAreaTimeRangeX.time2relative(chunk.firstTimestamp)
          @TimeRelative val endRelative = contentAreaTimeRangeX.time2relative(chunk.lastTimestamp)

          @Zoomed val startZoomed = calculator.domainRelative2zoomedX(startRelative)
          @Zoomed val windowWidth = calculator.chartState.windowWidth
          @Zoomed val endZoomed = calculator.domainRelative2zoomedX(endRelative)

          val min = -endZoomed + windowWidth - 20.0
          val max = (-startZoomed + 20.0 + gestalt.categoryAxisLayer.style.size).coerceAtLeast(min)

          return translation.coerceXWithin(min, max)
        }

        override fun modifyZoom(zoom: com.meistercharts.model.Zoom, calculator: ChartCalculator): com.meistercharts.model.Zoom {
          return zoom
        }
      }

      //Reset to defaults to force application of the new zoom/translation
      chartSupport.zoomAndTranslationSupport.resetToDefaults()
    }
  }

  /**
   * Removes all samples added to the history
   */
  fun clearHistory() {
    historyStorageCache.clear()
    gestalt.inMemoryStorage.clear()
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
    val currentVisibleTimeRange = meisterCharts.chartSupport.chartCalculator.visibleTimeRangeXinWindow(gestalt.configuration.contentAreaTimeRange)
    if (currentVisibleTimeRange == previousVisibleTimeRange) {
      return
    }
    //We dispatch a CustomEvent of type "VisibleTimeRangeChanged" every time the translation along the x-axis changes
    previousVisibleTimeRange = currentVisibleTimeRange
    dispatchCustomEvent("VisibleTimeRangeChanged", currentVisibleTimeRange.toJs())
  }

  /**
   * Schedules a notification about a changed time range change
   */
  private fun scheduleTimeRangeChangedNotification() {
    meisterCharts.chartSupport.timerSupport.throttleLast(visibleTimeRangeChangedEventWindow, this) {
      notifyVisibleTimeRangeChanged()
    }
  }

  fun getVisibleTimeRange(): TimeRange {
    return with(meisterCharts.chartSupport) {
      chartCalculator.visibleTimeRangeXinWindow(gestalt.configuration.contentAreaTimeRange).toJs()
    }
  }

  /**
   * Reset zoom and translation
   */
  fun resetView() {
    meisterCharts.chartSupport.zoomAndTranslationSupport.resetToDefaults()
  }

  /**
   * Get the current zoom
   */
  fun getZoom(): Zoom {
    meisterCharts.chartSupport.zoomAndTranslationSupport.chartState.zoom.let {
      return object : Zoom {
        override val scaleX: Double = it.scaleX
        override val scaleY: Double = it.scaleY
      }
    }
  }

  /**
   * Modify the current zoom
   */
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

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.api.discrete.DiscreteTimelineChart")
  }
}

private val DiscreteTimelineChartGestalt.inMemoryHistoryStorage: InMemoryHistoryStorage
  get() = this.historyStorage as InMemoryHistoryStorage

/**
 * Contains the data for the discrete timeline chart
 */
@JsExport
actual external interface DiscreteTimelineChartData {
  /**
   * Index corresponds to the data series index.
   * Contains one entry for each data series.
   */
  actual val series: Array<DiscreteDataEntriesForDataSeries>

  /**
   * The default duration (end - start) of a discrete data entry.
   */
  actual val defaultEntryDuration: @ms Double
}

/**
 * Contains the entries for a single discrete data series
 */
@JsExport
actual external interface DiscreteDataEntriesForDataSeries {
  /**
   * Contains all entries for this data series.
   * Must not overlap!
   */
  actual val entries: Array<@Sorted(by = "from") DiscreteDataEntry>
}

@JsExport
actual external interface DiscreteDataEntry {
  actual val start: @ms Double
  actual val end: @ms Double

  /**
   * The (optional) label
   */
  actual val label: String?

  /**
   * The status for this entry.
   */
  actual val status: Double? //must be double since JS does not support Int
}

private val DiscreteTimelineChartGestalt.inMemoryStorage: InMemoryHistoryStorage
  get() {
    return configuration.historyStorage as InMemoryHistoryStorage
  }
