package it.neckar.open.formatting

import it.neckar.datetime.minimal.LocalDate
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import kotlin.js.Date

actual class DefaultLocalDateFormat actual constructor() : LocalDateFormat {
  actual override fun format(localDate: LocalDate, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return Date(localDate.year.value, localDate.month.value - 1, localDate.dayOfMonth.value)
      .toLocaleDateString(i18nConfiguration.formatLocale.locale, localeOptions(i18nConfiguration.timeZone))
  }
}
