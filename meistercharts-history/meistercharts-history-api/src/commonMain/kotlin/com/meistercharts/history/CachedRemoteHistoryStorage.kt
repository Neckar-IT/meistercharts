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

import it.neckar.open.collections.cache
import it.neckar.open.dispose.DisposeSupport

/**
 * Returns history buckets from a remote source - cached
 */
class CachedRemoteHistoryStorage(val asyncHistoryAccess: AsyncHistoryAccess) : HistoryStorage {
  /**
   * Contains the cached history buckets
   */
  private val cache = cache<HistoryBucketDescriptor, HistoryBucket>("CachedRemoteHistoryStorage", 100)

  override fun getStart(): Double {
    return Double.NaN
  }

  override fun getEnd(): Double {
    return Double.NaN
  }

  override fun get(descriptor: HistoryBucketDescriptor): HistoryBucket? {
    val cached = cache[descriptor]

    if (cached != null) {
      return cached
    }

    asyncHistoryAccess.query(descriptor) {
      cache.store(descriptor, it)
      //TODO notify
    }

    return null
  }

  private val disposeSupport = DisposeSupport()

  override fun onDispose(action: () -> Unit) {
    disposeSupport.onDispose(action)
  }
}
