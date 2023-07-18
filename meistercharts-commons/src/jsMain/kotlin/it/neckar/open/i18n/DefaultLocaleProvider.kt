package it.neckar.open.i18n

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.logging.ifDebug
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.time.TimeZone
import kotlinx.browser.window
import org.w3c.dom.Navigator

/**
 * Provides the default locale
 */
actual class DefaultLocaleProvider {
  /**
   * Returns the default locale (from the browser or os)
   */
  actual val defaultLocale: Locale
    get() = getBrowserLocale()

  /**
   * Returns the locale from the browser
   */
  fun getBrowserLocale(): Locale {
    val navigator: Navigator = window.navigator

    if (navigator.language == "C") {
      //Fallback to ensure the locale is supported
      return Locale.US
    }

    //The locale that has been calculated using the browser language
    val locale = Locale(navigator.language)

    return try {
      val i18nConfiguration = I18nConfiguration(textLocale = locale, formatLocale = locale, timeZone = TimeZone.UTC) //timezone doesn't matter here
      //Do *not* access [DefaultI18nConfiguration] - has not yet been initialized!

      decimalFormat.format(0.0, i18nConfiguration)

      //The locale can be used to format a string, use it
      locale
    } catch (e: Throwable) {
      //Formatting with the given locale does not work. Fallback to US
      logger.info("Locale <${locale.locale}> not supported. Falling back to ${Locale.US}")
      logger.ifDebug {
        logger.debug { "Exception when formatting: ${e.message}\n$e" }
        e.printStackTrace()
      }
      Locale.US
    }
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("it.neckar.open.i18n.DefaultLocaleProvider")
  }
}
