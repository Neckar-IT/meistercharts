package it.neckar.open.i18n

/**
 * Provides the default locale
 */
expect class DefaultLocaleProvider() {
  /**
   * Returns the default locale (from the browser or os)
   */
  val defaultLocale: Locale
}
