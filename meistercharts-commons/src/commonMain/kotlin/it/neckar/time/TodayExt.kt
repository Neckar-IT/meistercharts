package it.neckar.time

import it.neckar.open.time.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Returns the current date in the given [zone] (default: the system zone).
 *
 * Based on [it.neckar.open.time.now] so it honours virtual time in tests — prefer this
 * over `kotlinx.datetime.Clock.System.todayIn(...)`, which bypasses the test clock.
 */
fun today(zone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
  now().toLocalDateTime(zone).date
