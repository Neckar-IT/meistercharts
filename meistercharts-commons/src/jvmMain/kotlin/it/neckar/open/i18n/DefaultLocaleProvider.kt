package it.neckar.open.i18n

import it.neckar.open.collections.cache

private val fx2localeCache = cache<java.util.Locale, it.neckar.open.i18n.Locale>("fx2localeCache", 50)
private val locale2FxCache = cache<it.neckar.open.i18n.Locale, java.util.Locale>("locale2FxCache", 50)

/**
 * Converts the given java.util.Locale
 */
fun java.util.Locale.convert(): it.neckar.open.i18n.Locale {
  return fx2localeCache.getOrStore(this) {
    it.neckar.open.i18n.Locale(this.toLanguageTag())
  }
}

/**
 * Converts the locale to a java.util.Locale
 */
fun it.neckar.open.i18n.Locale.convert(): java.util.Locale {
  return locale2FxCache.getOrStore(this) {
    java.util.Locale.forLanguageTag(this.locale)
  }
}

/**
 * Provides the default locale
 */
actual class DefaultLocaleProvider {
  /**
   * Returns the default locale (from the browser or os)
   */
  actual val defaultLocale: Locale
    get() = java.util.Locale.getDefault().convert()

}
