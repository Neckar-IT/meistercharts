/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
