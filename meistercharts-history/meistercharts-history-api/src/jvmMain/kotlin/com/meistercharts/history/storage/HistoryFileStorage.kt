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
package com.meistercharts.history.storage

import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.WritableHistoryStorage
import it.neckar.open.kotlin.lang.floor
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.unit.si.ms
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Offers access to history files using the storage
 */
@Deprecated("Use FileHistoryStorage instead")
class HistoryFileStorage(
  val baseDir: File,
  val serializer: HistoryStorageSerializer
) : HistoryStorage, WritableHistoryStorage, Disposable {

  init {
    require(baseDir.isDirectory && baseDir.exists()) {
      "Invalid base dir: <${baseDir.absolutePath}>"
    }
  }

  override fun getStart(): Double {
    return Double.NaN
  }

  override fun getEnd(): Double {
    return Double.NaN
  }

  /**
   * Returns the file for the given descriptor
   */
  fun getFile(descriptor: HistoryBucketDescriptor): File {
    return File(baseDir, getFileName(descriptor))
  }

  /**
   * Returns the
   */
  fun getFileName(descriptor: HistoryBucketDescriptor): String {
    val folders = calculateBaseFolders(descriptor)

    return folders.joinToString("/", descriptor.bucketRange.name + "/", "/${descriptor.start.toLong()}") {
      "${it.millis.toLong()}"
    }
  }

  override fun get(descriptor: HistoryBucketDescriptor): HistoryBucket? {
    return null
  }

  override fun storeWithoutCache(bucket: HistoryBucket, updateInfo: HistoryUpdateInfo) {
    val file = getFile(bucket.descriptor)
    file.parentFile.mkdirs()

    file.outputStream().use {
      serializer.serialize(bucket, it)
    }
  }

  override fun delete(descriptor: HistoryBucketDescriptor) {
    throw UnsupportedOperationException("Deleting is currently not supported")
  }

  private val disposeSupport = DisposeSupport()

  override fun onDispose(action: () -> Unit) {
    disposeSupport.onDispose(action)
  }

  override fun dispose() {
    disposeSupport.dispose()
  }

  companion object {
    /**
     * The number of files that are stored within one directory
     */
    const val filesPerFolder: Int = 500

    /**
     * The threshold for the base folder. This is the minimum amount of time the first folder must span.
     */
    val baseFolderThreshold: @ms Double = TimeUnit.DAYS.toMillis(100).toDouble()

    /**
     * Returns the milli seconds of the base folder for a given descriptor
     */
    internal fun calculateBaseFolderValue(descriptor: HistoryBucketDescriptor): TimestampWithPrecision {
      val bucketRange = descriptor.bucketRange
      val start = descriptor.start

      return calculateBaseFolderStart(bucketRange, start)
    }

    /**
     * Calculates a base folder name for a given bucket range and milli seconds.
     * Rounds the given start date using the precision from the duration
     */
    private fun calculateBaseFolderStart(bucketRange: HistoryBucketRange, start: @ms Double): TimestampWithPrecision {
      val precisionFactor = bucketRange.duration * filesPerFolder
      return TimestampWithPrecision.calculateFloor(start, precisionFactor)
    }

    /**
     * Calculates the base folders for the given descriptor
     */
    internal fun calculateBaseFolders(descriptor: HistoryBucketDescriptor): List<TimestampWithPrecision> {
      val bases = mutableListOf<TimestampWithPrecision>()

      //Add the first base folder
      var current: TimestampWithPrecision = calculateBaseFolderStart(descriptor.bucketRange, descriptor.start)
      bases.add(current)

      //Return immediately if the threshold has been reached
      if (current.precision > baseFolderThreshold) {
        return bases
      }

      //Additional base folders if required
      do {
        val newCurrent = TimestampWithPrecision.calculateFloor(current.millis, current.precision * filesPerFolder)
        require(newCurrent != current) {
          "New current <$newCurrent> must be different than current current: $current"
        }

        current = newCurrent
        bases.add(current)
      } while (current.precision < baseFolderThreshold)

      return bases.reversed()
    }
  }
}

/**
 * Contains the timestamp and a precision.
 * Is used to describe a folder in the storage.
 */
data class TimestampWithPrecision(
  /**
   * The milli seconds
   */
  val millis: @ms Double,
  /**
   * The precision factor (usually powers of 10).
   *
   * Examples:
   *
   * * 1: the value is exact to one milli second
   * * 10: the value is exact to one ten milliseconds
   */
  val precision: @ms Double
) {

  init {
    require(!millis.isNaN()) {
      "Millis must not be NaN"
    }

    require(precision > 0) {
      "Precision must be greater than 0 but was <$precision>"
    }
  }

  companion object {
    /**
     * Calculates the time stamp
     */
    fun calculateFloor(value: @ms Double, precision: @ms Double): TimestampWithPrecision {
      require(precision > 0) {
        "Precision must be greater than 0 but was <$precision>"
      }

      return TimestampWithPrecision(
        (value / precision).floor() * precision,
        precision
      )
    }
  }
}
