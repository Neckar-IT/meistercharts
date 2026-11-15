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

import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.impl.HistoryChunk
import kotlinx.serialization.Serializable

/** Nullable Rest Response which contains a descriptor and a nullable History Chunk */
@Serializable
data class HistoryBucketRestResponse(
  val descriptor: HistoryBucketDescriptor,
  val chunk: HistoryChunk?,
) {
  /**
   * Converts this response to a HistoryBucket
   */
  fun toHistoryBucket(): HistoryBucket? {
    return if (this.chunk == null) {
      null
    } else {
      HistoryBucket(this.descriptor, this.chunk)
    }
  }
}


