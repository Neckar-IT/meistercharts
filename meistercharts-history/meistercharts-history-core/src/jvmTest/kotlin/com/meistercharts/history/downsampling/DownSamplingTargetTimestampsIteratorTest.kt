package com.meistercharts.history.downsampling

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import org.junit.jupiter.api.Test

/**
 *
 */
class DownSamplingTargetTimestampsIteratorTest {
  @Test
  fun testIterator() {
    val bucketDescriptor = HistoryBucketDescriptor.forTimestamp(10_000_000.0, HistoryBucketRange.HundredMillis)

    assertThat(bucketDescriptor.start).isEqualTo(10_000_000.0)
    assertThat(bucketDescriptor.end).isEqualTo(10_000_000.0 + 100)


    val iterator = DownSamplingTargetTimestampsIterator.create(bucketDescriptor)

    assertThat(iterator.index.value).isEqualTo(0)
    assertThat(iterator.distance).all {
      isEqualTo(bucketDescriptor.bucketRange.distance)
      isEqualTo(1.0)
    }

    assertThat(iterator.slotCenter).isEqualTo(10_000_000.0 + 0.5)

    assertThat(iterator.slotStart).isEqualTo(10_000_000.0)
    assertThat(iterator.slotEnd).isEqualTo(10_000_000.0 + 1)
  }
}
