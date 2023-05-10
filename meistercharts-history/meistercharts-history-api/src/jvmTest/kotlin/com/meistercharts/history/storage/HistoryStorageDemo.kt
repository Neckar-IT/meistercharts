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
package com.meistercharts.history.storage

import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfigurationOnlyDecimals
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.chunk
import it.neckar.open.time.nowMillis
import it.neckar.open.formatting.dateTimeFormatWithMillis
import it.neckar.open.formatting.timeFormatWithMillis
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.unit.si.ms
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import java.io.File
import java.util.Locale

fun main() {
  HistoryStorageDemo.runDemo()
}

object HistoryStorageDemo {
  fun runDemo() {
    Locale.setDefault(Locale.GERMANY)

    println("Starting up history storage demo")

    val historyStorage = File("/tmp/storage").let { storageDir ->
      println("\tStorage dir: ${storageDir.absolutePath}")
      storageDir.mkdir()
      assertThat(storageDir).isDirectory()
      HistoryFileStorage(storageDir, GZippedHistoryStorageSerializer(JsonHistoryStorageSerializer()))
    }

    println("--------------------------------")
    println("Adding some data")

    val now = nowMillis()
    println("Now: $now --> ${dateTimeFormatWithMillis.format(now, I18nConfiguration.GermanyUTC)}")

    val descriptor = HistoryBucketDescriptor.forTimestamp(now, SamplingPeriod.EveryTenMillis)
    println("descriptor: $descriptor")
    println("\tstarts at:       ${dateTimeFormatWithMillis.format(descriptor.start, I18nConfiguration.GermanyUTC)}")
    println("\tends at:         ${dateTimeFormatWithMillis.format(descriptor.end, I18nConfiguration.GermanyUTC)}")
    println("\tEntries count:   ${descriptor.bucketRange.entriesCount}")
    println("\tDistance:        ${descriptor.bucketRange.distance} ms")
    val children = descriptor.children()
    println("\tChildren: (${children.size})")
    children
      .take(10)
      .forEach {
        println("\t\tStart at:      ${timeFormatWithMillis.format(it.start, I18nConfiguration.GermanyUTC)}")
        println("\t\tEntries Count: ${it.bucketRange.entriesCount}")
        println("\t\tDistance:      ${it.bucketRange.distance}")
        println("\t\t---------------")
      }

    val file = historyStorage.getFile(descriptor)
    println("file: ${file.absolutePath}")

    val loaded = historyStorage.get(descriptor)
    println("loaded: $loaded")

    val chunk = createDemoChunkOnlyDecimals(descriptor) { dsIndex, index ->
      (dsIndex.value + index.value).toDouble()
    }

    val bucket = HistoryBucket(descriptor, chunk)
    historyStorage.storeWithoutCache(bucket, HistoryUpdateInfo.fromChunk(chunk, bucket.descriptor.bucketRange.samplingPeriod))


    println("-------------------")
    println("-------------------")
    println("Reading the history")
    println("-------------------")


    historyStorage.query(now, now, SamplingPeriod.EveryTenMillis).let { result ->
      println("Query $now: ${result.size}\n\t$result")
    }
  }
}


fun createDemoChunkOnlyDecimals(
  descriptor: HistoryBucketDescriptor,
  dataSeriesCount: Int = 3,
  /**
   * Provides the significand value for a given data series index and timestamp index
   */
  decimalsProvider: (dataSeriesIndex: DecimalDataSeriesIndex, timestampIndex: TimestampIndex) -> Double,
): HistoryChunk {
  val timestampsCount = descriptor.bucketRange.entriesCount
  @ms val distance = descriptor.bucketRange.samplingPeriod.distance

  return historyConfigurationOnlyDecimals(dataSeriesCount) { dataSeriesIndex ->
    decimalDataSeries(
      DataSeriesId(1000 + dataSeriesIndex.value),
      TextKey("val$dataSeriesIndex", "Value $dataSeriesIndex"),
      HistoryUnit.None,
    )
  }.chunk(timestampsCount) { index ->
    addDecimalValues(descriptor.start + distance * index.value) { dataSeriesIndex: DecimalDataSeriesIndex ->
      decimalsProvider(dataSeriesIndex, index)
    }
  }
}
