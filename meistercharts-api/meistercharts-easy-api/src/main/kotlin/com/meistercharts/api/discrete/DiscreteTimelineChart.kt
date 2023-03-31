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

import com.meistercharts.algorithms.layers.debug.PaintPerformanceLayer
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
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
import com.meistercharts.js.MeisterChartJS
import com.meistercharts.model.Coordinates
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
  fun setHistory(data: DiscreteTimelineChartData) {
    //clear history
    //set new history configuration from data
    //set data
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

}

@JsExport
external interface DiscreteTimelineChartData {
  /**
   * Index corresponds to the data series index
   */
  val series: Array<DiscreteDataEntriesForDataSeries>
}

@JsExport
external interface DiscreteDataEntriesForDataSeries {
  /**
   * Contains all entries for this data series.
   * Must not overlap!
   */
  val entries: Array<@Sorted(by = "from") DiscreteDataEntry>
}

@JsExport
external interface DiscreteDataEntry {
  val start: @ms Double
  val end: @ms Double
  val label: String
  val status: Double //must be double since JS does not support Int
}

private val DiscreteTimelineChartGestalt.inMemoryStorage: InMemoryHistoryStorage
  get() {
    return configuration.historyStorage as InMemoryHistoryStorage
  }
