package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.getDateInMillis
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 */
class HistoryBucketTest {
  val millis = 1312003123123.0
  val instant = Instant.ofEpochMilli(millis.toLong())
  val dateTime = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)

  @Test
  fun testIt() {
    assertThat(dateTime).isEqualTo(OffsetDateTime.of(2011, 7, 30, 5, 18, 43, 123 * 1_000_000, ZoneOffset.UTC))
    assertThat(dateTime.year).isEqualTo(2011)
    assertThat(dateTime.dayOfMonth).isEqualTo(30)
  }

  @Test
  fun testItYearDescriptor() {
    val date = OffsetDateTime.of(2020, 12, 24, 5, 18, 43, 123 * 1_000_000, ZoneOffset.UTC)
    assertThat(date.year).isEqualTo(2020)
    assertThat(date.dayOfMonth).isEqualTo(24)

    val descriptor = HistoryBucketDescriptor.forTimestamp(date.toInstant().toEpochMilli().toDouble(), SamplingPeriod.Every360Days)

    assertThat(descriptor.start.formatUtc()).isEqualTo("1970-01-01T00:00:00.000")
    assertThat(descriptor.center.formatUtc()).isEqualTo("2324-11-01T00:00:00.000")
    assertThat(descriptor.end.formatUtc()).isEqualTo("2679-09-01T00:00:00.000")
  }

  @Test
  internal fun testDescriptor() {
    val millis = 1312003123123.0
    val descriptor = HistoryBucketDescriptor.forTimestamp(millis, HistoryBucketRange.OneDay)

    assertThat(descriptor.bucketRange).isSameAs(HistoryBucketRange.OneDay)

    assertThat(descriptor.start).isEqualTo(OffsetDateTime.of(2011, 7, 30, 0, 0, 0, 0, ZoneOffset.UTC).getDateInMillis)
    assertThat(descriptor.end).isEqualTo(OffsetDateTime.of(2011, 7, 31, 0, 0, 0, 0, ZoneOffset.UTC).getDateInMillis)
  }
}
