package com.meistercharts.history.storage

import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryObserver
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.ObservableHistoryStorage
import com.meistercharts.history.WritableHistoryStorage
import com.meistercharts.history.downsampling.DownSamplingService
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.io.SerializableHistoryBucket
import com.meistercharts.history.impl.io.toSerializable
import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.i18n.TextKey
import it.neckar.open.io.writeTextWithRename
import it.neckar.open.time.millis
import it.neckar.open.time.millisToUtc
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Offers access to history files using the storage
 */
class FileHistoryStorage(
  val baseDir: File,
  val json: Json,
  val clientCleanup: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : WritableHistoryStorage, ObservableHistoryStorage, Disposable {

  val historyConfiguration: HistoryConfiguration = historyConfiguration {
    decimalDataSeries(DataSeriesId(10), TextKey("val1", "Value 1"), HistoryUnit("kg"))
    decimalDataSeries(DataSeriesId(11), TextKey("val2", "Value 2"), HistoryUnit("cm"))
    decimalDataSeries(DataSeriesId(12), TextKey("val3", "Value 3"), HistoryUnit("C"))
  }


  internal val downSamplingService: DownSamplingService = DownSamplingService(this)
  val historyBucketUpdates: MutableList<HistoryBucket> = mutableListOf()

  init {
    require(baseDir.isDirectory && baseDir.exists()) {
      "Invalid base dir: <${baseDir.absolutePath}>"
    }
  }

  /**
   * Returns the file for the given descriptor
   */
  fun getFile(descriptor: HistoryBucketDescriptor): File {
    val parentFile = getParentFile(descriptor)
    return File(parentFile, getFileName(descriptor))
  }

  fun scheduleDownSampling() {
    downSamplingService.scheduleDownSampling(this)
  }

  /**
   * Returns the file name with a `.json` appendix
   */
  fun getFileName(descriptor: HistoryBucketDescriptor): String {
    val startInUtc = millisToUtc(descriptor.start)
    when (descriptor.bucketRange) {
      HistoryBucketRange.HundredMillis -> {
        return startInUtc.millis.toString() + ".json"
      }

      HistoryBucketRange.FiveSeconds -> {
        return startInUtc.second.toString() + ".json"
      }

      HistoryBucketRange.OneMinute -> {
        return startInUtc.minute.toString() + ".json"
      }

      HistoryBucketRange.TenMinutes -> {
        return startInUtc.minute.toString() + ".json"
      }

      HistoryBucketRange.OneHour -> {
        return startInUtc.hour.toString() + ".json"
      }

      HistoryBucketRange.SixHours -> {
        return startInUtc.hour.toString() + ".json"
      }

      HistoryBucketRange.OneDay -> {
        return startInUtc.dayOfMonth.toString() + ".json"
      }

      HistoryBucketRange.ThirtyDays -> {
        return startInUtc.monthValue.toString() + ".json"
      }

      HistoryBucketRange.OneQuarter -> {
        return startInUtc.monthValue.toString() + ".json"
      }

      HistoryBucketRange.OneYear -> {
        return startInUtc.year.toString() + ".json"
      }

      HistoryBucketRange.FiveYears -> {
        return startInUtc.year.toString() + ".json"
      }

      HistoryBucketRange.ThirtyYears -> {
        return startInUtc.year.toString() + ".json"
      }

      HistoryBucketRange.NinetyYears -> {
        return startInUtc.year.toString() + ".json"
      }

      HistoryBucketRange.SevenHundredTwentyYears -> {
        return startInUtc.year.toString() + ".json"
      }

      else -> {
        throw Exception("Bucket Range of descriptor could not be found: $descriptor")
      }
    }
  }


  private val observers = mutableListOf<HistoryObserver>()

  override fun observe(observer: HistoryObserver) {
    observers.add(observer)
  }

  override fun get(descriptor: HistoryBucketDescriptor): HistoryBucket? {
    val file = getFile(descriptor)
    if (file.exists().not()) return null
    val fileContent = file.readText()

    return json.decodeFromString<SerializableHistoryBucket>(fileContent).toHistoryBucket()
  }

  override fun get(descriptors: List<HistoryBucketDescriptor>): List<HistoryBucket> {
    val buckets: MutableList<HistoryBucket> = emptyList<HistoryBucket>().toMutableList()
      descriptors.forEach {
        get(it)?.let { bucket -> buckets.add(bucket) }
      }
    return buckets
  }

  override fun storeWithoutCache(bucket: HistoryBucket, updateInfo: HistoryUpdateInfo) {
    val descriptor = bucket.descriptor
    val parentFile = getParentFile(descriptor)
    parentFile.mkdirs()
    val file = File(parentFile, getFileName(descriptor))


    val serializableHistoryBucket = bucket.toSerializable()

    file.writeTextWithRename(json.encodeToString(serializableHistoryBucket))

    historyBucketUpdates.add(bucket)

    observers.fastForEach {
      it(descriptor, updateInfo)
    }
  }

  fun getUpdates() : MutableList<HistoryBucket> {
    val copy = historyBucketUpdates.toMutableList()
    return copy.also { historyBucketUpdates.clear() }
  }

  override fun delete(descriptor: HistoryBucketDescriptor) {
    val parentFile = getParentFile(descriptor)
    parentFile.mkdirs()
    val file = File(parentFile, getFileName(descriptor))

    file.delete()
    observers.fastForEach {
      it(descriptor, HistoryUpdateInfo.from(descriptor))
    }
  }

  private val disposeSupport = DisposeSupport()

  override fun onDispose(action: () -> Unit) {
    disposeSupport.onDispose(action)
  }

  override fun dispose() {
    downSamplingService.dispose()
    clientCleanup.cancel("Disposing clientCleanUp coroutine")
    disposeSupport.dispose()
  }


  /** returns the parent file*/
  fun getParentFile(descriptor: HistoryBucketDescriptor): File {
    val date = millisToUtc(descriptor.start)
    when (descriptor.bucketRange) {
      HistoryBucketRange.HundredMillis -> {
        return getSecondsDir(descriptor.bucketRange, date.year, date.monthValue, date.dayOfMonth, date.hour, date.minute, date.second)
      }

      HistoryBucketRange.FiveSeconds -> {
        return getMinutesDir(descriptor.bucketRange, date.year, date.monthValue, date.dayOfMonth, date.hour, date.minute)
      }

      HistoryBucketRange.OneMinute -> {
        return getHourDir(descriptor.bucketRange, date.year, date.monthValue, date.dayOfMonth, date.hour)
      }

      HistoryBucketRange.TenMinutes -> {
        return getHourDir(descriptor.bucketRange, date.year, date.monthValue, date.dayOfMonth, date.hour)
      }

      HistoryBucketRange.OneHour -> {
        return getDayDir(descriptor.bucketRange, date.year, date.monthValue, date.dayOfMonth)
      }

      HistoryBucketRange.SixHours -> {
        return getDayDir(descriptor.bucketRange, date.year, date.monthValue, date.dayOfMonth)
      }

      HistoryBucketRange.OneDay -> {
        return getMonthDir(descriptor.bucketRange, date.year, date.monthValue)
      }

      HistoryBucketRange.ThirtyDays -> {
        return getYearDir(descriptor.bucketRange, date.year)
      }

      HistoryBucketRange.OneQuarter -> {
        return getYearDir(descriptor.bucketRange, date.year)
      }

      HistoryBucketRange.OneYear -> {
        return getBucketRangeDir(descriptor.bucketRange)
      }

      HistoryBucketRange.FiveYears -> {
        return getBucketRangeDir(descriptor.bucketRange)
      }

      HistoryBucketRange.ThirtyYears -> {
        return getBucketRangeDir(descriptor.bucketRange)
      }

      HistoryBucketRange.NinetyYears -> {
        return getBucketRangeDir(descriptor.bucketRange)
      }

      HistoryBucketRange.SevenHundredTwentyYears -> {
        return getBucketRangeDir(descriptor.bucketRange)
      }

      else -> {
        throw Exception("Bucket Range of descriptor could not be found!")
      }
    }
  }


  fun getSecondsDir(bucketRange: HistoryBucketRange, year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): File {
    return File("$baseDir/$bucketRange/$year/$month/$day/$hour/$minute", second.toString())
  }

  fun getMinutesDir(bucketRange: HistoryBucketRange, year: Int, month: Int, day: Int, hour: Int, minute: Int): File {
    return File("$baseDir/$bucketRange/$year/$month/$day/$hour", minute.toString())
  }

  fun getHourDir(bucketRange: HistoryBucketRange, year: Int, month: Int, day: Int, hour: Int): File {
    return File("$baseDir/$bucketRange/$year/$month/$day", hour.toString())
  }

  fun getDayDir(bucketRange: HistoryBucketRange, year: Int, month: Int, day: Int): File {
    return File("$baseDir/$bucketRange/$year/$month", day.toString())
  }

  fun getMonthDir(bucketRange: HistoryBucketRange, year: Int, month: Int): File {
    return File("$baseDir/$bucketRange/$year", month.toString())
  }

  fun getYearDir(bucketRange: HistoryBucketRange, year: Int): File {
    return File("$baseDir/$bucketRange", year.toString())
  }

  fun getBucketRangeDir(bucketRange: HistoryBucketRange): File {
    return File("$baseDir/$bucketRange")
  }

}


