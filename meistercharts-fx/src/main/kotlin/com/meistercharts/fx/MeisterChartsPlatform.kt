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
package com.meistercharts.fx

import it.neckar.open.annotations.AnyThread
import it.neckar.open.annotations.Blocking
import it.neckar.open.annotations.UiThread
import com.meistercharts.algorithms.mainScreenDevicePixelRatio
import com.meistercharts.algorithms.painter.ArcPathWorkaroundEpsilon
import com.meistercharts.canvas.FontMetricsCacheAccess
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.MeisterChartsFactoryAccess
import com.meistercharts.canvas.jvmImageLoader
import com.meistercharts.design.CorporateDesign
import com.meistercharts.design.initCorporateDesign
import com.meistercharts.fx.font.FontMetricsCacheFX
import it.neckar.open.javafx.JavaFxTimer
import com.meistercharts.resources.jvmLocalResourcePaintableFactory
import it.neckar.open.time.jvmTimerSupport
import javafx.application.Platform

/**
 * Global configuration / settings object for [com.meistercharts.canvas.MeisterChart].
 *
 * Is referenced from [MeisterChartBuilder] and ensures that initial code is executed once
 */
object MeisterChartsPlatform {
  /**
   * Initializes the [MeisterChartsPlatform] in the UI thread.
   *
   * This method can be called form every thread. It will return when the platform has been initialized
   */
  @Blocking
  @AnyThread
  @JvmStatic
  fun initInUiThread(corporateDesign: CorporateDesign? = null) {
    JavaFxTimer.runAndWait(Runnable { init(corporateDesign) })
  }

  /**
   * Initializes the global configuration. Can be called multiple times but is only executed once
   */
  @UiThread
  @JvmStatic
  fun init(corporateDesign: CorporateDesign? = null) {
    require(Platform.isFxApplicationThread()) {
      "Init must only be called in the JavaFX thread"
    }

    if (initialized) {
      return
    }

    //Register the local resource paintable factor
    jvmLocalResourcePaintableFactory = LocalResourcePaintableProviderFX()

    //Register the image loader
    jvmImageLoader = ImageLoaderFX()

    //Register the timer support
    jvmTimerSupport = TimerSupportFX()

    corporateDesign?.let {
      initCorporateDesign(it)
    }

    //Apply the epsilon for JavaFX platform
    ArcPathWorkaroundEpsilon = 0.0001

    //Overwrite the device pixel ratio - if set as system property
    System.getProperty("devicePixelRatio")?.toDoubleOrNull()?.let { devicePixelRatioOverride ->
      println("mainScreenDevicePixelRatio is forced to $devicePixelRatioOverride")
      mainScreenDevicePixelRatio = devicePixelRatioOverride
    }

    FontMetricsCacheAccess.fontMetricsCache = FontMetricsCacheFX

    MeisterChartsFactoryAccess.factory = MeisterChartsFactoryFX()
    initialized = true
  }

  /**
   * Is set to true if already initialized
   */
  private var initialized = false
}
