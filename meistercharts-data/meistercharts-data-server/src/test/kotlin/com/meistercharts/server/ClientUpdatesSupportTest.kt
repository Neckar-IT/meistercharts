/*
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
package com.meistercharts.server

import assertk.*
import assertk.assertions.*
import kotlin.uuid.Uuid
import com.meistercharts.data.rest.ClientId
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.impl.MockSinusHistoryStorage
import com.meistercharts.time.TimeRange
import it.neckar.open.test.utils.DisableLogging
import it.neckar.uuid.pseudoRandom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@DisableLogging
class ClientUpdatesSupportTest {
  @Test
  fun addGetUpdates() {
    val updatesSupport: ClientUpdatesSupport = ClientUpdatesSupport.create()
    val range = HistoryBucketRange.HundredMillis
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)
    val clientId = ClientId(Uuid.pseudoRandom())

    updatesSupport.registerClient(clientId)

    updatesSupport.addUpdates(mutableListOf(mockBucket))
    val list = updatesSupport.getUpdates(clientId)

    assertThat(list.size).isEqualTo(1)
    assertThat(list).contains(HistoryUpdateInfo(mockBucket.samplingPeriod, TimeRange(mockBucket.start,mockBucket.end)))
  }

  @Test
  fun addGetUpdatesMultiple() {
    val updatesSupport: ClientUpdatesSupport = ClientUpdatesSupport.create()
    val range = HistoryBucketRange.HundredMillis
    val range2 = HistoryBucketRange.OneMinute
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1000.0, range)
    val descriptor1: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1100.0, range)
    val descriptor2: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(3000.0, range)
    val descriptor3: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1500.0, range2)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)
    val mockBucket1 = MockSinusHistoryStorage().get(descriptor1)
    val mockBucket2 = MockSinusHistoryStorage().get(descriptor2)
    val mockBucket3 = MockSinusHistoryStorage().get(descriptor3)
    val clientId = ClientId(Uuid.pseudoRandom())

    updatesSupport.registerClient(clientId)

    updatesSupport.addUpdates(mutableListOf(mockBucket, mockBucket1, mockBucket2, mockBucket3))
    val list = updatesSupport.getUpdates(clientId)

    list.forEach{ updateInfo ->
      if (updateInfo.samplingPeriod == range.samplingPeriod){
        if (false) {
          println(updateInfo.updatedTimeRanges)
        }
        assertThat(updateInfo.updatedTimeRanges.timeRanges.size).isEqualTo(2) // because update info for mockBucket and mockBucket1 should be merged
        assertThat(updateInfo.updatedTimeRanges.timeRanges).contains(TimeRange(mockBucket2.start, mockBucket2.end))
        assertThat(updateInfo.updatedTimeRanges.timeRanges).contains(TimeRange(mockBucket.start, mockBucket1.end))
      }
    }
    assertThat(list.size).isEqualTo(2) // because buckets with two different sampling periods were added

    val listNew = updatesSupport.getUpdates(clientId)
    assertThat(listNew).isEmpty()
  }

  /**
   * Hammers every map-mutating method from many coroutines on a real
   * multi-threaded dispatcher while the cleanup path runs in parallel.
   *
   * Without the lock in [ClientUpdatesSupport] this reproduces the race from
   * #2073: `addUpdates`/`addUpdateInfo` iterate `clientUpdates` while
   * `getUpdates` swaps an entry and `checkClients` removes from
   * `clientLastAccessTime`, throwing ConcurrentModificationException. With the
   * lock the run completes without exception and every registered client stays
   * queryable.
   *
   * Uses [runBlocking] on [Dispatchers.Default] on purpose: the bug is a real
   * thread race, so it needs genuine parallelism. `runTest` runs single-
   * threaded on virtual time and would never surface it.
   */
  @Test
  fun concurrentAccessIsRaceFree() {
    val updatesSupport: ClientUpdatesSupport = ClientUpdatesSupport.create()
    val range = HistoryBucketRange.HundredMillis
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1000.0, range)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)
    val updateInfo = HistoryUpdateInfo(mockBucket.samplingPeriod, TimeRange(mockBucket.start, mockBucket.end))

    val clients = (1..20).map { ClientId(Uuid.pseudoRandom()) }
    clients.forEach { updatesSupport.registerClient(it) }

    runBlocking(Dispatchers.Default) {
      coroutineScope {
        repeat(50) {
          launch { repeat(100) { updatesSupport.addUpdates(mutableListOf(mockBucket)) } }
        }
        repeat(50) {
          launch { repeat(100) { updatesSupport.addUpdateInfo(updateInfo) } }
        }
        repeat(50) {
          launch { repeat(100) { clients.forEach { client -> updatesSupport.getUpdates(client) } } }
        }
        repeat(10) {
          launch { repeat(200) { updatesSupport.checkClients() } }
        }
        repeat(10) {
          launch {
            repeat(100) {
              val churnClient = ClientId(Uuid.pseudoRandom())
              updatesSupport.registerClient(churnClient)
              updatesSupport.deleteClientFromUpdateList(churnClient.uuid)
            }
          }
        }
      }
    }

    // Completing without a ConcurrentModificationException is the assertion;
    // every originally registered client must still be queryable.
    clients.forEach { client ->
      assertThat(updatesSupport.getUpdates(client)).isNotNull()
    }

    updatesSupport.dispose()
  }

}
