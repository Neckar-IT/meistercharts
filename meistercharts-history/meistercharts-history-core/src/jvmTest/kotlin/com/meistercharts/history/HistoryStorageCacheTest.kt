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
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.chunk
import it.neckar.open.time.VirtualNowProvider
import it.neckar.open.javafx.JavaFxTimer
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.i18n.TextKey
import it.neckar.open.javafx.test.JavaFxTest
import it.neckar.open.test.utils.VirtualTime
import it.neckar.open.time.JvmTimerSupport
import it.neckar.open.time.jvmTimerSupport
import it.neckar.open.unit.si.ms
import org.awaitility.Awaitility
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

/**
 *
 */
@JavaFxTest
@VirtualTime(0.0)
class HistoryStorageCacheTest {
  private lateinit var disposeSupport: DisposeSupport

  @BeforeEach
  fun setUp() {
    disposeSupport = DisposeSupport()

    jvmTimerSupport = object : JvmTimerSupport {
      override fun delay(delay: Duration, callback: () -> Unit): Disposable {
        return JavaFxTimer.delay(delay, callback)
      }

      override fun repeat(delay: Duration, callback: () -> Unit): Disposable {
        return JavaFxTimer.repeat(delay, callback)
      }
    }
  }

  @Test
  fun tearDown() {
    disposeSupport.dispose()
  }

  val baseTimeStamp: @ms Double = 700.0
  val samplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis

  val historyConfiguration: HistoryConfiguration = historyConfiguration {
    decimalDataSeries(DataSeriesId(17), TextKey.simple("asdf1"))
    decimalDataSeries(DataSeriesId(18), TextKey.simple("asdf2"))
    decimalDataSeries(DataSeriesId(19), TextKey.simple("asdf3"))
  }

  fun createChunk(baseTimeStamp: Double = this@HistoryStorageCacheTest.baseTimeStamp): HistoryChunk {
    val chunk = historyConfiguration.chunk(5) { timestampIndex ->
      val timestamp = baseTimeStamp + timestampIndex.value
      addDecimalValues(timestamp) { dataSeriesIndex: DecimalDataSeriesIndex ->
        (dataSeriesIndex.value * 1000.0 + timestamp)
      }
    }

    return chunk
  }

  @Test
  fun testCreateSimple() {
    assertThat(createChunk().values.decimalValuesAsMatrixString()).isEqualTo(
      """
        700.0, 1700.0, 2700.0
        701.0, 1701.0, 2701.0
        702.0, 1702.0, 2702.0
        703.0, 1703.0, 2703.0
        704.0, 1704.0, 2704.0
    """.trimIndent()
    )
  }

  @Test
  fun testIt() {
    val history = InMemoryHistoryStorage()

    assertThat(history.get(HistoryBucketDescriptor.forTimestamp(baseTimeStamp, samplingPeriod))).isNull()
    history.storeWithoutCache(createChunk(baseTimeStamp), samplingPeriod)
    assertThat(history.get(HistoryBucketDescriptor.forTimestamp(baseTimeStamp, samplingPeriod))).isNotNull()
  }

  @VirtualTime(0.0)
  @Test
  @Timeout(40_000)
  fun testWithCache(nowProvider: VirtualNowProvider) {
    val history = InMemoryHistoryStorage().also {
      disposeSupport.onDispose(it)
    }

    val historyStorageCache = HistoryStorageCache(history)

    val chunk: HistoryChunk = createChunk(baseTimeStamp)

    assertThat(history.get(HistoryBucketDescriptor.forTimestamp(baseTimeStamp, samplingPeriod))).isNull()

    //Schedule
    assertThat(historyStorageCache.scheduledChunk).isNull()
    historyStorageCache.scheduleForStore(chunk, SamplingPeriod.EveryHundredMillis)
    assertThat(historyStorageCache.scheduledChunk).isNotNull()

    //*NOT* yet added!
    assertThat(history.get(HistoryBucketDescriptor.forTimestamp(baseTimeStamp, samplingPeriod))).isNull()

    nowProvider.add(10000.0)

    try {
      Awaitility.await()
        .pollInSameThread()
        .atMost(30, TimeUnit.SECONDS).until {
          history.get(HistoryBucketDescriptor.forTimestamp(baseTimeStamp, samplingPeriod)) != null
        }
    } catch (e: Throwable) {
      throw e
    }
  }

  @VirtualTime(0.0)
  @Timeout(40_000)
  @Test
  fun testMultipleEntries(nowProvider: VirtualNowProvider) {
    var storeCallCount = 0

    val history = object : InMemoryHistoryStorage() {
      override fun storeWithoutCache(bucket: HistoryBucket, updateInfo: HistoryUpdateInfo) {
        storeCallCount++
        super.storeWithoutCache(bucket, updateInfo)
      }
    }.also {
      disposeSupport.onDispose(it)
    }

    val historyStorageCache = HistoryStorageCache(history)
    val chunk0: HistoryChunk = createChunk(700.0)
    val chunk1: HistoryChunk = createChunk(800.0)

    assertThat(chunk0.start).isEqualTo(700.0)
    assertThat(chunk1.start).isEqualTo(800.0)

    val descriptorAt700 = HistoryBucketDescriptor.forTimestamp(700.0, samplingPeriod)
    val descriptorAt800 = HistoryBucketDescriptor.forTimestamp(800.0, samplingPeriod)

    assertThat(history.get(descriptorAt700)).isNull()
    assertThat(history.get(descriptorAt800)).isNull()

    //Schedule
    JavaFxTimer.runAndWait {
      historyStorageCache.scheduleForStore(chunk0, SamplingPeriod.EveryHundredMillis)
      historyStorageCache.scheduleForStore(chunk1, SamplingPeriod.EveryHundredMillis)
    }

    //*NOT* yet added!
    assertThat(history.get(descriptorAt700)).isNull()
    assertThat(history.get(descriptorAt800)).isNull()

    assertThat(storeCallCount).isEqualTo(0)

    nowProvider.add(1000.0)

    Awaitility.await()
      .pollInSameThread()
      .atMost(30, TimeUnit.SECONDS).until {
        history.get(descriptorAt700) != null
      }

    assertThat(storeCallCount).isEqualTo(1)

    val readBucket0 = history.get(descriptorAt700)
    val readBucket1 = history.get(descriptorAt800)

    assertThat(readBucket0).isNotNull()
    assertThat(readBucket1).isNotNull()

    require(readBucket0 != null)
    require(readBucket1 != null)

    assertThat(readBucket0.chunk.timeStamps).containsAll(700.0, 701.0, 702.0, 703.0, 704.0)
    assertThat(readBucket1.chunk.timeStamps).containsAll(800.0, 801.0, 802.0, 803.0, 804.0)
    assertThat(readBucket1.chunk.timeStamps).containsAll(*chunk0.timeStamps)
    assertThat(readBucket1.chunk.timeStamps).containsAll(*chunk1.timeStamps)

    assertThat(readBucket0.chunk.values.decimalValuesAsMatrixString()).isEqualTo(
      """
      700.0, 1700.0, 2700.0
      701.0, 1701.0, 2701.0
      702.0, 1702.0, 2702.0
      703.0, 1703.0, 2703.0
      704.0, 1704.0, 2704.0
      800.0, 1800.0, 2800.0
      801.0, 1801.0, 2801.0
      802.0, 1802.0, 2802.0
      803.0, 1803.0, 2803.0
      804.0, 1804.0, 2804.0
    """.trimIndent()
    )

    assertThat(readBucket1).isSameAs(readBucket0)
  }

  @Test
  @Timeout(40_000)
  fun testClear() {
    var storeCallCount = 0

    val history = object : InMemoryHistoryStorage() {
      override fun storeWithoutCache(bucket: HistoryBucket, updateInfo: HistoryUpdateInfo) {
        storeCallCount++
        super.storeWithoutCache(bucket, updateInfo)
      }
    }.also {
      disposeSupport.onDispose(it)
    }

    val historyStorageCache = HistoryStorageCache(history)
    val chunk: HistoryChunk = createChunk(700.0)

    val descriptorAt700 = HistoryBucketDescriptor.forTimestamp(700.0, samplingPeriod)

    assertThat(history.get(descriptorAt700)).isNull()

    //Schedule
    historyStorageCache.scheduleForStore(chunk, SamplingPeriod.EveryHundredMillis)

    //*NOT* yet added!
    assertThat(history.get(descriptorAt700)).isNull()

    assertThat(storeCallCount).isEqualTo(0)

    //cancel scheduling
    historyStorageCache.clear()

    //wait at least twice the window-duration
    Awaitility.await()
      .pollInSameThread()
      .atMost(30, TimeUnit.SECONDS).atLeast(historyStorageCache.window.inWholeMilliseconds, TimeUnit.MILLISECONDS)

    //nothing should have been stored
    assertThat(storeCallCount).isEqualTo(0)
  }
}
