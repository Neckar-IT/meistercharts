package com.meistercharts.history.impl

import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryStorage

/**
 * Mock implementation that returns a new (empty) instance every time
 */
class EmptyHistoryStorage : HistoryStorage {
  override fun get(descriptor: HistoryBucketDescriptor): HistoryBucket? {
    return null
  }

  /**
   * Attention: The registered action is never called since the EmptyHistoryStorage is never disposed
   */
  override fun onDispose(action: () -> Unit) {
  }
}
