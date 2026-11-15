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

import com.meistercharts.data.rest.ClientId
import com.meistercharts.data.rest.HistoryBucketRestResponse
import com.meistercharts.data.rest.MeisterchartsDataEndpoints
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.rest.HistoryBucketList
import com.meistercharts.history.storage.FileHistoryStorage
import com.meistercharts.time.TimeRange
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import it.neckar.ktor.get
import it.neckar.ktor.getOrFail
import it.neckar.ktor.post
import it.neckar.ktor.put
import it.neckar.ktor.server.openapi.ktor.doc
import it.neckar.open.http.UrlParameterName
import it.neckar.open.unit.si.ms
import it.neckar.rest.InvalidQueryParameterException
import kotlin.uuid.Uuid

fun Application.historyRouting(storage: FileHistoryStorage, clientUpdatesSupport: ClientUpdatesSupport) {
  routing {

    get(MeisterchartsDataEndpoints.Config) {
      call.respond<HistoryConfiguration>(storage.historyConfiguration)
    }.doc {
      summary = "Get the history configuration"
      description = "Returns the history configuration"
      response("The history configuration") {
        body<HistoryConfiguration> {
          noExamples()
        }
      }
    }

    put(MeisterchartsDataEndpoints.History.Buckets_Store) {
      val receivedBucket = call.receive<HistoryBucket>()
      storage.storeWithoutCache(receivedBucket, HistoryUpdateInfo(receivedBucket.descriptor.bucketRange.samplingPeriod, TimeRange(receivedBucket.start, receivedBucket.end)))
      call.respond<HttpStatusCode>(HttpStatusCode.Created)
    }

    get(MeisterchartsDataEndpoints.History.Bucket) {
      val descriptor = call.parameters.extractHistoryBucketDescriptor()
      val bucket = storage.get(descriptor)

      call.respond<HistoryBucketRestResponse>(HistoryBucketRestResponse(descriptor, bucket?.chunk))
    }

    get(MeisterchartsDataEndpoints.History.Query) {
      val samplingPeriod = call.parameters.bucketRange().samplingPeriod
      @ms val from = call.parameters.timestamp(MeisterchartsDataEndpoints.Parameters.from)
      @ms val to = call.parameters.timestamp(MeisterchartsDataEndpoints.Parameters.to)

      val buckets = storage.query(from, to, samplingPeriod)
      call.respond<HistoryBucketList>(HistoryBucketList(buckets))
    }


    post(MeisterchartsDataEndpoints.History.Updates.Register) {
      val receivedId = call.parameters.extractClientId()
      clientUpdatesSupport.registerClient(receivedId)
      call.respond<HttpStatusCode>(HttpStatusCode.Accepted)
    }

    post(MeisterchartsDataEndpoints.History.Updates.Unregister) {
      val receivedId = call.parameters.extractClientId()
      clientUpdatesSupport.deleteClientFromUpdateList(receivedId.uuid)
      call.respond<HttpStatusCode>(HttpStatusCode.Accepted)
    }

    get(MeisterchartsDataEndpoints.History.Updates.Updates) {
      val receivedId = call.parameters.extractClientId()
      call.respond<MutableList<HistoryUpdateInfo>>(clientUpdatesSupport.getUpdates(receivedId))
    }
  }
}

fun Parameters.bucketRange(): HistoryBucketRange {
  val received = getOrFail(MeisterchartsDataEndpoints.Parameters.bucketRange)

  //Not HistoryBucketRange.valueOf: its IllegalArgumentException names the enum but neither the parameter nor the accepted values
  return HistoryBucketRange.entries.firstOrNull { it.name == received }
    ?: throw InvalidQueryParameterException(
      "Invalid bucket range [$received] for parameter [${MeisterchartsDataEndpoints.Parameters.bucketRange.value}]. Value must be one of: [${HistoryBucketRange.entries.joinToString(", ") { it.name }}]"
    )
}

private fun Parameters.extractHistoryBucketDescriptor(): HistoryBucketDescriptor {
  val bucketRange = bucketRange()
  @ms val start = timestamp(MeisterchartsDataEndpoints.Parameters.start)

  return HistoryBucketDescriptor.forStart(start, bucketRange)
}

/**
 * Returns the timestamp the parameter [parameterName] holds.
 *
 * Rejects a non-numeric value with the parameter name *and* the received value: the [NumberFormatException] of a plain
 * `toDouble` only contains the value, which leaves the caller of an endpoint with three timestamp-ish parameters
 * guessing which one was rejected.
 */
private fun Parameters.timestamp(parameterName: UrlParameterName): @ms Double {
  val received = getOrFail(parameterName)

  return received.toDoubleOrNull()
    ?: throw InvalidQueryParameterException("Invalid timestamp [$received] for parameter [${parameterName.value}]. Value must be the milliseconds since 1970-01-01T00:00:00Z")
}

private fun Parameters.extractClientId(): ClientId {
  val received = getOrFail(MeisterchartsDataEndpoints.Parameters.clientId)

  val uuid = try {
    Uuid.parse(received)
  } catch (e: IllegalArgumentException) {
    //Uuid.parse describes the expected format but does not say which parameter carried the rejected value
    throw InvalidQueryParameterException("Invalid client id [$received] for parameter [${MeisterchartsDataEndpoints.Parameters.clientId.value}]. Value must be a UUID", e)
  }

  return ClientId(uuid)
}
