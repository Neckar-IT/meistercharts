package com.meistercharts.history

import com.meistercharts.algorithms.TimeRange
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.formatUtc
import it.neckar.open.unit.other.Exclusive
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.si.ms
import com.meistercharts.history.impl.HistoryChunk
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/**
 * Identifies a history bucket.
 */
@Serializable
data class HistoryBucketDescriptor
/**
 * Use the companion methods
 */
internal constructor(
  /**
   * The index of the descriptor.
   *
   * [index] * [bucketRange].distance describes the start of the bucket
   *
   * Attention: The index *must* be of type [Double]. [Int] does not have the necessary range - Long is not supported well with JS.
   */
  val index: Double,
  /**
   * The duration of the bucket
   */
  val bucketRange: HistoryBucketRange
) {
  /**
   * The time range of the bucket (start inclusive, end exclusive)
   * The start date has to be placed on "even" ranges depending on the bucket range
   */
  val timeRange: TimeRange
    get() {
      return TimeRange(start, end)
    }

  /**
   * The start of the bucket (inclusive).
   * The start date has been placed on "even" ranges depending on the bucket range
   */
  val start: @Inclusive @ms Double = bucketRange.calculateStartForIndex(index)

  /**
   * The end of the bucket (exclusive) - is calculated automatically using the range and the given start
   */
  val end: @Exclusive @ms Double = bucketRange.calculateEndForIndex(index)

  /**
   * The duration of the bucket
   */
  val duration: @ms Double
    get() = bucketRange.duration

  /**
   * Returns the time at the center
   */
  val center: @ms Double
    get() {
      return (start + end) / 2.0
    }

  /**
   * Returns true if this descriptor contains the given time
   */
  fun contains(time: @ms Double): Boolean {
    return start <= time && end > time
  }

  /**
   * Returns the next descriptor (directly after this one)
   * @param distance describes the distance to the neighbor. A distance of 1 describes the direct neighbor
   */
  fun next(distance: Int = 1): HistoryBucketDescriptor {
    require(distance > 0) { "Invalid distance <${distance}>" }
    return HistoryBucketDescriptor(index + distance, bucketRange)
  }

  /**
   * Returns the previous descriptor (directly before this one)
   * @param distance describes the distance to the neighbor. A distance of 1 describes the direct neighbor
   */
  fun previous(distance: Int = 1): HistoryBucketDescriptor {
    require(distance > 0) { "Invalid distance <${distance}>" }

    return HistoryBucketDescriptor(index - distance, bucketRange)
  }

  /**
   * Lists all children descriptors.
   *
   * Children are descriptors that have a higher resolution (smaller distance between data points) and therefore contain more information.
   *
   * Returns an empty list if there are no children (for the lowest level)
   */
  fun children(): List<HistoryBucketDescriptor> {
    val lowerRange = bucketRange.lower() ?: return emptyList()
    return forRange(start, end, lowerRange, false)
  }

  /**
   * Returns the parent history bucket descriptor.
   *
   * The parent uses information from several children and contains down sampled values.
   *
   * Returns null if there is no parent (for the highest level)
   */
  fun parent(): HistoryBucketDescriptor? {
    val upperRange = bucketRange.upper() ?: return null
    return forTimestamp(start, upperRange)
  }

  /**
   * Returns the distance between two descriptors
   */
  fun distanceTo(lastDescriptor: HistoryBucketDescriptor): Double {
    require(this.bucketRange == lastDescriptor.bucketRange) {
      "Same bucket ranges required. But this has <${this.bucketRange}> and the last descriptor has <${lastDescriptor.bucketRange}>"
    }

    return lastDescriptor.index - index
  }

  override fun toString(): String {
    return "HistoryBucketDescriptor(bucketRange=$bucketRange, start=${start.formatUtc()}, end=${end.formatUtc()})"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as HistoryBucketDescriptor

    if (index != other.index) return false
    if (bucketRange != other.bucketRange) return false

    return true
  }

  override fun hashCode(): Int {
    var result = index.hashCode()
    result = 31 * result + bucketRange.hashCode()
    return result
  }

  companion object {
    /**
     * Creates a new history bucket descriptor for the given start date.
     *
     * Attention: It is necessary that the given start date is an exact date for the given bucket range
     */
    fun forStart(start: @ms Double, bucketRange: HistoryBucketRange): HistoryBucketDescriptor {
      val exactStart = bucketRange.calculateStart(start)
      require(exactStart == start) {
        "Invalid start provided. Was <${start.formatUtc()}> but expected <${exactStart.formatUtc()}>"
      }

      val index = bucketRange.calculateIndex(start)
      return HistoryBucketDescriptor(index, bucketRange)
    }

    /**
     * Returns the descriptor that contains the given timestamp for the given bucket range
     */
    fun forTimestamp(timestamp: @ms Double, bucketRange: HistoryBucketRange): HistoryBucketDescriptor {
      val index = bucketRange.calculateIndex(timestamp)
      return HistoryBucketDescriptor(index, bucketRange)
    }

    /**
     * Returns the descriptor that contains the given timestamp for the given sampling period
     */
    fun forTimestamp(timestamp: @ms Double, samplingPeriod: SamplingPeriod): HistoryBucketDescriptor {
      return forTimestamp(timestamp, samplingPeriod.toHistoryBucketRange())
    }

    /**
     * Returns all descriptors which contains (potential) data in the chunk.
     * This method also works for chunks with a (very) large span
     */
    fun fromChunk(chunk: HistoryChunk, samplingPeriod: SamplingPeriod): List<HistoryBucketDescriptor> {
      if (chunk.isEmpty()) {
        //No time stamps found
        return emptyList()
      }

      //Check if there is only one time stamp
      if (chunk.timeStampsCount == 1) {
        return listOf(forTimestamp(chunk.firstTimeStamp(), samplingPeriod))
      }

      //Find all descriptors for all time stamps for the chunk

      //Check if there is only one or two descriptors for first and last
      val firstDescriptor = forTimestamp(chunk.firstTimeStamp(), samplingPeriod)
      val lastDescriptor = forTimestamp(chunk.lastTimeStamp(), samplingPeriod)

      if (firstDescriptor == lastDescriptor) {
        //first and last are the same
        return listOf(firstDescriptor)
      }

      //check if they are (very) close together
      val distance = firstDescriptor.distanceTo(lastDescriptor)

      if (distance == 1.0) {
        //First and last are direct neighbors
        return listOf(firstDescriptor, lastDescriptor)
      }

      //Range within the max supported descriptors count
      if (distance < MaxSupportedDescriptorsCount) {
        return forRange(chunk.firstTimeStamp(), chunk.lastTimeStamp(), samplingPeriod.toHistoryBucketRange())
      }

      //We have to iterate over all timestamps and create descriptors where necessary
      return buildList {
        var currentDescriptor = firstDescriptor
        add(currentDescriptor)

        chunk.timeStamps.fastForEach { timeStamp ->
          if (currentDescriptor.contains(timeStamp)) {
            //Sill in the same descriptor
            return@fastForEach
          }

          //we have to create a new descriptor
          forTimestamp(timeStamp, samplingPeriod).let {
            //Store in map
            add(it)
            currentDescriptor = it
          }
        }
      }
    }

    /**
     * Returns the descriptors for the given range.
     * Attention: When looking for descriptors for a chunk, use [fromChunk] instead.
     */
    fun forRange(start: @ms @Inclusive Double, end: @ms Double, range: HistoryBucketRange, includeEnd: Boolean = true, maxDescriptorsCount: Int = MaxSupportedDescriptorsCount): List<HistoryBucketDescriptor> {
      @ms val delta = end - start
      val estimatedDescriptorsCount = (delta / range.duration).roundToInt()
      require(estimatedDescriptorsCount <= maxDescriptorsCount) {
        "Requested range too big. Up to $maxDescriptorsCount buckets are supported but would return $estimatedDescriptorsCount buckets. Start: ${start.formatUtc()}; end: ${end.formatUtc()}, bucket-range: $range (duration: ${range.duration} ms)"
      }


      val descriptors = mutableListOf<HistoryBucketDescriptor>()

      //the first descriptor
      var descriptor = forTimestamp(range.calculateStart(start), range)
      while (descriptor.start < end || (includeEnd && descriptor.start == end)) {
        descriptors.add(descriptor)
        descriptor = descriptor.next()
      }

      return descriptors
    }

    fun forIndex(index: Double, bucketRange: HistoryBucketRange): HistoryBucketDescriptor {
      return HistoryBucketDescriptor(index, bucketRange)
    }

    /**
     * Descriptors may be calculated only up to this number.
     *
     * @see [forRange]
     */
    const val MaxSupportedDescriptorsCount: Int = 100
  }
}

/**
 * Returns all [HistoryBucketDescriptor]s for all [HistoryBucket]s this chunk contains data for
 */
fun HistoryChunk.calculateAllDescriptorsFor(samplingPeriod: SamplingPeriod): List<HistoryBucketDescriptor> {
  return HistoryBucketDescriptor.fromChunk(this, samplingPeriod)
}
