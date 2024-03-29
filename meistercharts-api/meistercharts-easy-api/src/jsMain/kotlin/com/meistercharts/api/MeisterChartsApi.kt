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
package com.meistercharts.api

import com.meistercharts.annotations.ContentArea
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.timerSupport
import com.meistercharts.js.MeisterchartJS
import it.neckar.geometry.Size
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.kotlin.lang.isCloseTo
import it.neckar.open.unit.number.MayBeZero
import org.w3c.dom.CustomEvent
import org.w3c.dom.CustomEventInit
import org.w3c.dom.HTMLDivElement
import kotlin.time.Duration.Companion.milliseconds

/**
 * The main API for MeisterCharts.
 * Call dispose when the component is no longer used.
 */
@JsExport
abstract class MeisterChartsApi<Configuration>
internal constructor(
  /**
   * The meister charts object. Can be used to call markAsDirty and dispose
   */
  internal val meisterCharts: MeisterchartJS,
) {

  /**
   * The window for dispatching events of a certain type.
   * Only one event per event-type is dispatched for each window.
   */
  private val eventDispatchWindow = 250.milliseconds

  init {
    meisterCharts.chartSupport.rootChartState.contentAreaSizeProperty.consume {
      scheduleContentAreaSizeChangedNotification(it)
    }
  }

  private fun scheduleContentAreaSizeChangedNotification(size: @ContentArea @MayBeZero Size) {
    meisterCharts.chartSupport.timerSupport.throttleLast(eventDispatchWindow, this) {
      notifyContentAreaSizeChanged(size)
    }
  }

  private var previousContentAreaSize: Size = Size.invalid

  private fun notifyContentAreaSizeChanged(size: @ContentArea @MayBeZero Size) {
    //Only if the content-area size has changed by a certain amount a custom-event will be dispatched.
    if (size.width.isCloseTo(previousContentAreaSize.width) && size.height.isCloseTo(previousContentAreaSize.height)) {
      return
    }
    previousContentAreaSize = size
    dispatchCustomEvent("content-area-size-changed", size.toJs())
  }

  /**
   * Dispatches a custom-event of type [eventType] and with detail [eventDetail]
   */
  protected fun dispatchCustomEvent(eventType: String, eventDetail: Any) {
    val customEvent = CustomEvent(
      type = "meistercharts:$eventType",
      eventInitDict = CustomEventInit(detail = eventDetail, bubbles = true, cancelable = false, composed = true)
    )

    loggerCustomEvents.debug({ "Dispatching CustomEvent: $eventType" }, customEvent)

    meisterCharts.htmlCanvas.canvasElement.dispatchEvent(customEvent)
  }

  /**
   * Disposes the canvas.
   * Stops all timers.
   */
  @JsName("dispose")
  fun dispose() {
    meisterCharts.dispose()
  }

  /**
   * Sets the data that is displayed
   */
  @JsName("setConfiguration")
  abstract fun setConfiguration(jsConfiguration: Configuration)

  /**
   * Should be called after each update of data/configuration/style
   */
  protected fun markAsDirty() {
    meisterCharts.chartSupport.markAsDirty(DirtyReason.Unknown)
  }

  /**
   * Returns the HTML div element that contains the MeisterCharts canvas
   */
  val holder: HTMLDivElement
    get() {
      return meisterCharts.holder
    }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.api.MeisterChartsApi")
    private val loggerCustomEvents: Logger = LoggerFactory.getLogger("com.meistercharts.api.MeisterChartsApi.CustomEvents")
  }
}
