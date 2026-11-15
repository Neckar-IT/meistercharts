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
import com.meistercharts.data.rest.MeisterchartsDataEndpoints
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.impl.MockSinusHistoryStorage
import com.meistercharts.history.rest.HistoryBucketList
import com.meistercharts.history.storage.FileHistoryStorage
import com.meistercharts.time.TimeRange
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import it.neckar.rest.test.*
import it.neckar.ktor.client.getJson
import it.neckar.ktor.client.putJson
import it.neckar.ktor.status.installStatusPages
import it.neckar.open.http.Url
import it.neckar.open.test.utils.DisableLogging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test


@DisableLogging
class DataServerRoutingTest {
  val serverUrl = Url.localhostHttp(80)

  val mockStorage: FileHistoryStorage = mockk<FileHistoryStorage>(relaxed = true)
  val mockSupport: ClientUpdatesSupport = ClientUpdatesSupport.create()
  val range: HistoryBucketRange = HistoryBucketRange.HundredMillis
  val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(1.5927516E12, range)
  val mockBucket: HistoryBucket = MockSinusHistoryStorage().get(descriptor)

  @Test
  fun testGet(): Unit = testApplication {
    val slotDescriptor = slot<HistoryBucketDescriptor>()

    every { mockStorage.get(capture(slotDescriptor)) } answers {
      MockSinusHistoryStorage().get(descriptor)
    }

    application {
      historyRouting(mockStorage, mockSupport)
      installJsonSupportForTests()
    }/* HttpClient sending requests */
    val client = createClient {
      setupTestClient(jwtConfiguration = null)
    }

    val receivedBucket: HistoryBucket = client.getJson(serverUrl, MeisterchartsDataEndpoints.History.bucketUrl(descriptor))

    assertThat(receivedBucket.start).isEqualTo(descriptor.start)
    assertThat(receivedBucket.bucketRange).isEqualTo(descriptor.bucketRange)
    assertThat(receivedBucket.descriptor.end).isEqualTo(descriptor.end)

    verify {
      mockStorage.get(descriptor)
    }
  }

  @Test
  fun testQuery(): Unit = testApplication {
    every { mockStorage.query(any(), any(), any()) } returns listOf(mockBucket)

    application {
      historyRouting(mockStorage, mockSupport)
      installJsonSupportForTests()
    }/* HttpClient sending requests */
    val client = createClient {
      setupTestClient(jwtConfiguration = null)
    }

    val receivedBucketList: HistoryBucketList = client.getJson(serverUrl, MeisterchartsDataEndpoints.History.queryUrl(descriptor.bucketRange, descriptor.start, descriptor.end))

    assertThat(receivedBucketList.buckets.size).isEqualTo(1)
    assertThat(receivedBucketList.buckets[0].start).isEqualTo(descriptor.start)

    verify {
      mockStorage.query(descriptor.start, descriptor.end, descriptor.bucketRange.samplingPeriod)
    }
  }

  @Test
  fun testSaveBucket(): Unit = testApplication {

    val slotBucket = slot<HistoryBucket>()
    every { mockStorage.storeWithoutCache(capture(slotBucket), any()) } answers {
    }

    application {
      historyRouting(mockStorage, mockSupport)
      installJsonSupportForTests()
    }/* HttpClient sending requests */
    val client = createClient {
      setupTestClient(jwtConfiguration = null)
    }

    val answer = client.putJson(serverUrl, MeisterchartsDataEndpoints.History.bucketsStoreUrl(), mockBucket)
    assertThat(answer).isCreated()

    verify {
      mockStorage.storeWithoutCache(mockBucket, HistoryUpdateInfo(mockBucket.descriptor.bucketRange.samplingPeriod, TimeRange(mockBucket.start, mockBucket.end)))
    }
  }

  @Test
  fun `unknown bucket range is rejected naming the parameter and the accepted values`(): Unit = testApplication {
    val response = requestWithStatusPages("/history/buckets/EveryFortnight/1592751600000.0")

    assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    assertThat(response.errorMessage()).all {
      contains(MeisterchartsDataEndpoints.Parameters.bucketRange.value)
      contains("EveryFortnight")
      contains(HistoryBucketRange.HundredMillis.name)
    }
  }

  @Test
  fun `non numeric timestamp is rejected naming the parameter and the value`(): Unit = testApplication {
    val response = requestWithStatusPages("/history/buckets/${range.name}/yesterday")

    assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    assertThat(response.errorMessage()).all {
      contains(MeisterchartsDataEndpoints.Parameters.start.value)
      contains("yesterday")
    }
  }

  @Test
  fun `client id that is no uuid is rejected naming the parameter and the value`(): Unit = testApplication {
    val response = requestWithStatusPages("/history/updates/not-a-uuid")

    assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    assertThat(response.errorMessage()).all {
      contains(MeisterchartsDataEndpoints.Parameters.clientId.value)
      contains("not-a-uuid")
    }
  }

  /**
   * Sends [path] to the history routing with the status pages installed - the way [DataServer] serves it.
   * The client must not throw on an error response, since the error response *is* what is asserted.
   */
  private suspend fun ApplicationTestBuilder.requestWithStatusPages(path: String): HttpResponse {
    application {
      historyRouting(mockStorage, mockSupport)
      installJsonSupportForTests()
      installStatusPages(DataServer.logger)
    }

    val client = createClient {
      expectSuccess = false
    }

    return client.get(path)
  }

  /**
   * The message of the [it.neckar.problem.UnexpectedApplicationError] the status pages responded with - this is what a
   * client sees.
   */
  private suspend fun HttpResponse.errorMessage(): String {
    return Json.parseToJsonElement(bodyAsText()).jsonObject.getValue("fallbackMessage").jsonPrimitive.content
  }
}






