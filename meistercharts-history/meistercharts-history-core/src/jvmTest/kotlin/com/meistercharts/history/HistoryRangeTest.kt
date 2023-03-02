package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.toDoubleMillis
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 *
 */
class HistoryRangeTest {
  @Test
  fun testRanges() {
    val millis = ZonedDateTime.of(2020, 7, 30, 7, 12, 24, 0, ZoneId.of("Europe/Berlin")).toDoubleMillis()

    val descriptor = HistoryBucketDescriptor.forTimestamp(millis, HistoryBucketRange.NinetyYears)

    assertThat(descriptor.start.formatUtc()).isEqualTo("1970-01-01T00:00:00.000")
    assertThat(descriptor.end.formatUtc()).isEqualTo("2058-09-16T00:00:00.000")
  }
}
