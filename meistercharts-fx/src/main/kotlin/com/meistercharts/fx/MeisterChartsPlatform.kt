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
package com.meistercharts.fx

import com.meistercharts.Meistercharts
import com.meistercharts.algorithms.painter.ArcPathWorkaroundEpsilon
import com.meistercharts.canvas.Meisterchart
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.canvas.PlatformStateListener
import com.meistercharts.design.Theme
import com.meistercharts.fx.font.FontAwesomeLoader
import com.meistercharts.fx.font.FontMetricsCacheFX
import com.meistercharts.platform.MeisterChartsAbstractPlatform
import com.meistercharts.platform.jvm.MeisterchartsJvm
import it.neckar.commons.logback.trace
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.annotations.AnyThread
import it.neckar.open.annotations.Blocking
import it.neckar.open.annotations.UiThread
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.javafx.JavaFxTimer
import it.neckar.open.time.nanos2millis
import it.neckar.open.unit.other.Relative
import it.neckar.open.unit.si.ns
import javafx.animation.AnimationTimer
import javafx.application.Platform
import java.util.concurrent.TimeUnit

/**
 * Global configuration / settings object for [com.meistercharts.canvas.Meisterchart].
 *
 * Is referenced from [MeisterchartBuilder] and ensures that initial code is executed once
 */
object MeisterChartsPlatform : MeisterChartsAbstractPlatform() {
  /**
   * Initializes the [MeisterChartsPlatform] in the UI thread.
   *
   * This method can be called form every thread. It will return when the platform has been initialized
   */
  @Blocking
  @AnyThread
  @JvmStatic
  fun initInUiThread(theme: Theme? = null) {
    JavaFxTimer.runAndWait(Runnable { init(theme) })
  }

  /**
   * Initializes the global configuration. Can be called multiple times but is only executed once
   */
  @UiThread
  @JvmStatic
  fun init(
    theme: Theme? = null,
    defaultI18nConfiguration: I18nConfiguration? = null,
  ) {
    require(Platform.isFxApplicationThread()) {
      "Init must only be called in the JavaFX thread"
    }

    initBasics(theme, defaultI18nConfiguration)
  }

  override fun initializeOnce() {
    super.initializeOnce()

    MeisterchartsJvm.localResourcePaintableFactory = LocalResourcePaintableProviderFX()

    MeisterchartsJvm.jvmImageLoader = ImageLoaderFX()

    //Apply the epsilon for JavaFX platform
    ArcPathWorkaroundEpsilon = 0.0001

    //Overwrite the device pixel ratio - if set as system property
    System.getProperty("devicePixelRatio")?.toDoubleOrNull()?.let { devicePixelRatioOverride ->
      logger.warn("devicePixelRatio is forced to $devicePixelRatioOverride")
      Meistercharts.mainScreenDevicePixelRatio = devicePixelRatioOverride
    }

    Meistercharts.fontMetricsCache = FontMetricsCacheFX
    Meistercharts.meisterchartFactory = MeisterchartFactoryFX()

    FontAwesomeLoader.loadSafe()

    armRenderLoop()
  }

  /**
   * Invokes the render loop
   */
  private val animationTimer: AnimationTimer = object : AnimationTimer() {
    private var lastRelativeNanos: @ns @Relative Long = 0

    override fun handle(relativeNowInNanos: @ns @Relative Long) {
      logger.trace {
        "Render loop - since last render: ${TimeUnit.NANOSECONDS.toMillis(relativeNowInNanos - lastRelativeNanos)} ms"
      }
      lastRelativeNanos = relativeNowInNanos

      Meistercharts.renderLoop.nextLoop(relativeNowInNanos.nanos2millis())
    }
  }

  /**
   * The render-loop subscription registered via [armRenderLoop]. Kept so its lifetime is tied to this singleton.
   */
  @Suppress("unused")
  private var renderLoopSubscription: it.neckar.open.dispose.Disposable? = null

  /**
   * Arms the render loop - will start the animation timer when the first chart is created and stop it when the last chart is disposed
   */
  private fun armRenderLoop() {
    require(Meistercharts.platformState.hasInstances.not()) { "Already contains instances!" }

    renderLoopSubscription = Meistercharts.platformState.onPlatformStateUpdate(object : PlatformStateListener {
      override fun firstInstanceCreated(meisterChart: Meisterchart) {
        animationTimer.start()
      }

      override fun lastInstanceDisposed() {
        animationTimer.stop()
      }
    })
  }

  private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.fx.MeisterChartsPlatform")
}
