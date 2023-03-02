package com.meistercharts.algorithms.time

import com.meistercharts.algorithms.time.TimeZoneOffsetProvider
import it.neckar.open.time.TimeZone
import java.time.Instant
import java.time.ZoneId

/**
 * Computes the 'real' time-zone offset for a given timestamp and a given time-zone
 */
actual class DefaultTimeZoneOffsetProvider : TimeZoneOffsetProvider {
  override fun timeZoneOffset(timestamp: Double, timeZone: TimeZone): Double {
    val zoneId = ZoneId.of(timeZone.zoneId)
    val instant = Instant.ofEpochMilli(timestamp.toLong())
    val zoneOffset = zoneId.rules.getOffset(instant)
    return zoneOffset.totalSeconds * 1000.0
  }

}
