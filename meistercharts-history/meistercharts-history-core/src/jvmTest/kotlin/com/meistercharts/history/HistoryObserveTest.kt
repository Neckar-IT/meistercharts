package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.impl.chunk
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

/**
 *
 */
class HistoryObserveTest {
  @Test
  fun testIt() {
    val historyStorage = InMemoryHistoryStorage()

    val updatedDescriptors = mutableListOf<HistoryBucketDescriptor>()

    historyStorage.observe { descriptor, updateInfo ->
      updatedDescriptors.add(descriptor)
    }

    assertThat(updatedDescriptors).isEmpty()

    val newChunk = historyConfiguration {
      decimalDataSeries(DataSeriesId(10), TextKey.simple("dasdf"))
    }.chunk() {
      addDecimalValues(124000.0, 7.70)
    }

    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(7.70)
    historyStorage.naturalSamplingPeriod = SamplingPeriod.EveryHour
    historyStorage.storeWithoutCache(newChunk, SamplingPeriod.EveryHour)

    assertThat(updatedDescriptors).hasSize(1)
  }
}
