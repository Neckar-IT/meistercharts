package com.meistercharts.history.downsampling

import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.TimestampIndex
import it.neckar.open.unit.si.ms

/**
 * Manages the target time stamps for down sampling calculations.
 *
 */
class DownSamplingTargetTimestampsIterator(
  /**
   * The (center) time stamps that are iterated
   */
  val timeStamps: @ms DoubleArray,
  /**
   * The distance between two timestamps for each slot
   */
  val distance: @ms Double
) {

  private var _index: Int = 0

  /**
   * The current index for the timestamp
   */
  val index: TimestampIndex
    get() {
      return TimestampIndex(_index)
    }

  /**
   * The center of the current slot
   */
  val slotCenter: @ms Double
    get() {
      return timeStamps[_index]
    }

  /**
   * The start of the current slot
   */
  val slotStart: @ms Double
    get() = slotCenter - distance / 2.0

  /**
   * The end timestamp for the current slot
   */
  val slotEnd: @ms Double
    get() = slotCenter + distance / 2.0

  fun next() {
    _index++

    require(index < timeStamps.size) { "Invalid call to next. Index is now <$index> but timestamps size is <${timeStamps.size}>" }
  }

  companion object {
    /**
     * Returns the down sampling iterator for the given descriptor.
     */
    fun create(sourceDescriptor: HistoryBucketDescriptor): DownSamplingTargetTimestampsIterator {
      return DownSamplingTargetTimestampsIterator(sourceDescriptor.calculateTimeStamps(), sourceDescriptor.bucketRange.distance)
    }
  }
}
