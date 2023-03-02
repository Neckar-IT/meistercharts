package com.meistercharts.history.storage

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.impl.MockSinusHistoryStorage
import it.neckar.open.test.utils.TempFolder
import it.neckar.open.test.utils.WithTempFiles
import it.neckar.open.test.utils.doesNotExist
import it.neckar.open.test.utils.getAllChildrenNames
import kotlinx.serialization.json.Json


import org.junit.jupiter.api.*
import java.io.File

@WithTempFiles
class FileHistoryStorageTest {

  @Test
  fun testGetFile(@TempFolder baseDirTemp: File) {
    val json: Json = Json {
      HistoryConfiguration
    }
    val storage = FileHistoryStorage(baseDirTemp,json)
    val range = HistoryBucketRange.HundredMillis
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
    val file = storage.getFile(descriptor)
    assertThat(file.name).contains(".json")
    assertThat(file).doesNotExist()
  }

  @Test
  fun testStoreWithoutCacheWithSamplingPeriod(@TempFolder baseDirTemp: File) {
    val json: Json = Json {
      HistoryConfiguration
    }
    val storage = FileHistoryStorage(baseDirTemp,json)
    val range = HistoryBucketRange.OneMinute
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)
    storage.storeWithoutCache(mockBucket.chunk,mockBucket.samplingPeriod)
    assertThat(baseDirTemp.getAllChildrenNames()).contains("0.json")
  }

  @Test
  fun testStoreWithoutCacheWithUpdateInfo(@TempFolder newTempDirectory: File){
    val json: Json = Json {
      HistoryConfiguration
    }
    val historyStorage = FileHistoryStorage(newTempDirectory,json)
    val range = HistoryBucketRange.HundredMillis
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)
    val updateInfo = HistoryUpdateInfo(SamplingPeriod.EveryHundredMillis, TimeRange(1.5927516E12, 1.5927518E12))
    historyStorage.storeWithoutCache(mockBucket, updateInfo)
    assertThat(newTempDirectory.getAllChildrenNames()).contains("0.json")
  }


  @Test
  fun testHistoryStorageLoad(@TempFolder newTempDirectory: File){
    val json: Json = Json {
      HistoryConfiguration
    }
    val historyStorage = FileHistoryStorage(newTempDirectory,json)
    val range = HistoryBucketRange.HundredMillis
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)
    val updateInfo = HistoryUpdateInfo(SamplingPeriod.EveryHundredMillis, TimeRange(1.5927516E12, 1.5927517E12))
    historyStorage.storeWithoutCache(mockBucket, updateInfo)
    assertThat(newTempDirectory.getAllChildrenNames()).contains("0.json")
    val bucket = historyStorage.get(descriptor)

    if (bucket != null) {
      assertThat(mockBucket.chunk.values.decimalHistoryValues.values).equals(bucket.chunk.values.decimalHistoryValues.values)
      assertThat(mockBucket.chunk.values.enumHistoryValues.values).equals(bucket.chunk.values.enumHistoryValues.values)
    }
    assertThat(bucket).isNotNull()
    assertThat(bucket?.start).isEqualTo(descriptor.start)
  }

  @Test
  fun testHistoryStorageStoreAndLoad(@TempFolder newTempDirectory: File) {
    val json: Json = Json {
      HistoryConfiguration
    }
    val historyStorage = FileHistoryStorage(newTempDirectory,json)
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(100000.0, SamplingPeriod.EveryTenMillis)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)
    val mockChunk = mockBucket.chunk
    historyStorage.storeWithoutCache(mockChunk, mockBucket.samplingPeriod)
    val incomingBucket = historyStorage.get(descriptor)
    val incomingChunk = incomingBucket?.chunk

    if (incomingChunk != null) {
      assertThat(incomingChunk.decimalDataSeriesCount).isEqualTo(3)
      assertThat(incomingChunk.enumDataSeriesCount).isEqualTo(0)

      assertThat(incomingChunk.timeStampsCount).isEqualTo(500)

      assertThat(incomingChunk.values.decimalHistoryValues.values.size).isEqualTo(500 * 3)
      assertThat(incomingChunk.values.enumHistoryValues.values.size).isEqualTo(0)
    }
  }
  @Test
  fun testGetUpdates(@TempFolder newTempDirectory: File){
    val json: Json = Json {
      HistoryConfiguration
    }
    val historyStorage = FileHistoryStorage(newTempDirectory,json)
    val range = HistoryBucketRange.HundredMillis
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)
    val updateInfo = HistoryUpdateInfo(SamplingPeriod.EveryHundredMillis, TimeRange(1.5927516E12, 1.5927517E12))
    historyStorage.storeWithoutCache(mockBucket, updateInfo)

    val updates = historyStorage.getUpdates()

    assertThat(updates).isNotEmpty()
    assertThat(updates.size).isEqualTo(1)
    assertThat(updates[0].start).isEqualTo(mockBucket.start)


  }




}
