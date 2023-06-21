package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.impl.EmptyHistoryStorage
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test

class QueryInfoHistoryStorageTest {
  @Test
  fun testCallbacks() {
    val historyStorage = EmptyHistoryStorage().withQueryMonitor()

    val onQueryCallbackDistinct = mockk<OnQueryCallback>(relaxed = true)
    val onQueryCallbackNew = mockk<OnQueryCallback>(relaxed = true)

    historyStorage.onQuery(onQueryCallbackDistinct)
    historyStorage.onQueryForNewDescriptor(onQueryCallbackNew)

    assertThat(historyStorage.query(10.0, 30.0, SamplingPeriod.Every30Days)).isEmpty()
    assertThat(historyStorage.query(10.0, 30.0, SamplingPeriod.Every30Days)).isEmpty()
    assertThat(historyStorage.query(10.0, 30.0, SamplingPeriod.Every30Days)).isEmpty()

    assertThat(historyStorage.query(10.0, 30.0, SamplingPeriod.Every24Hours)).isEmpty()
    assertThat(historyStorage.query(10.0, 30.0, SamplingPeriod.Every30Days)).isEmpty()

    verifyOrder {
      onQueryCallbackDistinct(HistoryBucketDescriptor.forStart(0.0, SamplingPeriod.Every30Days.toHistoryBucketRange()))
      onQueryCallbackDistinct(HistoryBucketDescriptor.forStart(0.0, SamplingPeriod.Every24Hours.toHistoryBucketRange()))
      onQueryCallbackDistinct(HistoryBucketDescriptor.forStart(0.0, SamplingPeriod.Every30Days.toHistoryBucketRange()))
    }

    verifyOrder {
      onQueryCallbackNew(HistoryBucketDescriptor.forStart(0.0, SamplingPeriod.Every30Days.toHistoryBucketRange()))
      onQueryCallbackNew(HistoryBucketDescriptor.forStart(0.0, SamplingPeriod.Every24Hours.toHistoryBucketRange()))
    }
  }
}
