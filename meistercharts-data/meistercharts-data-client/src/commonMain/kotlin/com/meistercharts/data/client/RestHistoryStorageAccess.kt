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

import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryObserver
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.ObservableHistoryStorage
import it.neckar.logging.debug
import it.neckar.open.async.PlatformReadWriteLock
import it.neckar.open.async.read
import it.neckar.open.async.write
import it.neckar.open.collections.cache
import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable
import it.neckar.open.time.formatUtcForDebug
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms
import kotlinx.coroutines.*


class RestHistoryStorageAccess(
  val dataClient: DataClient,
  val requestScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
  val updateScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
  val disposeScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : ObservableHistoryStorage, Disposable {

  val lock: PlatformReadWriteLock = PlatformReadWriteLock()

  /**
   * The cache - guarded by [lock]
   */
  private val cache = cache<HistoryBucketDescriptor, HistoryBucketNullableWrapper>("CachedRemoteHistoryStorage", 1000) { historyBucketDescriptor, wrapper ->
    logger.info("removed from cache $historyBucketDescriptor , $wrapper")
  }

  internal val currentlyGetting: MutableSet<HistoryBucketDescriptor> = mutableSetOf()
  private val observers = mutableListOf<HistoryObserver>()

  private val logger: it.neckar.logging.Logger = it.neckar.logging.LoggerFactory.getLogger("com.meistercharts.data.client.RestHistoryStorageAccess")

  override fun getStart(): Double {
    return Double.NaN
  }

  override fun getEnd(): Double {
    return Double.NaN
  }

  /**
   * notifies observer via Dispatchers.Main to avoid concurrent modification exception*/
  override fun observe(observer: HistoryObserver) {
    observers.add(observer)
  }

  /** Gets the History Bucket for the given descriptor. Uses a cache to minimize response time. */
  override fun get(descriptor: HistoryBucketDescriptor): HistoryBucket? {
    logger.info("getting Bucket for descriptor: $descriptor")

    lock.read {
      val result = cache[descriptor]
      if (result != null) {
        return result.historyBucket
      }
    }


    if (currentlyGetting.contains(descriptor)) {
      return null
    }
    currentlyGetting.add(descriptor)
    requestScope.launch {
      try {
        val historyBucket: HistoryBucket? = dataClient.get(descriptor)

        storeInCache(descriptor, historyBucket)
        informObservers(descriptor)
      } finally {
        //also on a failed request - else the descriptor would be blocked forever and never requested again
        currentlyGetting.remove(descriptor)
      }
    }
    return null
  }

  fun storeInCache(descriptor: HistoryBucketDescriptor, historyBucket: HistoryBucket?) {
    require(historyBucket == null || descriptor == historyBucket.descriptor) { "Expected Descriptor: $descriptor but was ${historyBucket?.descriptor}" }

    lock.write {
      cache.store(descriptor, HistoryBucketNullableWrapper(historyBucket))
    }
  }

  suspend fun informObservers(descriptor: HistoryBucketDescriptor) {
    val historyUpdateInfo = HistoryUpdateInfo.from(descriptor)
    withContext(Dispatchers.Main) {
      observers.fastForEach {
        it(historyUpdateInfo)
      }
    }
  }

  /** Repeatedly checks for updates from the server, creates the corresponding descriptors and informs the observers that changes occurred.  */
  fun getUpdateDescriptors(delay: Long = 1000): Job {
    return updateScope.launch {
      while (isActive) {
        val descriptorSet = mutableSetOf<HistoryBucketDescriptor>()
        val updateList = dataClient.getUpdates()
        @ms val now = nowMillis()
        logger.debug {
          "updates on ${now.formatUtcForDebug()}, empty: ${updateList.isEmpty()}"
        }
        for (update in updateList) {
          val samplingPeriod = update.samplingPeriod
          for (timeRange in update.updatedTimeRanges) {
            var current = timeRange.start
            while (current < timeRange.end) {
              val end = current + samplingPeriod.toHistoryBucketRange().duration
              HistoryBucketDescriptor.forRange(current, end, samplingPeriod.toHistoryBucketRange()).forEach {
                descriptorSet.add(it)
              }
              current = end
            }
          }
        }

        for (descriptor in descriptorSet) {
          lock.write {
            cache.remove(descriptor)
          }
          currentlyGetting.remove(descriptor)
        }

        for (descriptor in descriptorSet) {
          informObservers(descriptor)
        }
        delay(delay)
      }


      logger.warn("UPDATE SCOPE NOT ACTIVE!!!")
    }
  }

  suspend fun getConfig(): HistoryConfiguration {
    return dataClient.getConfig()
  }

  suspend fun registerAtServer() {
    dataClient.registerAtServer()
  }

  fun deregisterAtServer() {
    disposeScope.launch {
      dataClient.unregisterAtServer()
      disposeScope.cancel("Disposing Storage Access")
    }
  }


  override fun dispose() {
    deregisterAtServer()
    updateScope.cancel("Disposing Storage Access")
    requestScope.cancel("Disposing Storage Access")
  }

  override fun onDispose(action: () -> Unit) {
    throw UnsupportedOperationException("JOHANNES PLS FIX")
  }
}

data class HistoryBucketNullableWrapper(val historyBucket: HistoryBucket?)
