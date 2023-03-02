package com.meistercharts.history

typealias HistoryObserver = (HistoryBucketDescriptor, updateInfo: HistoryUpdateInfo) -> Unit

/**
 * History storage that can be observed and notifies about model changes
 */
interface ObservableHistoryStorage : HistoryStorage {
  /**
   * Observes the history storage changes
   */
  fun observe(observer: HistoryObserver)
}
