package it.neckar.open.i18n

import it.neckar.open.time.TimeZone

/**
 * Contains the configuration for I18n
 */
data class I18nConfiguration(
  /**
   * The locale that is used to resolve texts
   */
  val textLocale: Locale,

  /**
   * The locale that is used to format numbers and dates.
   */
  val formatLocale: Locale,

  /**
   * The time zone
   */
  var timeZone: TimeZone,
) {

  companion object {
    /**
     * Convenience constructor emulation - useful when refactoring
     */
    operator fun invoke(timeZone: TimeZone, locale: Locale): I18nConfiguration {
      return I18nConfiguration(locale, locale, timeZone)
    }

    /**
     * I18n configuration for Germany
     */
    val Germany: I18nConfiguration = I18nConfiguration(
      textLocale = Locale.Germany,
      formatLocale = Locale.Germany,
      timeZone = TimeZone.Berlin
    )

    /**
     * German locales with time zone set to UTC
     */
    val GermanyUTC: I18nConfiguration = I18nConfiguration(
      textLocale = Locale.Germany,
      formatLocale = Locale.Germany,
      timeZone = TimeZone.UTC
    )

    val US: I18nConfiguration = I18nConfiguration(
      textLocale = Locale.US,
      formatLocale = Locale.US,
      timeZone = TimeZone.NewYork
    )

    val US_UTC: I18nConfiguration = I18nConfiguration(
      textLocale = Locale.US,
      formatLocale = Locale.US,
      timeZone = TimeZone.UTC
    )
  }
}
