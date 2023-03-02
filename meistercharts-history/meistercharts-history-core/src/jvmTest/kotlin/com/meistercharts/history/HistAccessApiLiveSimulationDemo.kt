package com.meistercharts.history

import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.HistoryValues
import com.meistercharts.history.impl.RecordingType
import it.neckar.open.collections.DoubleArray2
import it.neckar.open.collections.IntArray2
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
class HistAccessApiLiveSimulationDemo {
  lateinit var historyStorage: InMemoryHistoryStorage

  @BeforeEach
  fun setUp() {
    historyStorage = InMemoryHistoryStorage()
  }

  @Test
  fun testSimulation() {
    val resolution = SamplingPeriod.EveryMillisecond
    historyStorage.naturalSamplingPeriod = resolution
    val bucketRange = resolution.toHistoryBucketRange()

    println("Bucket range: $bucketRange, Duration: ${bucketRange.duration} ms")

    val historyConfig = historyConfiguration {
      decimalDataSeries(DataSeriesId(10), TextKey.simple("DS 1"))
      decimalDataSeries(DataSeriesId(20), TextKey.simple("DS 2"))
    }

    for (i in 0..1000 step 10) {
      //Add data for all 10 milliseconds
      val values = HistoryValues(
        decimalValues = DoubleArray2(2, 1, i * 100.0),
        enumValues = IntArray2(2, 1, i * 100),
        referenceEntryIds = IntArray2(2, 1, i * 100),
        referenceEntriesDataMaps = emptyList()
      )
      val newChunk = HistoryChunk(historyConfig, doubleArrayOf(i.toDouble()), values, RecordingType.Measured)

      historyStorage.storeWithoutCache(newChunk, resolution)
    }

    val result = historyStorage.query(0.0, 1000.0, resolution)

    result.forEach {
      println("Result: ${it.descriptor}")
      println("\t${it.start} - ${it.end}")
      println("\ttimestamps count: ${it.chunk.timeStampsCount}")
    }
  }
}
