package com.meistercharts.js

import com.meistercharts.canvas.FontMetricsCacheAccess
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.MeisterChartsFactoryAccess
import com.meistercharts.canvas.UrlConversion
import com.meistercharts.canvas.UrlConverter
import com.meistercharts.design.CorporateDesign
import com.meistercharts.design.initCorporateDesign
import com.meistercharts.js.external.FontFaceSet
import com.meistercharts.js.external.listenForLoadingDone
import com.meistercharts.events.FontLoadedEventBroker
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.updateDefaultI18nConfiguration
import com.meistercharts.version.MeisterChartsVersion
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.logging.info
import kotlinx.browser.document
import org.w3c.dom.get

/**
 * Global configuration / settings object for [com.meistercharts.canvas.MeisterChart].
 *
 * Is referenced from [MeisterChartBuilder] and ensures that initial code is executed once
 */
object MeisterChartsPlatform {

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
    urlConverter: UrlConverter = UrlConverter.Noop,
  ) {
    logger.info { "Initializing MeisterChartsPlatform ${MeisterChartsVersion.versionAsStringVerbose}" }

    UrlConversion.activate(urlConverter)

    corporateDesign?.let {
      initCorporateDesign(it)
    }

    defaultI18nConfiguration?.let {
      updateDefaultI18nConfiguration(it)
    }

    FontMetricsCacheAccess.fontMetricsCache = FontMetricsCacheJS

    //KotlinLoggingConfiguration.LOG_LEVEL = KotlinLoggingLevel.INFO
    MeisterChartsFactoryAccess.factory = MeisterChartsFactoryJS()

    logger.debug { "MeisterChartsPlatform initialized successfully.\ncorporateDesign: $corporateDesign\ndefaultI18nConfiguration: $defaultI18nConfiguration" }
  }
}

private val logger = LoggerFactory.getLogger("com.meistercharts.js.MeisterChartsPlatform")
