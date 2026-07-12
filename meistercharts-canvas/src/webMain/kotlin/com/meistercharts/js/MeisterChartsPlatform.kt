/*
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
import com.meistercharts.canvas.Meisterchart
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.canvas.PlatformStateListener
import com.meistercharts.canvas.UrlConversion
import com.meistercharts.canvas.UrlConverter
import com.meistercharts.design.Theme
import com.meistercharts.events.FontLoadedEventBroker
import com.meistercharts.platform.MeisterChartsAbstractPlatform
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.logging.ifDebug
import it.neckar.open.i18n.I18nConfiguration
import kotlinx.browser.window

/**
 * Global configuration / settings object for [com.meistercharts.canvas.Meisterchart].
 *
 * Is referenced from [MeisterchartBuilder] and ensures that initial code is executed once
 */
object MeisterChartsPlatform : MeisterChartsAbstractPlatform() {
  init {
    val fontLoadingSupported = listenForFontLoadingDone { loadedFontsProvider ->
      logger.ifDebug {
        val loadedFonts = loadedFontsProvider()
        logger.debug("${loadedFonts.size} fonts loaded:")
        loadedFonts.forEach {
          logger.debug(" - $it")
        }
      }
      FontLoadedEventBroker.notifyLoaded()
    }
    if (fontLoadingSupported.not()) {
      logger.warn("WARNING: document.fonts is not supported by this browser -> fonts loaded from now on may not be rendered correctly")
    }
  }

  /**
   * Initializes the global configuration. Can be called multiple times
   */
  fun init(
    /**
     * The (optional) theme that will be applied as *default* theme for all charts
     */
    theme: Theme? = null,
    /**
     * The (optional) default i18n configuration that will be applied as *default* i18n configuration for all charts
     */
    defaultI18nConfiguration: I18nConfiguration? = null,
    /**
     * The (optional) url converter
     */
    urlConverter: UrlConverter? = null,
  ) {
    initBasics(theme, defaultI18nConfiguration)

    urlConverter?.let {
      UrlConversion.activate(it)
    }
  }

  override fun initializeOnce() {
    super.initializeOnce()

    Meistercharts.fontMetricsCache = FontMetricsCacheJS
    Meistercharts.meisterchartFactory = MeisterchartFactoryJS()

    armRenderLoop()
  }

  /**
   * The render-loop subscription registered via [armRenderLoop]. Kept so its lifetime is tied to this singleton.
   */
  @Suppress("unused")
  private var renderLoopSubscription: it.neckar.open.dispose.Disposable? = null

  /**
   * Start the animation frame when the first chart is created and stop it when the last chart is disposed
   */
  private fun armRenderLoop() {
    require(Meistercharts.platformState.hasInstances.not()) { "Already contains instances!" }

    renderLoopSubscription = Meistercharts.platformState.onPlatformStateUpdate(object : PlatformStateListener {
      /**
       * ID for the current animation frame request - used to cancel the request
       */
      var currentRequestId: Int = 0

      override fun firstInstanceCreated(meisterChart: Meisterchart) {
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

private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.js.MeisterChartsPlatform")
