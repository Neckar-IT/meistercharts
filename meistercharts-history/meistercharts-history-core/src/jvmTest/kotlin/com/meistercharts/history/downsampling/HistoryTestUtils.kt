package com.meistercharts.history.downsampling

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfigurationOnlyDecimals
import com.meistercharts.history.historyConfigurationOnlyEnums
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.chunk
import it.neckar.open.formatting.formatUtc
import it.neckar.open.i18n.TextKey
import it.neckar.open.unit.si.ms


/**
 * A time stamp that can be used for tests
 */
val nowForTests: Double = 1.5900732415E12.also {
  assertThat(it.formatUtc()).isEqualTo("2020-05-21T15:00:41.500")
}

/**
 * Creates a chunk with demo data
 */
fun createDemoChunkOnlyDecimals(
  descriptor: HistoryBucketDescriptor,
  decimalsDataSeriesCount: Int = 3,
  /**
   * Provides the significand value for a given data series index and timestamp index
   */
  decimalValuesProvider: (dataSeriesIndex: DecimalDataSeriesIndex, timestampIndex: TimestampIndex) -> Double,
): HistoryChunk {
  val timestampsCount = descriptor.bucketRange.entriesCount
  @ms val distance = descriptor.bucketRange.samplingPeriod.distance

  val historyConfiguration = historyConfigurationOnlyDecimals(decimalsDataSeriesCount) { dataSeriesIndex ->
    decimalDataSeries(
      DataSeriesId(1000 + dataSeriesIndex.value), TextKey("val$dataSeriesIndex", "Value $dataSeriesIndex")
    )
  }

  return historyConfiguration.chunk(timestampsCount) { timestampIndex ->
    addDecimalValues(descriptor.start + distance * timestampIndex.value) { dataSeriesIndex ->
      decimalValuesProvider(dataSeriesIndex, timestampIndex)
    }
  }
}

fun createDemoChunkOnlyEnums(
  descriptor: HistoryBucketDescriptor,
  enumsDataSeriesCount: Int = 3,
  /**
   * Provides the significand value for a given data series index and timestamp index
   */
  enumValuesProvider: (dataSeriesIndex: EnumDataSeriesIndex, timestampIndex: TimestampIndex) -> HistoryEnumSet,
): HistoryChunk {
  val timestampsCount = descriptor.bucketRange.entriesCount
  @ms val distance = descriptor.bucketRange.samplingPeriod.distance

  val historyConfiguration = historyConfigurationOnlyEnums(enumsDataSeriesCount) { dataSeriesIndex ->
    enumDataSeries(
      DataSeriesId(1000 + dataSeriesIndex.value), TextKey("val$dataSeriesIndex"), createDemoEnumConfiguration()
    )
  }

  return historyConfiguration.chunk(timestampsCount) { timestampIndex ->
    addEnumValues(descriptor.start + distance * timestampIndex.value) { dataSeriesIndex ->
      enumValuesProvider(dataSeriesIndex, timestampIndex)
    }
  }
}

/**
 * Creates a demo enumeration with the given amount of options
 */
fun createDemoEnumConfiguration(optionsCount: Int = 3): HistoryEnum {
  return HistoryEnum.create("demo Enum", List(optionsCount) { TextKey.simple("EnumOption $it") })
}
