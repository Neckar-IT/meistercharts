package it.neckar.open.time

import it.neckar.open.kotlin.lang.WhitespaceConfig
import it.neckar.open.kotlin.lang.toIntFloor
import kotlin.time.Duration

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
