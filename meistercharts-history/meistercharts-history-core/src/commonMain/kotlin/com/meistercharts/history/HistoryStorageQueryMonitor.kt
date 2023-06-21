package com.meistercharts.history

import com.meistercharts.algorithms.TimeRange
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.removeFirst
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Callback that is notified about queries.
 */
typealias OnQueryCallback = (descriptor: HistoryBucketDescriptor) -> Unit

/**
 * A [HistoryStorage] that logs queries.
 */
class HistoryStorageQueryMonitor<HistoryStorageType : ObservableHistoryStorage>(
  val historyStorage: HistoryStorageType,
  /**
   * The max size of the [knownDescriptors] set
   */
  private val knownDescriptorsMaxSize: Int = 10_000,
  ) : HistoryStorage, ObservableHistoryStorage {

  /**
   * Remember the last descriptor that was queried.
   * This field is used to ensure that [onQueryCallbacks] are only called for distinct queries.
   */
  private var lastDescriptor: HistoryBucketDescriptor? = null

  /**
   * Remember all known descriptors.
   * This field is used to ensure that [onQueryForNewDescriptorsCallbacks] are only called once for each [HistoryBucketDescriptor].
   */
  private val knownDescriptors = LinkedHashSet<HistoryBucketDescriptor>()

  override fun get(descriptor: HistoryBucketDescriptor): HistoryBucket? {
    if (lastDescriptor != descriptor) {
      lastDescriptor = descriptor

      onQueryCallbacks.fastForEach {
        it(descriptor)
      }
    }

    if (knownDescriptors.add(descriptor)) {
      onQueryForNewDescriptorsCallbacks.fastForEach {
        it(descriptor)
      }

      // Limit the size of the set
      while (knownDescriptors.size > knownDescriptorsMaxSize) {
        knownDescriptors.removeFirst()
      }
    }

    return historyStorage.get(descriptor)
  }

  /**
   * The callbacks that are notified about all queries.
   */
  private val onQueryCallbacks = mutableListOf<OnQueryCallback>()

  private val onQueryForNewDescriptorsCallbacks = mutableListOf<OnQueryCallback>()

  /**
   * Registers a lambda that is notified about queries.
   * Only is called for distinct queries (never called for the same [HistoryBucketDescriptor] twice in a row).
   */
  fun onQuery(callback: OnQueryCallback) {
    onQueryCallbacks.add(callback)
  }

  /**
   * Registers a lambda that is notified about new queries.
   * Only is called once for each [HistoryBucketDescriptor].
   */
  fun onQueryForNewDescriptor(callback: OnQueryCallback) {
    onQueryForNewDescriptorsCallbacks.add(callback)
  }

  override fun getStart(): Double {
    return historyStorage.getStart()
  }

  override fun getEnd(): @ms @MayBeNaN Double {
    return historyStorage.getEnd()
  }

  override fun onDispose(action: () -> Unit) {
    historyStorage.onDispose(action)
  }

  override fun observe(observer: HistoryObserver) {
    historyStorage.observe(observer)
  }
}

/**
 * Wraps this [HistoryStorage] with a [HistoryStorageQueryMonitor].
 */
fun <DelegateType : ObservableHistoryStorage> DelegateType.withQueryMonitor(): HistoryStorageQueryMonitor<DelegateType> = HistoryStorageQueryMonitor(this)
