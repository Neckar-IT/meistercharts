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

import com.meistercharts.canvas.FontMetricsCacheAccess
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.MeisterChartsFactoryAccess
import com.meistercharts.canvas.UrlConversion
import com.meistercharts.canvas.UrlConverter
import com.meistercharts.design.CorporateDesign
import com.meistercharts.events.FontLoadedEventBroker
import com.meistercharts.js.external.FontFaceSet
import com.meistercharts.js.external.listenForLoadingDone
import com.meistercharts.platform.MeisterChartsAbstractPlatform
import it.neckar.logging.LoggerFactory
import it.neckar.open.i18n.I18nConfiguration
import kotlinx.browser.document
import org.w3c.dom.get

/**
 * Global configuration / settings object for [com.meistercharts.canvas.MeisterChart].
 *
 * Is referenced from [MeisterChartBuilder] and ensures that initial code is executed once
 */
object MeisterChartsPlatform : MeisterChartsAbstractPlatform() {

  init {
    (document["fonts"]?.unsafeCast<FontFaceSet>())?.listenForLoadingDone {
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
  }
}

private val logger = LoggerFactory.getLogger("com.meistercharts.js.MeisterChartsPlatform")
