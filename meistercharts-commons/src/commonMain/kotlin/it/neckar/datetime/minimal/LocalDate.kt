package it.neckar.datetime.minimal

import it.neckar.open.unit.si.ms

/**
 * Represents a local date.
 *
 * Attention: This is a very minimalistic implementation without any special checks.
 */
data class LocalDate(
  val year: Year,
  val month: Month,
  val dayOfMonth: DayOfMonth,
) : Comparable<LocalDate> {

  /**
   * Calculates the months since the start of the epoch
   */
  fun monthOfEpoche(): Int {
    return year.value * 12 + month.value
  }

  fun dayOfYear(): Int {
    return month.daysBeforeThisMonth(year) + dayOfMonth.value
  }

  fun format(): String {
    val formattedYear = year.value.toString()
    val formattedMonth = month.value.toString().padStart(2, '0')
    val formattedDay = dayOfMonth.value.toString().padStart(2, '0')

    return "$formattedYear-$formattedMonth-$formattedDay"
  }

  /**
   * Creates a new date at the start of the month
   */
  fun atStartOfMonth(): LocalDate {
    return atDayOfMonth(DayOfMonth.FirstDay)
  }

  fun atStartOfNextMonth(): LocalDate {
    return atStartOfMonth().plusMonths(1)
  }

  fun atStartOfYear(): LocalDate {
    return LocalDate(year, Month.January, DayOfMonth.FirstDay)
  }

  fun atStartOfNextYear(): LocalDate {
    return LocalDate(year + 1, Month.January, DayOfMonth.FirstDay)
  }

  /**
   * Creates a new instance with the new day of month
   */
  fun atDayOfMonth(newDayOfMonth: DayOfMonth): LocalDate {
    return LocalDate(year, month, newDayOfMonth)
  }

  fun withYear(year: Year): LocalDate {
    return LocalDate(year, month, dayOfMonth)
  }

  fun withMonth(month: Month): LocalDate {
    val updatedDayOfMonth = dayOfMonth.coerceDayOfMonth(year, month)
    return LocalDate(year, month, updatedDayOfMonth)
  }

  fun withDay(dayOfMonth: DayOfMonth): LocalDate {
    return LocalDate(year, month, dayOfMonth)
  }

  fun plusMonths(monthsToAdd: Int): LocalDate {
    if (monthsToAdd == 0) {
      return this
    }

    val newMonthValue = (month.value - 1 + monthsToAdd + 12) % 12 + 1
    val newYearValue = year.value + (month.value - 1 + monthsToAdd) / 12

    val newYear = Year(newYearValue)
    val newMonth = Month(newMonthValue)

    val newDayOfMonth = dayOfMonth.coerceDayOfMonth(newYear, newMonth)
    return LocalDate(newYear, newMonth, newDayOfMonth)
  }

  fun plusDays(daysToAdd: Int): LocalDate {
    require(daysToAdd in -10_000..10_000) {
      "Days to add ($daysToAdd) are out of range - performance too bad"
    }

    var newDayOfMonth = this.dayOfMonth.value + daysToAdd
    var newMonth = this.month
    var newYear = this.year

    while (newDayOfMonth > newMonth.daysInMonth(newYear).value) {
      newDayOfMonth -= newMonth.daysInMonth(newYear).value
      newMonth = Month((newMonth.value % 12) + 1)
      if (newMonth.value == 1) {
        newYear = Year(newYear.value + 1)
      }
    }

    return LocalDate(newYear, newMonth, DayOfMonth(newDayOfMonth))
  }

  fun plusYears(yearsToAdd: Int): LocalDate {
    return LocalDate(year + yearsToAdd, month, dayOfMonth)
  }

  override fun compareTo(other: LocalDate): Int {
    return when {
      this.year.value != other.year.value -> this.year.value - other.year.value
      this.month.value != other.month.value -> this.month.value - other.month.value
      else -> this.dayOfMonth.value - other.dayOfMonth.value
    }
  }

  override fun toString(): String {
    return format()
  }

  companion object {
    operator fun invoke(
      year: Int,
      month: Int,
      dayOfMonth: Int,
    ): LocalDate {
      return LocalDate(Year(year), Month(month), DayOfMonth(dayOfMonth))
    }
  }
}

/**
 * Calculates the UTC timestamp
 *
 * Attention: At the moment the time zone is ignored on JS
 */
expect fun LocalDate.toMillisAtStartOfDay(timeZone: TimeZone): @ms Double

/**
 * Creates a [LocalDate] from the provided milliseconds value.
 * Does only support the *current* time zone - due to limitations of the browsers.
 *
 * Provide the [expectedTimeZone] to avoid errors due to different time zones.
 */
expect fun LocalDate.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): LocalDate
