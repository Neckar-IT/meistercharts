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
@file:Suppress("RETURN_VALUE_NOT_USED") // Test scope

package com.meistercharts.data.client

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.impl.MockSinusHistoryStorage
import io.ktor.http.*
import it.neckar.rest.test.*
import it.neckar.ktor.client.createHttpClient
import it.neckar.open.http.Url
import it.neckar.open.time.toMillis
import kotlinx.coroutines.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import java.time.OffsetDateTime
import java.time.ZoneOffset

/** contains a demo for the data client in the form of tests, which are disabled by default,
 * because the Data Server must run for the tests to be successful.*/
class DataClientDemoTest {
  val serverUrl = Url.localhostHttp

  @Test
  @Disabled
  fun testPost() {

    val range = HistoryBucketRange.HundredMillis
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
    val mockBucket = MockSinusHistoryStorage().get(descriptor)
    val client = DataClient(createHttpClient("test") {
      setupTestClient(jwtConfiguration = null)
    }, serverUrl)
    runBlocking {
      val answer = client.put(mockBucket)
      assertThat(answer).isCreated()
    }
  }

  @Test
  @Disabled
  fun testGet() {

    val range = HistoryBucketRange.HundredMillis
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
    val client = DataClient(createHttpClient("test") {
      setupTestClient(jwtConfiguration = null)
    }, serverUrl)

    runBlocking {
      val bucket = client.get(descriptor.bucketRange, descriptor.start)
      if (bucket != null) {
        assertThat(bucket.start).isEqualTo(descriptor.start)
      }
    }
  }

  @Test
  @Disabled
  fun testQuery() {

    val range = HistoryBucketRange.HundredMillis
    val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
    val client = DataClient(createHttpClient("test") {
      setupTestClient(jwtConfiguration = null)
    }, serverUrl)

    runBlocking {
      val buckets = client.query(descriptor.bucketRange, descriptor.start, descriptor.end)
      assertThat(buckets.buckets[0].start).isEqualTo(descriptor.start)
    }
  }


  @Test
  @Disabled
  fun testPostFromNowBucketOneMin() {
    val offsetNow = OffsetDateTime.now(ZoneOffset.UTC)
    val range = HistoryBucketRange.OneMinute

    val client = DataClient(createHttpClient("test") {
      setupTestClient(jwtConfiguration = null)
    }, serverUrl)

    for (h in 0..23) {
      for (m in 0..59) {
        val offsetMin = OffsetDateTime.of(offsetNow.year, offsetNow.monthValue, offsetNow.dayOfMonth, h, m, 0, 0, ZoneOffset.UTC).toMillis()
        val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(offsetMin, range)
        val mockBucket = MockSinusHistoryStorage().get(descriptor)
        assertThat(mockBucket.chunk.values.decimalHistoryValues.values.size).isEqualTo(range.entriesCount * mockBucket.chunk.decimalDataSeriesCount.value)
        runBlocking {
          client.put(mockBucket)
          logger.debug("Adding bucket for $descriptor")
        }
      }
    }
  }

  @Test
  @Disabled
  fun testPostFromNowBucketOneDayLater() {
    val offsetNow = OffsetDateTime.now(ZoneOffset.UTC)
    val range = HistoryBucketRange.OneMinute

    val client = DataClient(createHttpClient("test") {
      setupTestClient(jwtConfiguration = null)
    }, serverUrl)

    for (h in 0..23) {
      for (m in 0..59) {
        val offsetMin = OffsetDateTime.of(offsetNow.year, offsetNow.monthValue, offsetNow.dayOfMonth + 1, h, m, 0, 0, ZoneOffset.UTC).toMillis()
        val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(offsetMin, range)
        val mockBucket = MockSinusHistoryStorage().get(descriptor)
        assertThat(mockBucket.chunk.values.decimalHistoryValues.values.size).isEqualTo(range.entriesCount * mockBucket.chunk.decimalDataSeriesCount.value)
        runBlocking {
          client.put(mockBucket)
          logger.debug("Adding bucket for $descriptor")
        }
      }
    }
  }

  @Test
  @Disabled
  fun testPostFromNowBucketTwoDaysLater() {
    val offsetNow = OffsetDateTime.now(ZoneOffset.UTC)
    val range = HistoryBucketRange.OneMinute

    val client = DataClient(createHttpClient("test") {
      setupTestClient(jwtConfiguration = null)
    }, serverUrl)

    for (h in 0..23) {
      for (m in 0..59) {
        val offsetMin = OffsetDateTime.of(offsetNow.year, offsetNow.monthValue, offsetNow.dayOfMonth + 2, h, m, 0, 0, ZoneOffset.UTC).toMillis()
        val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(offsetMin, range)
        val mockBucket = MockSinusHistoryStorage().get(descriptor)
        assertThat(mockBucket.chunk.values.decimalHistoryValues.values.size).isEqualTo(range.entriesCount * mockBucket.chunk.decimalDataSeriesCount.value)
        runBlocking {
          client.put(mockBucket)
          logger.debug("Adding bucket for $descriptor")
        }
      }
    }
  }


  @Test
  @Disabled
  fun testPostFromNowBucketThreeDaysLater() {
    val offsetNow = OffsetDateTime.now(ZoneOffset.UTC)
    val range = HistoryBucketRange.OneMinute

    val client = DataClient(createHttpClient("test") {
      setupTestClient(jwtConfiguration = null)
    }, serverUrl)

    for (h in 0..23) {
      for (m in 0..59) {
        val offsetMin = OffsetDateTime.of(offsetNow.year, offsetNow.monthValue, offsetNow.dayOfMonth + 3, h, m, 0, 0, ZoneOffset.UTC).toMillis()
        val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(offsetMin, range)
        val mockBucket = MockSinusHistoryStorage().get(descriptor)
        assertThat(mockBucket.chunk.values.decimalHistoryValues.values.size).isEqualTo(range.entriesCount * mockBucket.chunk.decimalDataSeriesCount.value)
        runBlocking {
          client.put(mockBucket)
          logger.debug("Adding bucket for $descriptor")
        }
      }
    }
  }


  companion object {
    private val logger: Logger by it.neckar.logging.LoggerDelegate()
  }
}





