package it.neckar.open.formatting

import it.neckar.open.collections.cache
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.time.TimeZone
import it.neckar.open.unit.number.PositiveOrZero
import kotlin.js.Date

/**
 * The formatted date is always UTC.
 */
actual class DateTimeFormatIso8601 : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    //https://developer.mozilla.org/de/docs/Web/JavaScript/Reference/Global_Objects/Date/toISOString
    return Date(timestamp).toISOString()
  }
}

actual class DateFormatIso8601 : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val date = Date(timestamp)
    return "${date.getFullYear()}-${(date.getMonth() + 1).formatWithLeadingZeros()}-${date.getDate().formatWithLeadingZeros()}"
  }
}

actual class TimeFormatIso8601 : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val date = Date(timestamp)
    return date.toLocaleTimeString(i18nConfiguration.formatLocale.locale, localeOptions(i18nConfiguration.timeZone) {
      hour12 = false
      hour = "2-digit"
      minute = "2-digit"
      second = "2-digit"
    }) + "." + date.getMilliseconds().formatWithLeadingZeros(3)
  }
}

/**
 * ATTENTION! This method must only be used for positive values
 */
private fun @PositiveOrZero Int.formatWithLeadingZeros(length: Int = 2): String {
  return this.toString().padStart(length, '0')
}

/**
 * The formatted date is always UTC.
 */
actual class DateTimeFormatUTC : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    //https://developer.mozilla.org/de/docs/Web/JavaScript/Reference/Global_Objects/Date/toISOString
    return Date(timestamp).toISOString()
  }
}

/**
 * A formatted date (no date)
 */
actual class TimeFormat : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return Date(timestamp).toLocaleTimeString(i18nConfiguration.formatLocale.locale, localeOptions(i18nConfiguration.timeZone))
  }
}

actual class TimeFormatWithMillis : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val date = Date(timestamp)
    val formattedTimeWithoutMillis = date.toLocaleTimeString(i18nConfiguration.formatLocale.locale, localeOptions(i18nConfiguration.timeZone))
    return insertMillis(formattedTimeWithoutMillis, date.getUTCMilliseconds())
  }
}

actual class DateFormat : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return Date(timestamp).toLocaleDateString(i18nConfiguration.formatLocale.locale, localeOptions(i18nConfiguration.timeZone))
  }
}

actual class DefaultDateTimeFormat : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return Date(timestamp).toLocaleString(i18nConfiguration.formatLocale.locale, localeOptions(i18nConfiguration.timeZone))
  }
}

actual class DateTimeFormatShort : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return Date(timestamp).toLocaleString(i18nConfiguration.formatLocale.locale, localeOptions(i18nConfiguration.timeZone) {
      dateStyle = "short"
      timeStyle = "short"
    })
  }
}

actual class DateTimeFormatWithMillis : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val date = Date(timestamp)
    val formattedDateTimeWithoutMillis = date.toLocaleString(i18nConfiguration.formatLocale.locale, localeOptions(i18nConfiguration.timeZone))
    return insertMillis(formattedDateTimeWithoutMillis, date.getUTCMilliseconds())
  }
}

actual class DateTimeFormatShortWithMillis : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val date = Date(timestamp)
    val formattedDateTimeWithoutMillis = date.toLocaleString(i18nConfiguration.formatLocale.locale, localeOptions(i18nConfiguration.timeZone))
    return insertMillis(formattedDateTimeWithoutMillis, date.getUTCMilliseconds())
  }
}

/**
 * A format that formats a date - but only prints the month and year
 */
actual class YearMonthFormat actual constructor() : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val date = Date(timestamp)

    return date.toLocaleString(i18nConfiguration.formatLocale.locale, localeOptions(i18nConfiguration.timeZone) {
      month = "long"
      year = "numeric"
    })
  }
}

actual class YearFormat actual constructor() : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val date = Date(timestamp)

    return date.toLocaleString(i18nConfiguration.formatLocale.locale, localeOptions(i18nConfiguration.timeZone) {
      year = "numeric"
    })
  }
}

/**
 * Formats a time stamp as second with millis
 */
actual class SecondMillisFormat actual constructor() : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val date = Date(timestamp)
    val number = date.getSeconds() + date.getMilliseconds() / 1000.0
    return number.format(
      numberOfDecimals = 3,
      i18nConfiguration = i18nConfiguration
    )
  }
}

private val localeOptionsCache = cache<TimeZone, Date.LocaleOptions>("localeOptionsCache", 10)

private fun localeOptions(timeZone: TimeZone, additionalConfig: Date.LocaleOptions.() -> Unit = {}): Date.LocaleOptions {
  return localeOptionsCache.getOrStore(timeZone) {
    dateLocaleOptions {
      this.timeZone = timeZone.zoneId
    }
  }.apply {
    //reset all options to avoid problems when reusing a config - every time
    year = undefined
    month = undefined
    day = undefined
    era = undefined
    hour = undefined
    hour12 = undefined
    minute = undefined
    second = undefined
    weekday = undefined
    timeZoneName = undefined
    localeMatcher = undefined
    formatMatcher = undefined

    //Specific settings for date/time
    timeStyle = undefined
    dateStyle = undefined

    //Apply the additional config
    this.additionalConfig()
  }
}

/**
 * Inserts [millis] milliseconds into the formatted date [formattedWithoutMillis] which has no milliseconds part yet
 */
private fun insertMillis(formattedWithoutMillis: String, millis: Int): String {
  try {
    // This is a crude workaround to display milliseconds for the most common locales (which use ':' as a separator between hour, minute and second).
    // Unfortunately there is no browser supported way to format a date with milliseconds directly.
    val firstIndexOfSeparator = formattedWithoutMillis.indexOf(":")
    if (firstIndexOfSeparator != -1) {
      val secondIndexOfSeparator = formattedWithoutMillis.indexOf(":", firstIndexOfSeparator + 1)
      if (secondIndexOfSeparator != -1) {
        var indexAfterLastDigit = secondIndexOfSeparator + 1
        while (indexAfterLastDigit < formattedWithoutMillis.length) {
          if (!formattedWithoutMillis[indexAfterLastDigit].isDigit()) {
            break
          }
          ++indexAfterLastDigit
        }
        val formattedMillisPart = millis.toString().padStart(3, '0')
        return formattedWithoutMillis.substring(0, indexAfterLastDigit) + '.' + formattedMillisPart + formattedWithoutMillis.substring(indexAfterLastDigit)
      }
    }
  } catch (e: Exception) {
    println("failed to format date <$formattedWithoutMillis> with milliseconds: $e")
  }
  return formattedWithoutMillis
}

private fun Char.isDigit(): Boolean {
  return when (this) {
    '0' -> true
    '1' -> true
    '2' -> true
    '3' -> true
    '4' -> true
    '5' -> true
    '6' -> true
    '7' -> true
    '8' -> true
    '9' -> true
    else -> false
  }
}

var Date.LocaleOptions.dateStyle: String?
  get() {
    return asDynamic().dateStyle as? String
  }
  set(value) {
    asDynamic().dateStyle = value
  }

var Date.LocaleOptions.timeStyle: String?
  get() {
    return asDynamic().timeStyle as? String
  }
  set(value) {
    asDynamic().timeStyle = value
  }
