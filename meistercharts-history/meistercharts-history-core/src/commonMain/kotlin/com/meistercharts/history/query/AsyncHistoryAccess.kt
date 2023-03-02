package com.meistercharts.history.query

import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor

/**
 * Can be used for async history queries
 */
fun interface AsyncHistoryAccess {
  /**
   * Queries the history asynchronously.
   */
  fun query(
    /**
     * Describes the history bucket that is requested
     */
    descriptor: HistoryBucketDescriptor,
    /**
     * The consumer is notified when the history bucket have been retrieved
     */
    consumer: (HistoryBucket) -> Unit
  )
}
