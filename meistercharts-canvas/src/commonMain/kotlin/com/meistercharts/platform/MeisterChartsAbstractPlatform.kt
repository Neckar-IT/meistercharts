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
package com.meistercharts.platform

import com.meistercharts.design.CorporateDesign
import com.meistercharts.design.initCorporateDesign
import com.meistercharts.version.MeisterChartsVersion
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.info
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.updateDefaultI18nConfiguration

/**
 * Abstract base class for meister charts platform implementations
 */
abstract class MeisterChartsAbstractPlatform {
  /**
   * Is set to true if already initialized
   */
  var initialized: Boolean = false
    private set


  fun initBasics(corporateDesign: CorporateDesign?, defaultI18nConfiguration: I18nConfiguration?) {
    if (initialized.not()) {
      logger.info { "Initializing MeisterChartsPlatform ${MeisterChartsVersion.versionAsStringVerbose}" }
    }


    corporateDesign?.let {
      initCorporateDesign(it)
    }

    defaultI18nConfiguration?.let {
      updateDefaultI18nConfiguration(it)
    }

    initializeOnce()

    initialized = true
  }

  /**
   * This method is only called once - if the platform has not yet been initialized
   */
  abstract fun initializeOnce()

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.platform.MeisterChartsPlatform")
  }
}
