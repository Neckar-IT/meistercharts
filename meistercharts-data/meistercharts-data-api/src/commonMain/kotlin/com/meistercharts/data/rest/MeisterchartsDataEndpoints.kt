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
package com.meistercharts.data.rest

import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import it.neckar.open.annotations.Effective
import it.neckar.open.http.Url
import it.neckar.open.http.UrlParameterName
import it.neckar.open.http.UrlPattern
import it.neckar.open.unit.si.ms
import it.neckar.uuid.resolve

/**
 * Contains the API constants for Meistercharts Data Server
 *
 * The @Effective annotations spell out the resolved URL patterns. There is no test that regenerates them
 * for this object - they are maintained by hand and have to be updated whenever a pattern changes.
 */
object MeisterchartsDataEndpoints {

  object Parameters {
    val bucketRange: UrlParameterName = UrlParameterName("bucketRange")
    val start: UrlParameterName = UrlParameterName("start")
    val from: UrlParameterName = UrlParameterName("from")
    val to: UrlParameterName = UrlParameterName("to")
    val clientId: UrlParameterName = UrlParameterName("clientId")
  }

  @Effective("config")
  val Config: UrlPattern.Relative0 = Url.pattern("config")
  fun configUrl(): Url.Relative = Config.resolve()

  object History {
    @Effective("history")
    val BasePath: UrlPattern.Relative0 = Url.pattern("history")

    @Effective("history/buckets")
    val Buckets: UrlPattern.Relative0 = BasePath + "buckets"

    /**
     * Stores a bucket. No parameters are required, since the bucket sent in the body is self-describing.
     */
    @Effective("history/buckets/store")
    val Buckets_Store: UrlPattern.Relative0 = Buckets + "store"
    fun bucketsStoreUrl(): Url.Relative = Buckets_Store.resolve()

    /**
     * Pattern to query an exact bucket with a given range and start.
     */
    @Effective("history/buckets/{bucketRange}/{start}")
    val Bucket: UrlPattern.Relative2 = Buckets + Parameters.bucketRange + Parameters.start
    fun bucketUrl(bucketRange: HistoryBucketRange, start: @ms Double): Url.Relative = Bucket.resolve(bucketRange.name, start.toString())
    fun bucketUrl(descriptor: HistoryBucketDescriptor): Url.Relative = bucketUrl(descriptor.bucketRange, descriptor.start)

    /**
     * Query pattern to get a range of history data
     */
    @Effective("history/{bucketRange}/{from}/{to}")
    val Query: UrlPattern.Relative3 = BasePath + Parameters.bucketRange + Parameters.from + Parameters.to
    fun queryUrl(bucketRange: HistoryBucketRange, from: @ms Double, to: @ms Double): Url.Relative = Query.resolve(bucketRange.name, from.toString(), to.toString())

    object Updates {
      @Effective("history/register/{clientId}")
      val Register: UrlPattern.Relative1 = BasePath + "register" + Parameters.clientId
      fun registerUrl(clientId: ClientId): Url.Relative = Register.resolve(clientId)

      @Effective("history/unregister/{clientId}")
      val Unregister: UrlPattern.Relative1 = BasePath + "unregister" + Parameters.clientId
      fun unregisterUrl(clientId: ClientId): Url.Relative = Unregister.resolve(clientId)

      @Effective("history/updates/{clientId}")
      val Updates: UrlPattern.Relative1 = BasePath + "updates" + Parameters.clientId
      fun updatesUrl(clientId: ClientId): Url.Relative = Updates.resolve(clientId)
    }
  }

}
