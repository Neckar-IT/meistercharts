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
package com.meistercharts.history.generator

import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.WritableHistoryStorage
import com.meistercharts.history.createDefaultHistoryConfiguration
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.chunk
import com.meistercharts.history.valueAt
import com.meistercharts.time.TimeRange
import it.neckar.open.annotations.TestOnly
import it.neckar.open.formatting.formatUtc
import it.neckar.open.kotlin.lang.requireFinite
import it.neckar.open.provider.MultiProvider
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.si.ms

/**
 * Generates [HistoryChunk]s with sample data.
 */
class HistoryChunkGenerator(
  /**
   * The place where to store the chunks
   */
  val historyStorage: WritableHistoryStorage,

  /**
   * The sampling period to be used to compute the timestamps
   */
  val samplingPeriod: SamplingPeriod,

  /**
   * The generators to be used to compute the values - one generator for one (decimal) data series
   */
  val decimalValueGenerators: MultiProvider<DecimalDataSeriesIndex, DecimalValueGenerator>,

  /**
   * The enum value generators
   */
  val enumValueGenerators: MultiProvider<EnumDataSeriesIndex, EnumValueGenerator>,

  /**
   * The generators for the reference entry data series
   */
  val referenceEntryGenerators: MultiProvider<ReferenceEntryDataSeriesIndex, ReferenceEntryGenerator>,

  /**
   * Returns the statuses for the provided data series
   */
  val referenceEntryStatusProvider: (referenceEntryId: ReferenceEntryId, millis: @ms Double) -> HistoryEnumSet,

  /**
   * Provides the reference entry data
   */
  val referenceEntriesDataMap: ReferenceEntriesDataMap,

  /**
   * The history configuration that is used as base for the generated chunks.
   * If the history configuration should be created automatically, use the secondary constructor (with lists) instead
   */
  override val historyConfiguration: HistoryConfiguration,
) : HistoryChunkProvider {
  @TestOnly
  constructor(
    historyStorage: WritableHistoryStorage,
    samplingPeriod: SamplingPeriod,

    /**
     * The generators to be used to compute the values - one generator for one data series
     */
    decimalValueGenerators: List<DecimalValueGenerator>,
    enumValueGenerators: List<EnumValueGenerator>,
    referenceEntryGenerators: List<ReferenceEntryGenerator>,
    referenceEntryStatusProvider: (referenceEntryId: ReferenceEntryId, millis: @ms Double) -> HistoryEnumSet = { _, _ -> HistoryEnumSet.NoValue },

    historyConfiguration: HistoryConfiguration = createDefaultHistoryConfiguration(decimalValueGenerators.size, enumValueGenerators.size, referenceEntryGenerators.size),
  ) : this(
    historyStorage = historyStorage,
    samplingPeriod = samplingPeriod,
    decimalValueGenerators = MultiProvider.forListModulo(decimalValueGenerators),
    enumValueGenerators = MultiProvider.forListModulo(enumValueGenerators),
    referenceEntryGenerators = MultiProvider.forListModulo(referenceEntryGenerators),
    referenceEntryStatusProvider = referenceEntryStatusProvider,

    referenceEntriesDataMap = ReferenceEntriesDataMap.generated,

    historyConfiguration = historyConfiguration,
  ) {
    require(decimalValueGenerators.size == historyConfiguration.decimalDataSeriesCount) {
      "Invalid decimal value generators size. Was ${decimalValueGenerators.size} but require ${historyConfiguration.decimalDataSeriesCount}"
    }
    require(enumValueGenerators.size == historyConfiguration.enumDataSeriesCount) {
      "Invalid enum value generators size. Was ${enumValueGenerators.size} but require ${historyConfiguration.enumDataSeriesCount}"
    }
    require(referenceEntryGenerators.size == historyConfiguration.referenceEntryDataSeriesCount) {
      "Invalid reference entry value generators size. Was ${referenceEntryGenerators.size} but require ${historyConfiguration.referenceEntryDataSeriesCount}"
    }

    lastCreatedTimeStamp = nowMillis() //set initial time to now - helps with creation of initial data when using a virtual now provider
  }

  val decimalDataSeriesCount: Int
    get() = historyConfiguration.decimalDataSeriesCount

  val enumDataSeriesCount: Int
    get() = historyConfiguration.enumDataSeriesCount

  val referenceEntryDataSeriesCount: Int
    get() = historyConfiguration.referenceEntryDataSeriesCount


  val totalDataSeriesCount: Int
    get() = historyConfiguration.totalDataSeriesCount

  /**
   * The timestamp of the last created sample
   */
  var lastCreatedTimeStamp: @ms Double? = null
    private set

  /**
   * Creates a [HistoryChunk] that contains samples for timestamps from [lastCreatedTimeStamp] to [nowMillis] with each timestamp being [samplingPeriod] apart.
   *
   * @return null if [historyStorage] already contains samples for the computed timestamps or if no value generator are defined
   */
  override fun next(until: @ms Double): HistoryChunk? {
    if (totalDataSeriesCount < 1) {
      return null
    }
    val timestamps = mutableListOf<@ms Double>()

    @ms val lastTimestamp = lastCreatedTimeStamp
    if (lastTimestamp == null) {
      timestamps.add(until)
    } else {
      @ms var timestampToAdd = lastTimestamp + samplingPeriod.distance
      timestampToAdd.requireFinite()
      while (timestampToAdd <= until) {
        timestamps.add(timestampToAdd)
        timestampToAdd += samplingPeriod.distance
      }
    }
    return generate(timestamps)
  }

  /**
   * Creates a [HistoryChunk] that contains samples for the given time range.
   *
   * * Start of the time range is inclusive
   * * end of the time range is exclusive
   *
   * @return null if [historyStorage] already contains samples for the time range or if no value generator are defined
   */
  fun forTimeRange(timeRange: TimeRange): HistoryChunk? {
    if (totalDataSeriesCount < 1) {
      return null
    }

    @ms val timestamps = mutableListOf<@ms Double>()
    @ms var timestampToAdd = timeRange.start
    while (timestampToAdd < timeRange.end) {
      timestamps.add(timestampToAdd.requireFinite())
      timestampToAdd += samplingPeriod.distance
    }

    return generate(timestamps)
  }

  /**
   * Creates a [HistoryChunk] that contains a sample for the current time.
   *
   * @return null if [historyStorage] already contains a sample for the computed timestamp
   */
  fun forNow(): HistoryChunk? {
    return generate(listOf(nowMillis()))
  }

  private fun generate(timestamps: List<@ms @IsFinite Double>): HistoryChunk? {
    if (timestamps.isEmpty()) {
      return null
    }
    if (totalDataSeriesCount < 1) {
      return null
    }

    val chunk = historyConfiguration.chunk(timestamps.size) { timestampIndex ->
      @IsFinite @ms val timestamp = timestamps[timestampIndex.value].requireFinite()

      addValues(
        timestamp = timestamp,
        decimalValuesProvider = { dataSeriesIndex: DecimalDataSeriesIndex -> decimalValueGenerators.valueAt(dataSeriesIndex).generate(timestamp) },
        enumValuesProvider = { dataSeriesIndex ->
          val historyEnum = historyConfiguration.enumConfiguration.getEnum(dataSeriesIndex)
          enumValueGenerators.valueAt(dataSeriesIndex).generate(timestamp, historyEnum)
        },
        referenceEntryIdProvider = { dataSeriesIndex ->
          val referenceEntryId = referenceEntryGenerators.valueAt(dataSeriesIndex).generate(timestamp)
          referenceEntryId
        },
        referenceEntryStatusProvider = { referenceEntryId: ReferenceEntryId ->
          referenceEntryStatusProvider(referenceEntryId, timestamp)
        },
        referenceEntriesDataMap = referenceEntriesDataMap,
      )
    }

    lastCreatedTimeStamp = maxOf(chunk.lastTimeStamp(), lastCreatedTimeStamp ?: Double.MIN_VALUE)
    return chunk
  }
}
