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

import com.meistercharts.time.TimeRange
import com.meistercharts.history.cleanup.HistoryCleanupService
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.downsampling.DownSamplingService
import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.formatting.formatUtc
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms


/**
 * Stores the history buckets in memory.
 *
 * Attention: It is necessary to dispose the history storage itself by calling [dispose] to clean up the started timer if
 * any of the schedule* methods has been called.
 */
open class InMemoryHistoryStorage : HistoryStorage, WritableHistoryStorage, ObservableHistoryStorage, Disposable {
  private val disposeSupport = DisposeSupport()

  internal val map = mutableMapOf<HistoryBucketDescriptor, HistoryBucket>()

  /**
   * Bookkeeping class
   */
  val bookKeeping: InMemoryBookKeeping = InMemoryBookKeeping()

  override fun getStart(): Double {
    val historyBucketRange = naturalSamplingPeriod.toHistoryBucketRange()
    val descriptor = bookKeeping.earliestBound(historyBucketRange) ?: return Double.NaN

    val first = get(descriptor) ?: return Double.NaN
    if (first.isEmpty()) {
      return Double.NaN
    }

    return first.chunk.firstTimeStamp()
  }

  override fun getEnd(): @ms @MayBeNaN Double {
    val historyBucketRange = naturalSamplingPeriod.toHistoryBucketRange()
    val descriptor = bookKeeping.latestBound(historyBucketRange) ?: return Double.NaN

    val last = get(descriptor) ?: return Double.NaN
    if (last.isEmpty()) {
      return Double.NaN
    }

    return last.chunk.lastTimeStamp()
  }

  /**
   * The down sampling service that can be used to calculate the down sampling ([scheduleDownSampling])
   */
  internal val downSamplingService: DownSamplingService = DownSamplingService(this).also {
    disposeSupport.onDispose(it)
  }

  /**
   * The cleanup service for this history storage. Must be scheduled manually ([scheduleCleanupService])
   */
  internal val historyCleanupService: HistoryCleanupService = HistoryCleanupService(this).also {
    disposeSupport.onDispose(it)
  }

  /**
   * The natural sampling period - that is the bucket range that is used to add the "natural" values as they have been provided.
   *
   * Attention: When the [naturalSamplingPeriod] is changed, it is almost always necessary to call [clear].
   */
  var naturalSamplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis

  /**
   * The max size configuration that is used by the [historyCleanupService].
   */
  var maxSizeConfiguration: MaxHistorySizeConfiguration = MaxHistorySizeConfiguration(100)

  /**
   * Returns the number of buckets
   */
  val bucketCount: Int
    get() = map.size

  /**
   * Returns all descriptors
   */
  internal fun keys(): Set<HistoryBucketDescriptor> {
    return map.keys
  }

  /**
   * Removes all entries from this history
   */
  fun clear() {
    // make a copy of the descriptors that are about to be removed
    val historyBucketDescriptors = map.keys.toList()
    // clear the history before notifying the observers
    map.clear()
    bookKeeping.clear()

    // notify the observers
    historyBucketDescriptors.fastForEach { descriptor ->
      observers.fastForEach {
        it(descriptor, HistoryUpdateInfo(descriptor.bucketRange.samplingPeriod, TimeRange(descriptor.start, descriptor.end)))
      }
    }
  }

  override fun get(descriptor: HistoryBucketDescriptor): HistoryBucket? {
    return map[descriptor]
  }

  /**
   * Stores the bucket.
   * Overwrites an existing bucket with the same descriptor - if there is one
   */
  override fun storeWithoutCache(bucket: HistoryBucket, updateInfo: HistoryUpdateInfo) {
    map[bucket.descriptor] = bucket
    bookKeeping.store(bucket.descriptor)

    observers.fastForEach {
      it(bucket.descriptor, updateInfo)
    }
  }

  /**
   * Deletes the history bucket for the given descriptor
   */
  override fun delete(descriptor: HistoryBucketDescriptor) {
    bookKeeping.remove(descriptor)
    map.remove(descriptor) ?: return

    val historyUpdateInfo = HistoryUpdateInfo.from(descriptor)
    observers.fastForEach {
      it(descriptor, historyUpdateInfo)
    }
  }

  private val observers = mutableListOf<HistoryObserver>()

  override fun observe(observer: HistoryObserver) {
    observers.add(observer)
  }

  /**
   * Schedules down sampling for the storage (starts a timer).
   *
   * Attention: It is necessary to dispose the history storage itself by calling [dispose] to clean up the started timer
   *
   * Attention: It is necessary to schedule down sampling *before* any data is added to the history storage
   */
  fun scheduleDownSampling() {
    downSamplingService.scheduleDownSampling(this)
  }

  /**
   * Schedules the cleanup service that deletes the history automatically (starts a timer).
   *
   * Attention: It is necessary to dispose the history storage itself by calling [dispose] to clean up the started timer
   */
  fun scheduleCleanupService() {
    historyCleanupService.schedule()
  }

  /**
   * Deletes the described history buckets and all buckets before - with the same range
   */
  fun deleteAndBefore(toDelete: HistoryBucketDescriptor): DeletionReport {
    val earliest = bookKeeping.earliestBound(toDelete.bucketRange) ?: return DeletionReport.empty

    if (toDelete.start < earliest.start) {
      //The file we want to delete is earlier than everything we have in the storage. Therefore there is nothing to delete
      return DeletionReport.empty
    }

    val deletedDescriptors = mutableListOf<HistoryBucketDescriptor>()

    var current = toDelete

    while (current.start >= earliest.start) {
      delete(current)
      deletedDescriptors.add(current)
      current = current.previous()
    }

    //Fix the bookkeeping
    //We delete from later to earlier, therefore the bookkeeping is not correct
    bookKeeping.setEarliestBound(toDelete.next())

    return DeletionReport(deletedDescriptors)
  }

  override fun dispose() {
    disposeSupport.dispose()
  }

  override fun onDispose(action: () -> Unit) {
    disposeSupport.onDispose(action)
  }
}

/**
 * Bookkeeping class for [InMemoryHistoryStorage]
 */
class InMemoryBookKeeping {
  /**
   * Returns the time range from the earliest to the latest
   */
  @Deprecated("do not use anymore")
  fun getTimeRange(range: HistoryBucketRange): TimeRange? {
    val startDescriptor = earliestBound(range) ?: return null
    val endDescriptor = latestBound(range) ?: return null

    check(startDescriptor.start <= endDescriptor.end) { "start ${startDescriptor.start.formatUtc()} is greater than end ${endDescriptor.end.formatUtc()}" }

    return TimeRange(startDescriptor.start, endDescriptor.end)
  }

  /**
   * Contains the earliest (possible) stored descriptor for every bucket range
   */
  private val earliestBound: MutableMap<HistoryBucketRange, HistoryBucketDescriptor> = mutableMapOf()

  /**
   * Contains the latest stored descriptor for every bucket range
   */
  private val latestBound: MutableMap<HistoryBucketRange, HistoryBucketDescriptor> = mutableMapOf()

  fun clear() {
    earliestBound.clear()
    latestBound.clear()
  }

  /**
   * Ensures that [latestBound].end is always greater than [earliestBound].start
   */
  private fun fixBounds(bucketRange: HistoryBucketRange) {
    val start = earliestBound[bucketRange] ?: return
    val end = latestBound[bucketRange] ?: return
    if (start.start >= end.end) {
      latestBound.remove(bucketRange)
    }
  }

  /**
   * Stores the history bucket descriptor
   */
  fun store(descriptor: HistoryBucketDescriptor) {
    val bucketRange = descriptor.bucketRange

    earliestBound[bucketRange].let { current ->
      if (current == null || current.start > descriptor.start) {
        earliestBound[bucketRange] = descriptor
      }
    }

    latestBound[bucketRange].let { current ->
      if (current == null || current.end < descriptor.end) {
        latestBound[bucketRange] = descriptor
      }
    }

    fixBounds(bucketRange)
  }

  /**
   * Removes the given descriptor
   */
  fun remove(descriptor: HistoryBucketDescriptor) {
    val bucketRange = descriptor.bucketRange

    //If earliestBound[bucketRange] == latestBound[bucketRange] then the following
    //lines will lead to earliestBound[bucketRange] > latestBound[bucketRange] which
    //needs to be fixed by fixBounds.
    if (earliestBound(bucketRange) == descriptor) {
      earliestBound[bucketRange] = descriptor.next()
    }

    if (latestBound(bucketRange) == descriptor) {
      latestBound[bucketRange] = descriptor.previous()
    }

    fixBounds(bucketRange)
  }

  /**
   * Retrieve the earliest [HistoryBucketDescriptor] (lower bound)
   */
  fun earliestBound(range: HistoryBucketRange): HistoryBucketDescriptor? {
    return earliestBound[range]
  }

  fun setEarliestBound(descriptor: HistoryBucketDescriptor) {
    this.earliestBound[descriptor.bucketRange] = descriptor
    fixBounds(descriptor.bucketRange)
  }

  /**
   * Retrieve the latest [HistoryBucketDescriptor] (upper bound)
   */
  fun latestBound(range: HistoryBucketRange): HistoryBucketDescriptor? {
    return latestBound[range]
  }
}

/**
 * Contains information about deleted buckets
 */
data class DeletionReport(val deletedDescriptors: List<HistoryBucketDescriptor>) {

  companion object {
    /**
     * Empty deletion report
     */
    val empty: DeletionReport = DeletionReport(emptyList())
  }
}
