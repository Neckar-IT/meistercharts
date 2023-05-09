package it.neckar.open.formatting

import it.neckar.open.time.TimeZone
import java.time.ZoneId

/**
 * Converts to a Java ZoneID
 */
fun TimeZone.toZoneId(): ZoneId {
  return ZoneId.of(this.zoneId)
}
