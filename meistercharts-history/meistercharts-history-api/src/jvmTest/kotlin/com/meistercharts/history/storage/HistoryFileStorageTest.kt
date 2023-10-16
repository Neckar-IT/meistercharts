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

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import it.neckar.open.formatting.dateTimeFormat
import it.neckar.open.formatting.formatUtc
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.test.utils.TempFolder
import it.neckar.open.test.utils.WithTempFiles
import it.neckar.open.test.utils.doesNotExist
import it.neckar.open.time.toDoubleMillis
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class HistoryFileStorageTest {
  @Test
  fun testElementsHundredMillis() {
    val time = 1407_060_214_313.253
    assertThat(time.formatUtc()).isEqualTo("2014-08-03T10:03:34.313Z")

    val baseFolders: List<TimestampWithPrecision> = HistoryFileStorage.calculateBaseFolders(HistoryBucketDescriptor.forTimestamp(time, HistoryBucketRange.HundredMillis))
    assertThat(baseFolders.size).isEqualTo(3)

    assertThat(baseFolders[0].millis).isEqualTo(1.4E12)
    assertThat(baseFolders[0].precision).isEqualTo(100.0 * 500 * 500 * 500)

    assertThat(baseFolders[1].millis).isEqualTo(1.4070_5E12)
    assertThat(baseFolders[1].precision).isEqualTo(100.0 * 500 * 500)

    assertThat(baseFolders[2].millis).isEqualTo(1.4070_602E12)
    assertThat(baseFolders[2].precision).isEqualTo(100.0 * 500) //100ms * 500 files
  }

  @Test
  fun testElementsOneMinute() {
    val time = 1407_060_214_313.253
    assertThat(time.formatUtc()).isEqualTo("2014-08-03T10:03:34.313Z")

    val baseFolders: List<TimestampWithPrecision> = HistoryFileStorage.calculateBaseFolders(HistoryBucketDescriptor.forTimestamp(time, HistoryBucketRange.OneMinute))
    assertThat(baseFolders.size).isEqualTo(2)

    assertThat(baseFolders[0].millis).isEqualTo(1.395E12)
    assertThat(baseFolders[0].precision).isEqualTo(60 * 1000.0 * 500 * 500)

    assertThat(baseFolders[1].millis).isEqualTo(1.40706E12)
    assertThat(baseFolders[1].precision).isEqualTo(60 * 1000.0 * 500) //1 minute * 500 files
  }

  @Test
  fun testElementsTenMinute() {
    val time = 1407_060_214_313.253
    assertThat(time.formatUtc()).isEqualTo("2014-08-03T10:03:34.313Z")

    val baseFolders: List<TimestampWithPrecision> = HistoryFileStorage.calculateBaseFolders(HistoryBucketDescriptor.forTimestamp(time, HistoryBucketRange.TenMinutes))
    assertThat(baseFolders.size).isEqualTo(2)

    assertThat(baseFolders[0].millis).isEqualTo(1.35E12)
    assertThat(baseFolders[0].precision).isEqualTo(10 * 60 * 1000.0 * 500 * 500)

    assertThat(baseFolders[1].millis).isEqualTo(1.407E12)
    assertThat(baseFolders[1].precision).isEqualTo(10 * 60 * 1000.0 * 500) //10 minutes * 500 files
  }

  @Test
  fun testElementsOneHour() {
    val time = 1407_060_214_313.253
    assertThat(time.formatUtc()).isEqualTo("2014-08-03T10:03:34.313Z")

    val baseFolders: List<TimestampWithPrecision> = HistoryFileStorage.calculateBaseFolders(HistoryBucketDescriptor.forTimestamp(time, HistoryBucketRange.OneHour))
    assertThat(baseFolders.size).isEqualTo(2)

    assertThat(baseFolders[0].millis).isEqualTo(9.0E11)
    assertThat(baseFolders[0].precision).isEqualTo(60 * 60 * 1000.0 * 500 * 500)

    assertThat(baseFolders[1].millis).isEqualTo(1.4058E12)
    assertThat(baseFolders[1].precision).isEqualTo(60 * 60 * 1000.0 * 500) //1 hour * 500 files
  }

  @Test
  fun testElementsSixHours() {
    val time = 1407_060_214_313.253
    assertThat(time.formatUtc()).isEqualTo("2014-08-03T10:03:34.313Z")

    val baseFolders: List<TimestampWithPrecision> = HistoryFileStorage.calculateBaseFolders(HistoryBucketDescriptor.forTimestamp(time, HistoryBucketRange.SixHours))
    assertThat(baseFolders.size).isEqualTo(1)

    assertThat(baseFolders[0].millis).isEqualTo(1.404E12)
    assertThat(baseFolders[0].precision).isEqualTo(6 * 60 * 60 * 1000.0 * 500) //6 hours * 500 files
  }

  @Test
  fun testElementsOneDay() {
    val time = 1407_060_214_313.253
    assertThat(time.formatUtc()).isEqualTo("2014-08-03T10:03:34.313Z")

    val baseFolders: List<TimestampWithPrecision> = HistoryFileStorage.calculateBaseFolders(HistoryBucketDescriptor.forTimestamp(time, HistoryBucketRange.OneDay))
    assertThat(baseFolders.size).isEqualTo(1)

    assertThat(baseFolders[0].millis).isEqualTo(1.3824E12)
    assertThat(baseFolders[0].precision).isEqualTo(24 * 60 * 60 * 1000.0 * 500) //24 hours * 500 files
  }

  @Test
  fun testElements30Days() {
    val time = 1407_060_214_313.253
    assertThat(time.formatUtc()).isEqualTo("2014-08-03T10:03:34.313Z")

    val baseFolders: List<TimestampWithPrecision> = HistoryFileStorage.calculateBaseFolders(HistoryBucketDescriptor.forTimestamp(time, HistoryBucketRange.ThirtyDays))
    assertThat(baseFolders.size).isEqualTo(1)

    assertThat(baseFolders[0].millis.formatUtc()).isEqualTo(1.296E12.formatUtc())
    assertThat(baseFolders[0].precision).isEqualTo(30 * 24 * 60 * 60 * 1000.0 * 500) //30 days * 500 files
  }

  @Test
  internal fun testCalculateBaseFolderStart() {
    assertThat(HistoryFileStorage.calculateBaseFolderValue(HistoryBucketDescriptor.forTimestamp(100000000.0, HistoryBucketRange.HundredMillis)).millis).isEqualTo(100000000.0)
    assertThat(HistoryFileStorage.calculateBaseFolderValue(HistoryBucketDescriptor.forTimestamp(100000000.0 + HistoryBucketRange.HundredMillis.duration, HistoryBucketRange.HundredMillis)).millis).isEqualTo(100000000.0)
    assertThat(HistoryFileStorage.calculateBaseFolderValue(HistoryBucketDescriptor.forTimestamp(100000000.0 - HistoryBucketRange.HundredMillis.duration, HistoryBucketRange.HundredMillis)).millis).isEqualTo(99950000.0)
    assertThat(HistoryFileStorage.calculateBaseFolderValue(HistoryBucketDescriptor.forTimestamp(100000000.0 + HistoryBucketRange.HundredMillis.duration * 99, HistoryBucketRange.HundredMillis)).millis).isEqualTo(100000000.0)
    assertThat(HistoryFileStorage.calculateBaseFolderValue(HistoryBucketDescriptor.forTimestamp(100000000.0 + HistoryBucketRange.HundredMillis.duration * 100, HistoryBucketRange.HundredMillis)).millis).isEqualTo(1.0E8)
    assertThat(HistoryFileStorage.calculateBaseFolderValue(HistoryBucketDescriptor.forTimestamp(100000000.0 + HistoryBucketRange.HundredMillis.duration * 499, HistoryBucketRange.HundredMillis)).millis).isEqualTo(1.0E8)
    assertThat(HistoryFileStorage.calculateBaseFolderValue(HistoryBucketDescriptor.forTimestamp(100000000.0 + HistoryBucketRange.HundredMillis.duration * 500, HistoryBucketRange.HundredMillis)).millis).isEqualTo(1.0005E8)
  }

  @WithTempFiles
  @Test
  internal fun testFileNamesRealistic(@TempFolder dir: File) {
    val historyStorage = HistoryFileStorage(dir, JsonHistoryStorageSerializer())

    val localDate = LocalDate.of(2020, 2, 19)
    val localTime = LocalTime.of(17, 30, 32)
    val instant = LocalDateTime.of(localDate, localTime).toInstant(ZoneOffset.UTC)

    historyStorage.getFileName(HistoryBucketDescriptor.forTimestamp(instant.toDoubleMillis(), HistoryBucketRange.HundredMillis)).let { fileName ->
      assertThat(fileName).isEqualTo("HundredMillis/1575000000000/1582125000000/1582133400000/1582133432000")

      val parts: List<String> = fileName.split("/")
      assertThat(parts.size).isEqualTo(5)

      assertThat(dateTimeFormat.format(parts[1].toLong().toDouble(), I18nConfiguration.GermanyUTC)).isEqualTo("29.11.2019 04:00:00")
      assertThat(dateTimeFormat.format(parts[2].toLong().toDouble(), I18nConfiguration.GermanyUTC)).isEqualTo("19.02.2020 15:10:00")
      assertThat(dateTimeFormat.format(parts[3].toLong().toDouble(), I18nConfiguration.GermanyUTC)).isEqualTo("19.02.2020 17:30:00")

      assertThat(dateTimeFormat.format(parts[4].toLong().toDouble(), I18nConfiguration.GermanyUTC)).isEqualTo("19.02.2020 17:30:32")
    }
  }

  @WithTempFiles
  @Test
  internal fun testCreateFileName(@TempFolder dir: File) {
    val historyStorage = HistoryFileStorage(dir, JsonHistoryStorageSerializer())

    assertThat(historyStorage.getFileName(HistoryBucketDescriptor.forTimestamp(100000000.0, HistoryBucketRange.HundredMillis))).isEqualTo("HundredMillis/0/100000000/100000000/100000000")
    assertThat(historyStorage.getFileName(HistoryBucketDescriptor.forTimestamp(100000000.0 + HistoryBucketRange.HundredMillis.duration, HistoryBucketRange.HundredMillis))).isEqualTo("HundredMillis/0/100000000/100000000/100000100")
    assertThat(historyStorage.getFileName(HistoryBucketDescriptor.forTimestamp(100000000.0 - HistoryBucketRange.HundredMillis.duration, HistoryBucketRange.HundredMillis))).isEqualTo("HundredMillis/0/75000000/99950000/99999900")
    assertThat(historyStorage.getFileName(HistoryBucketDescriptor.forTimestamp(100000000.0 + HistoryBucketRange.HundredMillis.duration * 99, HistoryBucketRange.HundredMillis))).isEqualTo("HundredMillis/0/100000000/100000000/100009900")
    assertThat(historyStorage.getFileName(HistoryBucketDescriptor.forTimestamp(100000000.0 + HistoryBucketRange.HundredMillis.duration * 100, HistoryBucketRange.HundredMillis))).isEqualTo("HundredMillis/0/100000000/100000000/100010000")
  }

  @Disabled
  @WithTempFiles
  @Test
  internal fun testIt(@TempFolder dir: File) {
    val historyStorage = HistoryFileStorage(dir, JsonHistoryStorageSerializer())

    val descriptor = HistoryBucketDescriptor.forTimestamp(100000000.0, HistoryBucketRange.HundredMillis)

    val file = historyStorage.getFile(descriptor)
    assertThat(file).doesNotExist()

    val historyBucket = historyStorage.get(descriptor)
    assertThat(historyBucket).isNotNull()
  }

  //@WithTempFiles
  //@Test
  //internal fun testSerialize(@TempFolder dir: File) {
  //  val historyStorage = HistoryFileStorage(dir, JsonHistoryStorageSerializer())
  //  val descriptor = HistoryBucketDescriptor.forMillis(100000000.0, HistoryBucketRange.HundredMillis)
  //
  //
  //  val dataSeriesCount = 20
  //  val bucket = HistoryBucket(descriptor, createDemoChunk(descriptor, dataSeriesCount) { dataSeriesIndex, timestampIndex ->
  //    //Random.nextInt(-10_000, 10_000)
  //    Random.nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE)
  //    //dataSeriesIndex + timestampIndex
  //  })
  //
  //  assertThat(bucket.chunk.timeStampsCount).isEqualTo(100)
  //  assertThat(bucket.chunk.dataSeriesCount).isEqualTo(dataSeriesCount)
  //
  //  val valuesCount = bucket.chunk.timeStampsCount * bucket.chunk.dataSeriesCount
  //  println("valuesCount = ${valuesCount}")
  //
  //  //300 values
  //
  //
  //  val serializer = HistoryBucket.serializer()
  //
  //  val json = Json.indented.toJson(serializer, bucket)
  //  val jsonLength = json.toString().length
  //  println("bytesPerDataPoint = ${jsonLength.toDouble() / valuesCount}")
  //  println("Lenght: $jsonLength")
  //  println("Result: $json")
  //
  //
  //  println("-----------")
  //  val out = ByteArrayOutputStream()
  //
  //  GZIPOutputStream(out).use {
  //    val encodeToByteArray = json.toString().encodeToByteArray()
  //    println(encodeToByteArray.size)
  //    it.write(encodeToByteArray)
  //    it.flush()
  //  }
  //
  //  println("Zipped length: ${out.toByteArray().size}")
  //  println("bytesPerDataPoint = ${out.toByteArray().size.toDouble() / valuesCount}")
  //
  //  GZIPInputStream(ByteArrayInputStream(out.toByteArray())).use {
  //    println("Read: ")
  //    println(it.readBytes().decodeToString().length)
  //  }
  //}
}
