package it.neckar.open.formatting

import it.neckar.datetime.minimal.toZoneId
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.convert
import it.neckar.open.kotlin.lang.SpecialChars
import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.time.DateUtils
import it.neckar.open.time.toDoubleMillis
import it.neckar.open.unit.si.ms
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.time.temporal.ChronoField
import java.time.temporal.TemporalQueries

actual class DateTimeFormatIso8601 : DateTimeFormat {
  /**
   * Append three digits for the milliseconds
   */
  private val isoTimeFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.HOUR_OF_DAY, 2)
    .appendLiteral(':')
    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
    .optionalStart()
    .appendLiteral(':')
    .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
    .optionalStart()
    .appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
    .toFormatter()

  /**
   * Uses the custom [isoTimeFormatter]
   */
  private val isoLocalDateTime = DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(DateTimeFormatter.ISO_LOCAL_DATE)
    .appendLiteral('T')
    .append(isoTimeFormatter)
    .toFormatter()

  private val iso8601DateTimeFormatter = DateTimeFormatterBuilder()
    .append(isoLocalDateTime)
    .optionalStart()
    .appendOffsetId()
    .toFormatter()

  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    //Always return UTC
    return DateUtils.toOffsetDateTime(timestamp.toLong(), ZoneOffset.UTC).format(iso8601DateTimeFormatter)
  }

  actual companion object {
    /**
     * Parses the date-time string to a timestamp
     */
    @Throws(DateTimeParseException::class)
    actual fun parse(formattedIsoString: String): @ms Double {
      DateTimeFormatter.ISO_DATE_TIME.parse(formattedIsoString).let {
        val date = it.query(TemporalQueries.localDate())
        val time = it.query(TemporalQueries.localTime())
        val zoneId = it.query(TemporalQueries.zone())

        val instant = ZonedDateTime.of(date, time, zoneId).toInstant()
        return instant.toDoubleMillis()
      }
    }
  }
}

actual class DateFormatIso8601 : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return DateUtils.toZonedDateTime(timestamp.toLong(), i18nConfiguration.timeZone.toZoneId()).format(DateTimeFormatter.ISO_LOCAL_DATE)
  }
}

actual class TimeFormatIso8601 : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return DateUtils.toZonedDateTime(timestamp.toLong(), i18nConfiguration.timeZone.toZoneId()).format(DateTimeFormatter.ISO_LOCAL_TIME)
  }
}

actual class DateTimeFormatUTC : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    return DateUtils.toOffsetDateTime(timestamp.toLong(), ZoneOffset.UTC).format(it.neckar.open.time.utcDateTimeFormat)
  }

  actual companion object {
    /**
     * Parses the UTC string to a timestamp
     */
    actual fun parse(formattedUtc: String): @ms Double {
      it.neckar.open.time.utcDateTimeFormat.parse(formattedUtc).let {
        val date = it.query(TemporalQueries.localDate())
        val time = it.query(TemporalQueries.localTime())

        val instant = ZonedDateTime.of(date, time, ZoneOffset.UTC).toInstant()
        return instant.toDoubleMillis()
      }
    }
  }
}

actual class TimeFormat : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()), i18nConfiguration.timeZone.toZoneId())
    return DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(i18nConfiguration.formatLocale.convert()).format(dateTime)
  }
}

actual class TimeFormatWithMillis : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()), i18nConfiguration.timeZone.toZoneId())
    return DateUtils.createTimeMillisFormat(i18nConfiguration.formatLocale.convert()).format(dateTime)
  }
}

actual class DateFormat : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()), i18nConfiguration.timeZone.toZoneId())
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(i18nConfiguration.formatLocale.convert()).format(dateTime)
  }
}

actual class DefaultDateTimeFormat : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()), i18nConfiguration.timeZone.toZoneId())
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(i18nConfiguration.formatLocale.convert()).format(dateTime)
  }
}

actual class DateTimeFormatShort : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()), i18nConfiguration.timeZone.toZoneId())
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(i18nConfiguration.formatLocale.convert()).format(dateTime)
  }
}

actual class DateTimeFormatWithMillis : DateTimeFormat {
  override fun format(timestamp: @ms Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    if (timestamp.isNaN()) {
      return "NaN"
    }

    if (timestamp.isInfinite()) {
      return "∞"
    }
    val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()), i18nConfiguration.timeZone.toZoneId())
    return DateUtils.createDateTimeMillisFormat(i18nConfiguration.formatLocale.convert()).format(dateTime)
  }
}

actual class DateTimeFormatShortWithMillis : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()), i18nConfiguration.timeZone.toZoneId())
    return DateUtils.createDateTimeShortMillisFormat(i18nConfiguration.formatLocale.convert()).format(dateTime)
  }
}

/**
 * A format that formats a date - but only prints the month and year
 */
actual class YearMonthFormat actual constructor() : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()), i18nConfiguration.timeZone.toZoneId())
    return monthYearFormatSpace(whitespaceConfig).withLocale(i18nConfiguration.formatLocale.convert()).format(dateTime)
  }

  companion object {
    fun monthYearFormatSpace(whitespaceConfig: WhitespaceConfig): DateTimeFormatter {
      return when (whitespaceConfig) {
        WhitespaceConfig.NonBreaking, WhitespaceConfig.NonBreakingOnlyNbsp -> monthYearFormatNbsp
        WhitespaceConfig.Spaces -> monthYearFormatSpace
      }
    }

    val monthYearFormatSpace: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    val monthYearFormatNbsp: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM${SpecialChars.nbsp}yyyy")
  }
}

actual class YearFormat actual constructor() : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()), i18nConfiguration.timeZone.toZoneId())
    return yearFormat.withLocale(i18nConfiguration.formatLocale.convert()).format(dateTime)
  }

  companion object {
    val yearFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy")
  }
}

/**
 * Formats a time stamp as second with millis
 */
actual class SecondMillisFormat actual constructor() : DateTimeFormat {
  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp.toLong()), i18nConfiguration.timeZone.toZoneId())

    val value = dateTime.second + dateTime.nano / 1_000_000_000.0
    return decimalFormat(3).format(value, i18nConfiguration)
  }
}
