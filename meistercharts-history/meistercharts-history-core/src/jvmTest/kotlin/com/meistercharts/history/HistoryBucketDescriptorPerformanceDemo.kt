package com.meistercharts.history

import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 *
 */
class HistoryBucketDescriptorPerformanceDemo {
  @Test
  fun testPerformance() {

    for (run in 0..10) {
      measureTimeMillis {
        for (i in 0..1_000_000) {
          val descriptor = HistoryBucketDescriptor.forTimestamp(777777777777.0, HistoryBucketRange.OneMinute)
          descriptor.start
          descriptor.end
          descriptor.timeRange
        }
      }.also { println("Took $it ms") }

      //25-35 ms with old implementation
    }
  }
}
