package com.meistercharts.history

import com.meistercharts.history.query.AsyncHistoryAccess
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
