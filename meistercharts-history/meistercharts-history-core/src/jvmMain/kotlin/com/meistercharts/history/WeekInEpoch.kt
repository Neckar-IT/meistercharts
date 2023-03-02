package com.meistercharts.history

import org.threeten.extra.Weeks
import java.text.NumberFormat
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.annotation.Nonnull

/**
 * Represents a week
 *
 */
data class WeekInEpoch(val week: Int) {
  /**
   * Returns the local date that represents the start of the week
   */
  val startOfWeek: LocalDate
    get() = base.plusWeeks(week.toLong()).toLocalDate()

  override fun toString(): String {
    return week.toString()
  }

  /**
   * Formats the week with the date
   */
  fun formatWithDate(): String {
    val numberFormat = NumberFormat.getNumberInstance()
    numberFormat.minimumIntegerDigits = 4
    numberFormat.isGroupingUsed = false
    return numberFormat.format(week.toLong()) + "_" + startOfWeek
  }

  /**
   * Converts the start of this week to an offset date time
   */
  fun asOffsetDateTime(): OffsetDateTime {
    return base.plusWeeks(week.toLong())
  }

  companion object {
    /**
     * The first monday before 1970-1-1
     */
    val base: OffsetDateTime = OffsetDateTime.of(1970, 1, 5, 0, 0, 0, 0, ZoneOffset.UTC).minusDays(7)

    fun parse(@Nonnull directoryName: String): WeekInEpoch {
      val weekString = directoryName.substring(0, directoryName.indexOf('_'))
      return WeekInEpoch(weekString.toInt())
    }

    /**
     * Calculates the week in epoch for the given date
     *
     * @param date the date
     * @return the week in epoch
     */
    fun calculate(@Nonnull date: OffsetDateTime): WeekInEpoch {
      return WeekInEpoch(Weeks.between(base, date).amount)
    }
  }
}
