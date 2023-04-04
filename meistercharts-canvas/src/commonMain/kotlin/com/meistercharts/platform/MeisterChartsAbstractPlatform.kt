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
