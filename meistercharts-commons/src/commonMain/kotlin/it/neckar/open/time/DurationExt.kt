package it.neckar.open.time

import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.kotlin.lang.toIntCeil
import it.neckar.open.kotlin.lang.toIntFloor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 *
 */

/**
 * Formats the duration as "hours:minutes"
 */
fun Duration.formatHourAndMinutesShort(): String {
  val minutes = inWholeMinutes
  val hours = (minutes / 60.0).toIntFloor()
  val remainingMinutes = minutes - hours * 60

  return "${hours}:${remainingMinutes.toString().padStart(2, '0')}"
}

/**
 * Formats the duration as "17h 12min"
 */
fun Duration.formatHourAndMinutes(whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
  val minutes = inWholeMinutes
  val hours = (minutes / 60.0).toIntFloor()
  val remainingMinutes = minutes - hours * 60

  return "${hours}${whitespaceConfig.smallSpace}h${whitespaceConfig.space}${remainingMinutes.toString().padStart(2, '0')}${whitespaceConfig.smallSpace}min"
}

fun Duration.formatMinutes(whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
  return "$inWholeMinutes${whitespaceConfig.smallSpace}min"
}

/**
 * Formats the duration as string - without milliseconds

 */
fun Duration.formatWithoutMillis(): String {
  return this.inWholeSeconds.seconds.toString()
}

fun Duration.formatPersonDays(personDayDuration: Duration = 8.hours, whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String {
  val days = (this / personDayDuration).toIntCeil()
  return "${days}${whitespaceConfig.smallSpace}Personentage${whitespaceConfig.smallSpace}(à ${personDayDuration.formatHourAndMinutes()})"
}

/**
 * Calculates the sum
 */
fun Iterable<Duration>.sum(): Duration {
  var duration: Duration = Duration.ZERO
  this.forEach { duration += it }
  return duration
}
