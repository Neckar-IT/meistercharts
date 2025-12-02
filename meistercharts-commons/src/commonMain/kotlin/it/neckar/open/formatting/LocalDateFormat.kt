package it.neckar.open.formatting

import it.neckar.datetime.minimal.LocalDate
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.unit.si.ms

/**
 * Formats a local date
 */
interface LocalDateFormat {
  /**
   * Formats a date to a string
   */
  fun format(@ms localDate: LocalDate, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String

  /**
   * Format the provided timestamp. If the timestamp is null, the [fallback] is returned.
   */
  fun formatNullable(@ms localDate: LocalDate?, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking, fallback: String): String {
    if (localDate == null) {
      return fallback
    }

    return format(localDate, i18nConfiguration, whitespaceConfig)
  }
}

/**
 * Formats a date according to ISO 8601
 */
val localDateFormatIso8601: CachedLocalDateFormat = LocalDateFormatIso8601().cached()

val localDateFormat: CachedLocalDateFormat = DefaultLocalDateFormat().cached()


class LocalDateFormatIso8601 : LocalDateFormat {
  override fun format(@ms localDate: LocalDate, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return localDate.format()
  }
}

/**
 * Formats a date according to the locale
 */
expect class DefaultLocalDateFormat() : LocalDateFormat {
  override fun format(localDate: LocalDate, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String
}
