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


import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.formatUtc
import com.meistercharts.history.impl.HistoryChunk

/**
 * Writable history storage
 */
interface WritableHistoryStorage : HistoryStorage {
  /**
   * Stores a history bucket - without using the cache.
   *
   * ATTENTION: Use [HistoryStorageCache] instead of directly adding the data!
   */
  fun storeWithoutCache(bucket: HistoryBucket, updateInfo: HistoryUpdateInfo)

  /**
   * Deletes the bucket described by the descriptor
   */
  fun delete(descriptor: HistoryBucketDescriptor)

  /**
   * Stores the given [chunkToStore] for the given sampling period.
   *
   * ATTENTION: Use [HistoryStorageCache] instead of directly adding the data!
   */
  fun storeWithoutCache(chunkToStore: HistoryChunk, samplingPeriod: SamplingPeriod) {
    if (chunkToStore.isEmpty()) {
      return
    }

    //Find all descriptors that match the start/end of the chunk that shall be saved
    val relevantDescriptors = HistoryBucketDescriptor.fromChunk(chunkToStore, samplingPeriod)

    //Iterate over all relevant iterators and merge each one
    relevantDescriptors.fastForEach { descriptor ->
      //The bucket that already exists in the history storage
      val originalBucket = get(descriptor) ?: HistoryBucket(descriptor, chunkToStore.withoutValues())

      //Merge the original bucket with the new chunk
      originalBucket.chunk.merge(chunkToStore, descriptor.start, descriptor.end)?.let { merged ->
        require(merged.start >= descriptor.start) {
          "Invalid start: ${merged.start.formatUtc()} for descriptor $descriptor"
        }
        require(merged.end < descriptor.end) {
          "Invalid end: ${merged.end.formatUtc()} most not end after end for descriptor $descriptor"
        }

        //Save the merged history bucket
        storeWithoutCache(HistoryBucket(descriptor, merged), HistoryUpdateInfo.fromChunk(chunkToStore, samplingPeriod))
      }
    }
  }

}
