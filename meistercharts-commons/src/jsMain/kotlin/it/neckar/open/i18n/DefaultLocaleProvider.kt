package it.neckar.open.i18n

import it.neckar.open.formatting.decimalFormat
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
      decimalFormat.format(0.0, DefaultI18nConfiguration.copy(formatLocale = locale))

      //The locale can be used to format a string, use it
      locale
    } catch (ignore: Throwable) {
      //Formatting with the given locale does not work. Fallback to US
      println("Locale <${locale.locale}> not supported. Falling back to ${Locale.US}")
      Locale.US
    }
  }
}
