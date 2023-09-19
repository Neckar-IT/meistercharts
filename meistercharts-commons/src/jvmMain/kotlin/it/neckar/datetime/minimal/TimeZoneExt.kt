package it.neckar.datetime.minimal

import java.time.ZoneId


/**
 * Converts to a Java ZoneID
 */
fun TimeZone.toZoneId(): ZoneId {
  return ZoneId.of(this.zoneId)
}
