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

val Duration.inWholeWeeks: Long
  get() = inWholeDays / 7

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

/**
 * Formats the duration in a nice, human-readable way (e.g. "vor 3 Tagen")
 * @param whitespaceConfig the whitespace configuration
 * @return the formatted string or null if the duration longer than 4 weeks
 */
fun Duration.formatHumanized(whitespaceConfig: WhitespaceConfig = WhitespaceConfig.NonBreaking): String? {
  return if (inWholeMilliseconds < 0) "in der Zukunft"
  else if (inWholeMilliseconds < 1000) "gerade eben"
  else if (inWholeSeconds < 60) buildString {
    append("vor")
    append(whitespaceConfig.space)
    append(inWholeSeconds)
    append(whitespaceConfig.space)
    append("Sekunde")
    if (inWholeSeconds > 1) append("n")
  }
  else if (inWholeMinutes < 60) buildString {
    append("vor")
    append(whitespaceConfig.space)
    append(inWholeMinutes)
    append(whitespaceConfig.space)
    append("Minute")
    if (inWholeMinutes > 1) append("n")
  }
  else if (inWholeHours < 24) buildString {
    append("vor")
    append(whitespaceConfig.space)
    append(inWholeHours)
    append(whitespaceConfig.space)
    append("Stunde")
    if (inWholeHours > 1) append("n")
  }
  else if (inWholeDays < 7) buildString {
    append("vor")
    append(whitespaceConfig.space)
    append(inWholeDays)
    append(whitespaceConfig.space)
    append("Tag")
    if (inWholeDays > 1) append("en")
  }
  else if (inWholeWeeks < 4) buildString {
    append("vor")
    append(whitespaceConfig.space)
    append(inWholeWeeks)
    append(whitespaceConfig.space)
    append("Woche")
    if (inWholeWeeks > 1) append("n")
  }
  else null
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
