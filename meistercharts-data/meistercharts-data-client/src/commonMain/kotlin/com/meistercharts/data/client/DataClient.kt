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
package com.meistercharts.data.client


import com.meistercharts.data.rest.ClientId
import com.meistercharts.data.rest.HistoryBucketRestResponse
import com.meistercharts.data.rest.MeisterchartsDataEndpoints
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.rest.HistoryBucketList
import io.ktor.client.*
import io.ktor.client.statement.*
import it.neckar.ktor.client.getJson
import it.neckar.ktor.client.postJson
import it.neckar.ktor.client.putJson
import it.neckar.open.kotlin.lang.random
import it.neckar.open.unit.si.ms
import kotlin.uuid.Uuid

/**
 * Provides access to the history data - for a client
 *
 */
class DataClient(
  val client: HttpClient,
  val serverUrl: it.neckar.open.http.Url.Absolute,
  val clientId: ClientId = ClientId(Uuid.fromByteArray(random.nextBytes(16))),
) {

  suspend fun get(bucketRange: HistoryBucketRange, start: @ms Double): HistoryBucket? {
    val received: HistoryBucketRestResponse = client.getJson(serverUrl + MeisterchartsDataEndpoints.History.bucketUrl(bucketRange, start))
    return received.toHistoryBucket()
  }

  suspend fun get(descriptor: HistoryBucketDescriptor): HistoryBucket? {
    val received: HistoryBucketRestResponse = client.getJson(serverUrl + MeisterchartsDataEndpoints.History.bucketUrl(descriptor))
    return received.toHistoryBucket()
  }

  suspend fun query(bucketRange: HistoryBucketRange, from: @ms Double, to: @ms Double): HistoryBucketList {
    return client.getJson(serverUrl + MeisterchartsDataEndpoints.History.queryUrl(bucketRange, from, to))
  }

  suspend fun put(bucket: HistoryBucket): HttpResponse {
    return client.putJson(serverUrl + MeisterchartsDataEndpoints.History.bucketsStoreUrl(), bucket)
  }

  suspend fun getConfig(): HistoryConfiguration {
    return client.getJson(serverUrl + MeisterchartsDataEndpoints.configUrl())
  }

  @IgnorableReturnValue
  suspend fun registerAtServer(): HttpResponse {
    return client.postJson(serverUrl + MeisterchartsDataEndpoints.History.Updates.registerUrl(clientId), clientId)
  }

  @IgnorableReturnValue
  suspend fun unregisterAtServer(): HttpResponse {
    return client.postJson(serverUrl + MeisterchartsDataEndpoints.History.Updates.unregisterUrl(clientId), clientId)
  }

  suspend fun getUpdates(): List<HistoryUpdateInfo> {
    return client.getJson(serverUrl + MeisterchartsDataEndpoints.History.Updates.updatesUrl(clientId))
  }
}
