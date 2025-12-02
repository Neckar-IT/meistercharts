package it.neckar.open.formatting

import it.neckar.datetime.minimal.LocalDate
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.convert
import it.neckar.open.kotlin.lang.WhitespaceConfig
import java.time.chrono.Chronology
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle

actual class DefaultLocalDateFormat : LocalDateFormat {
  actual override fun format(localDate: LocalDate, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val javaLocalDate = java.time.LocalDate.of(localDate.year.value, localDate.month.value, localDate.dayOfMonth.value)

    val locale = i18nConfiguration.formatLocale.convert()
    val localizedDateTimePattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT, null, Chronology.ofLocale(locale), locale)
    return java.time.format.DateTimeFormatter.ofPattern(localizedDateTimePattern).format(javaLocalDate)
  }
}
