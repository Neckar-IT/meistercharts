package it.neckar.open.i18n

/**
 * A service that resolves texts - for a given locale.
 *
 * Attention: Does *not* replace parameters! Just resolves texts
 */
fun interface TextResolver {
  /**
   * Resolves the text for the given key and locale
   */
  fun resolve(
    key: TextKey,
    i18nConfiguration: I18nConfiguration
  ): String?
}
