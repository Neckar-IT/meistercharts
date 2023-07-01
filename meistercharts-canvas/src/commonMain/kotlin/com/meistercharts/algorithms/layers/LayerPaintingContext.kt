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

import com.meistercharts.calc.ChartCalculator
import com.meistercharts.calc.TileChartCalculator
import com.meistercharts.calc.TimeChartCalculator
import com.meistercharts.time.TimeRange
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.tile.TileIndex
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DebugConfiguration
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.DirtyReasonBitSet
import com.meistercharts.canvas.LayerSupport
import com.meistercharts.canvas.PaintingLoopIndex
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.animation.Tween
import com.meistercharts.canvas.debug
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.i18nSupport
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.canvas.textService
import com.meistercharts.charts.ChartId
import com.meistercharts.model.Size
import com.meistercharts.state.ChartState
import it.neckar.open.http.Url
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.resolve
import it.neckar.open.time.TimeZone
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Context for the painting of layers
 */
data class LayerPaintingContext(
  /**
   * The graphics context that is used to paint the layer
   */
  val gc: CanvasRenderingContext,
  /**
   * The canvas support. Can be used to access the state
   */
  val layerSupport: LayerSupport,
  /**
   * The time of the current frame.
   *
   * This timestamp can also be accessed using the val [currentFrameTimestamp] while painting.
   * Therefore, it is not always necessary to pass the frame timestamp as parameter
   */
  val frameTimestamp: @ms Double,
  /**
   * The time delta to the last frame. (0 on the first paint)
   */
  val frameTimestampDelta: @ms Double,

  /**
   * The painting loop index (will overflow after about 414 days).
   */
  val loopIndex: PaintingLoopIndex,
  /**
   * The dirty reasons
   */
  val dirtyReasons: DirtyReasonBitSet,
) {
  val chartSupport: ChartSupport
    get() = layerSupport.chartSupport

  /**
   * Returns the chart ID
   */
  val chartId: ChartId
    get() {
      return chartSupport.chartId
    }

  /**
   * Returns the width of the canvas
   */
  @px
  val width: Double
    get() = gc.width

  /**
   * Returns the height of the canvas
   */
  @px
  val height: Double
    get() = gc.height

  /**
   * Returns the time zone from the layer support that is used when displaying time stamps
   */
  val timeZone: TimeZone
    get() = chartSupport.i18nSupport.timeZone

  val i18nConfiguration: I18nConfiguration
    get() = chartSupport.i18nSupport.configuration

  /**
   * Returns the snap configuration form the layer support
   */
  val snapConfiguration: SnapConfiguration
    get() = chartSupport.pixelSnapSupport.snapConfiguration

  /**
   * Returns the chart calculator
   */
  val chartCalculator: ChartCalculator
    get() = chartSupport.chartCalculator

  /**
   * Returns the current chart state
   */
  val chartState: ChartState
    get() = chartSupport.currentChartState

  /**
   * Returns the debug configuration
   */
  val debug: DebugConfiguration
    get() = chartSupport.debug

  /**
   * Executes the provided code, if the debug feature is enabled
   */
  inline fun ifDebug(debugFeature: DebugFeature, debugAction: () -> Unit) {
    contract {
      callsInPlace(debugAction, InvocationKind.AT_MOST_ONCE)
    }

    if (debug[debugFeature]) {
      debugAction()
    }
  }

  /**
   * Contains the missing resources for the given frame
   */
  val missingResources: MissingResources = MissingResources()
}

/**
 * Resolves this [TextKey] within the given [paintingContext]
 */
inline fun TextKey.resolve(paintingContext: LayerPaintingContext): String {
  return this.resolve(paintingContext.chartSupport)
}

/**
 * Resolves this [TextKey] within the given [chartSupport]
 */
inline fun TextKey.resolve(chartSupport: ChartSupport): String {
  return this.resolve(chartSupport.textService, chartSupport.i18nConfiguration)
}

/**
 * Runs the given action with an updated current chart state from the given provider
 */
fun LayerPaintingContext.withCurrentChartState(chartStateProvider: ChartState.() -> ChartState, action: () -> Unit) {
  withCurrentChartState(chartStateProvider(chartState), action)
}

/**
 * Runs the given action with an updated current chart state
 */
fun LayerPaintingContext.withCurrentChartState(chartState: ChartState, action: () -> Unit) {
  this.chartSupport.withCurrentChartState(chartState, action)
}

/**
 * Returns the tile calculator
 */
fun LayerPaintingContext.tileCalculator(
  tileIndex: TileIndex,
  tileSize: Size,
): TileChartCalculator {
  return chartSupport.tileCalculator(tileIndex, tileSize)
}

fun ChartSupport.tileCalculator(
  tileIndex: TileIndex,
  tileSize: Size,
): TileChartCalculator {
  return currentChartState.tileCalculator(tileIndex, tileSize)
}

/**
 * Returns the tile calculator for this tile index
 */
fun ChartState.tileCalculator(
  tileIndex: TileIndex,
  tileSize: Size,
): TileChartCalculator {
  //TODO think about cache
  return TileChartCalculator(this, tileIndex, tileSize)
}

fun ChartSupport.timeChartCalculator(
  contentAreaTimeRangeX: TimeRange,
): TimeChartCalculator {
  return currentChartState.timeChartCalculator(contentAreaTimeRangeX)
}

/**
 * Creates a new time chart calculator, that is based on the provided content area time ranges
 */
fun ChartState.timeChartCalculator(
  contentAreaTimeRangeX: TimeRange,
): TimeChartCalculator {
  //TODO think about cache
  return TimeChartCalculator(this, contentAreaTimeRangeX)
}

/**
 * Returns the elapsed ratio for the given painting context
 */
fun Tween.interpolate(paintingContext: LayerPaintingContext): @pct Double {
  return this.interpolate(paintingContext.frameTimestamp)
}

/**
 * Collects missing resources during the paint
 */
class MissingResources() {
  /**
   * Contains the missing URLs
   */
  val missingURLs: Set<Url> = mutableSetOf()

  fun reportMissing(urlPaintable: UrlPaintable) {
    reportMissing(urlPaintable.url)
  }

  fun reportMissing(url: Url) {
    (missingURLs as MutableSet).add(url)
  }

  fun isEmpty(): Boolean {
    return missingURLs.isEmpty()
  }
}
