package it.neckar.datetime.minimal

import it.neckar.open.unit.si.ms

/**
 * Represents a local date + time
 */
data class LocalDateTime(
  val date: LocalDate,
  val time: LocalTime,
) : Comparable<LocalDateTime> {

  inline val year: Year
    get() {
      return date.year
    }
  inline val month: Month
    get() {
      return date.month
    }

  inline val dayOfMonth: DayOfMonth
    get() {
      return date.dayOfMonth
    }

  inline val hour: Int
    get() {
      return time.hour
    }

  inline val minute: Int
    get() {
      return time.minute
    }

  inline val second: Int
    get() {
      return time.second
    }

  inline val millis: Int
    get() {
      return time.millis
    }

  override fun compareTo(other: LocalDateTime): Int {
    return when {
      this.date != other.date -> this.date.compareTo(other.date)
      this.time != other.time -> this.time.compareTo(other.time)
      else -> 0
    }
  }

  /**
   * Creates a new instance at the start of the current hour
   */
  fun atStartOfHour(): LocalDateTime {
    return LocalDateTime(date, time.startStartOfHour())
  }

  /**
   * Creates a new instance at the start of the provided hour
   */
  fun atStartOfHour(hour: Int): LocalDateTime {
    return LocalDateTime(date, time.startStartOfHour(hour))
  }

  fun format(): String {
    return "${date.format()}T${time.format()}"
  }

  override fun toString(): String {
    return format()
  }

  companion object {
    /**
     * Creates a new instance
     */
    fun of(
      year: Int, month: Int, day: Int,
      hour: Int, minute: Int, second: Int, millis: Int = 0,
    ): LocalDateTime {
      return LocalDateTime(
        LocalDate(year, month, day),
        LocalTime(hour, minute, second, millis)
      )
    }
  }
}

expect fun LocalDateTime.Companion.fromMillisCurrentTimeZone(millis: @ms Double, expectedTimeZone: TimeZone): LocalDateTime

expect fun LocalDateTime.toMillis(timeZone: TimeZone): @ms Double
