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
package com.meistercharts.js

import com.meistercharts.Meistercharts
import com.meistercharts.canvas.FontMetricsCacheAccess
import com.meistercharts.canvas.MeisterChart
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.MeisterChartsFactoryAccess
import com.meistercharts.canvas.PlatformStateListener
import com.meistercharts.canvas.UrlConversion
import com.meistercharts.canvas.UrlConverter
import com.meistercharts.design.CorporateDesign
import com.meistercharts.events.FontLoadedEventBroker
import com.meistercharts.js.external.FontFace
import com.meistercharts.js.external.FontFaceSet
import com.meistercharts.js.external.listenForLoadingDone
import com.meistercharts.platform.MeisterChartsAbstractPlatform
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.logging.ifDebug
import it.neckar.open.i18n.I18nConfiguration
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.get

/**
 * Global configuration / settings object for [com.meistercharts.canvas.MeisterChart].
 *
 * Is referenced from [MeisterChartBuilder] and ensures that initial code is executed once
 */
object MeisterChartsPlatform : MeisterChartsAbstractPlatform() {
  init {
    (document["fonts"]?.unsafeCast<FontFaceSet>())?.listenForLoadingDone {
      logger.ifDebug {
        logger.debug("${it.fontfaces.size} fonts loaded:")
        it.fontfaces.forEach {
          logger.debug(" - ${it.format()}")
        }
      }
      FontLoadedEventBroker.notifyLoaded()
    } ?: logger.warn("WARNING: document.fonts is not supported by this browser -> fonts loaded from now on may not be rendered correctly")
  }

  /**
   * Initializes the global configuration. Can be called multiple times
   */
  fun init(
    corporateDesign: CorporateDesign? = null,
    defaultI18nConfiguration: I18nConfiguration? = null,
    /**
     * The (optional) url converter
     */
    urlConverter: UrlConverter? = null,
  ) {
    initBasics(corporateDesign, defaultI18nConfiguration)

    urlConverter?.let {
      UrlConversion.activate(it)
    }
  }

  override fun initializeOnce() {
    FontMetricsCacheAccess.fontMetricsCache = FontMetricsCacheJS
    MeisterChartsFactoryAccess.factory = MeisterChartsFactoryJS()

    armRenderLoop()
  }

  /**
   * Start the animation frame when the first chart is created and stop it when the last chart is disposed
   */
  private fun armRenderLoop() {
    require(Meistercharts.platformState.hasInstances.not()) { "Already contains instances!" }

    Meistercharts.platformState.onPlatformStateUpdate(object : PlatformStateListener {
      /**
       * ID for the current animation frame request - used to cancel the request
       */
      var currentRequestId: Int = 0

      override fun firstInstanceCreated(meisterChart: MeisterChart) {
        requestNextFrame()
      }

      private fun requestNextFrame() {
        logger.trace("Requesting next frame")

        currentRequestId = window.requestAnimationFrame { relativeNowInMillis ->
          Meistercharts.renderLoop.nextLoop(relativeNowInMillis)

          if (Meistercharts.platformState.hasInstances) {
            //Request next frame - if there are instances left
            requestNextFrame()
          }
        }
      }

      override fun lastInstanceDisposed() {
        if (currentRequestId != 0) {
          logger.debug { "Canceling current frame request $currentRequestId" }
          window.cancelAnimationFrame(currentRequestId)
        }
      }
    })
  }
}

private fun FontFace.format(): String {
  return "$family $style, $variant $weight"
}

private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.js.MeisterChartsPlatform")
