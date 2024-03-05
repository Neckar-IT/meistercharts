package it.neckar.open.formatting

import it.neckar.datetime.minimal.TimeConstants
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.kotlin.lang.toIntFloor
import it.neckar.open.unit.si.ms

/**
 * Formats a date and/or time
 */
interface DateTimeFormat {
  /**
   * Formats a date to a string
   */
  fun format(@ms timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String
}

/**
 * Formats a date and time in accordance to the ISO format 8601
 */
val dateTimeFormatIso8601: CachedDateTimeFormat = DateTimeFormatIso8601().cached()

/**
 * Formats only the date according to ISO 8601
 */
val dateFormatIso8601: CachedDateTimeFormat = DateFormatIso8601().cached()

/**
 * Formats only the (local!) time according to ISO 8601.
 */
val timeFormatIso8601: CachedDateTimeFormat = TimeFormatIso8601().cached()

/**
 * Formats a date as a UTC date (locale independent)
 */
val dateTimeFormatUTC: CachedDateTimeFormat = DateTimeFormatUTC().cached()

/**
 * A formatted date (time only)
 */
val timeFormat: CachedDateTimeFormat = TimeFormat().cached()

/**
 * A formatted time including milliseconds
 */
val timeFormatWithMillis: CachedDateTimeFormat = TimeFormatWithMillis().cached()

/**
 * A format for a date (without time)
 */
val dateFormat: CachedDateTimeFormat = DateFormat().cached()

/**
 * Prints the year and months
 */
val yearMonthFormat: CachedDateTimeFormat = YearMonthFormat().cached()

/**
 * Prints the year
 */
val yearFormat: CachedDateTimeFormat = YearFormat().cached()

/**
 * Formats the second and millisecond
 */
val secondMillisFormat: CachedDateTimeFormat = SecondMillisFormat().cached()

/**
 * A format for a date and a time
 */
val dateTimeFormat: CachedDateTimeFormat = DefaultDateTimeFormat().cached()

/**
 * A format for a date and a time (short date format)
 */
val dateTimeFormatShort: CachedDateTimeFormat = DateTimeFormatShort().cached()

/**
 * A format for a date and a time with millis
 */
val dateTimeFormatWithMillis: CachedDateTimeFormat = DateTimeFormatWithMillis().cached()

/**
 * A format for a date and a time with millis (short date format)
 */
val dateTimeFormatShortWithMillis: CachedDateTimeFormat = DateTimeFormatShortWithMillis().cached()

/**
 * Formats a date-time in accordance to the ISO format 8601 (https://en.wikipedia.org/wiki/ISO_8601)
 *
 * Example: `2001-09-08T21:46:40:00Z`
 *
 * This formatter always returns "Z" (UTC) as time zone
 */
expect class DateTimeFormatIso8601() : DateTimeFormat {
  companion object {
    /**
     * Parses the date-time string to a timestamp
     */
    fun parse(formattedIsoString: String): @ms Double
  }
}

expect class DateFormatIso8601() : DateTimeFormat

expect class TimeFormatIso8601() : DateTimeFormat

/**
 * Formats a date as a UTC date (locale independent)
 */
expect class DateTimeFormatUTC() : DateTimeFormat {
  companion object {
    /**
     * Parses the UTC string to a timestamp
     */
    fun parse(formattedUtc: String): @ms Double
  }
}

/**
 * A formatted date (time only)
 */
expect class TimeFormat() : DateTimeFormat

/**
 * A formatted time including milliseconds
 */
expect class TimeFormatWithMillis() : DateTimeFormat

/**
 * A format for a date (without time)
 */
expect class DateFormat() : DateTimeFormat

/**
 * A format that formats a date - but only prints the month and year
 */
expect class YearMonthFormat() : DateTimeFormat

/**
 * A format that formats a date - but only prints the year
 */
expect class YearFormat() : DateTimeFormat

/**
 * Formats a time stamp as second with millis
 */
expect class SecondMillisFormat() : DateTimeFormat

/**
 * A format for a date and a time
 */
expect class DefaultDateTimeFormat() : DateTimeFormat

/**
 * A format for a date and a time (short date format)
 */
expect class DateTimeFormatShort() : DateTimeFormat

/**
 * A format for a date and a time with millis
 */
expect class DateTimeFormatWithMillis() : DateTimeFormat

/**
 * A format for a date and a time with millis (short date format)
 */
expect class DateTimeFormatShortWithMillis() : DateTimeFormat


/**
 * Formats millis as UTC. Uses [dateTimeFormatUTC] - with cache
 */
fun @ms Double.formatUtc(whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
  if (this.isNaN()) {
    return "NaN"
  }

  if (this.isInfinite()) {
    return "∞"
  }

  return dateTimeFormatUTC.format(this, I18nConfiguration.GermanyUTC, whitespaceConfig)
}

/**
 * Parses a UTC date - reverse for [formatUtc
 */
fun parseUtc(formattedUtc: String): @ms Double {
  if (formattedUtc == "NaN") {
    return Double.NaN
  }

  if (formattedUtc == "∞") {
    return Double.POSITIVE_INFINITY
  }

  return DateTimeFormatUTC.parse(formattedUtc)
}


/**
 * Formats this (interpreted as milliseconds) as human-readable duration
 */
fun @ms Double.formatAsDuration(whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
  if (this.isNaN()) {
    return "NaN"
  }

  if (this.isInfinite()) {
    return "∞"
  }


  var days: Int = 0
  var hours: Int = 0
  var minutes: Int = 0
  var seconds: Int = 0
  var milliseconds: Double = this

  if (true) {
    days = (milliseconds / TimeConstants.millisPerDay).toIntFloor()
    milliseconds = milliseconds - days * TimeConstants.millisPerDay
  }
  if (true) {
    hours = (milliseconds / TimeConstants.millisPerHour).toIntFloor()
    milliseconds = milliseconds - hours * TimeConstants.millisPerHour
  }
  if (true) {
    minutes = (milliseconds / TimeConstants.millisPerMinute).toIntFloor()
    milliseconds = milliseconds - minutes * TimeConstants.millisPerMinute
  }
  if (true) {
    seconds = (milliseconds / TimeConstants.millisPerSecond).toIntFloor()
    milliseconds = milliseconds - seconds * TimeConstants.millisPerSecond
  }

  return buildString {
    var addedSomething = false

    if (addedSomething || days != 0) {
      append("${intFormat.format(days.toDouble())}${whitespaceConfig.smallSpace}days${whitespaceConfig.space}")
      addedSomething = true
    }
    if (addedSomething || hours != 0) {
      append("${hours}${whitespaceConfig.smallSpace}h${whitespaceConfig.space}")
      addedSomething = true
    }
    if (addedSomething || minutes != 0) {
      append("${minutes}${whitespaceConfig.smallSpace}min${whitespaceConfig.space}")
      addedSomething = true
    }
    if (addedSomething || seconds != 0) {
      append("${seconds}${whitespaceConfig.smallSpace}s${whitespaceConfig.space}")
      addedSomething = true
    }

    append("${milliseconds}${whitespaceConfig.smallSpace}ms")
  }
}
