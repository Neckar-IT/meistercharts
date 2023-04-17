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
package com.meistercharts.api.discrete

import com.meistercharts.api.forEnumValueFromJsDouble
import com.meistercharts.history.DefaultReferenceEntriesDataMap
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSetInt
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryIdInt
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.get
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.historyChunk
import it.neckar.open.charting.api.sanitizing.sanitize
import it.neckar.open.collections.binarySearchBy
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.si.ms
import kotlin.jvm.JvmInline

/**
 * Converts the data to a history chunk that can be added to the history storage
 */
fun DiscreteTimelineChartData.toChunk(historyConfiguration: HistoryConfiguration): Pair<HistoryChunk, SamplingPeriod>? {
  val seriesDataCount = this.series.size
  if (seriesDataCount == 0) {
    //empty data - return null
    return null
  }

  check(historyConfiguration.referenceEntryDataSeriesCount == seriesDataCount) {
    "The number of reference entry data series in the history configuration (${historyConfiguration.referenceEntryDataSeriesCount}) does not match the number of data series in the data (${seriesDataCount})"
  }

  //Guess the SamplingPeriod
  @ms val start = findFirstStart()
  @ms val end = findLastEnd()

  require(end > start) { "Invalid start ($start), end ($end)" }

  //find shortest entry duration
  //@ms val shortestEntryDuration = findShortestEntry().duration
  //println("shortestEntryDuration: $shortestEntryDuration ms")
  //@ms val longestEntryDuration = findLongestEntry().duration
  //println("longestEntryDuration: $longestEntryDuration ms")
  //@ms val targetDuration = findAverageEntryDuration()

  @ms val defaultEntryDuration = this.defaultEntryDuration
  println("defaultEntryDuration $defaultEntryDuration ms")

  //We want the shorted entry to span at least three samples
  val samplingPeriod = SamplingPeriod.withMaxDistance(defaultEntryDuration * 4)

  //
  //Generate the timestamps
  //

  @ms val timeStamps = series.allTimestampsForChunk(start, end, samplingPeriod)


  var referenceIdAsInt = 1000
  fun nextReferenceId(): ReferenceEntryId {
    referenceIdAsInt++
    return ReferenceEntryId(referenceIdAsInt)
  }


  val chunk = historyChunk(historyConfiguration) {
    timeStamps.forEachIndexed { timestampIndexAsInt, timestamp: @ms Double ->
      //Collect all values for the provided timestamp
      val referenceEntryValues: @ReferenceEntryIdInt IntArray = IntArray(seriesDataCount) { ReferenceEntryId.PendingAsInt }
      val referenceEntryStatuses: @HistoryEnumSetInt IntArray = IntArray(seriesDataCount) { HistoryEnumSet.PendingAsInt }
      val referenceEntryDataMap = mutableMapOf<ReferenceEntryId, ReferenceEntryData>()

      seriesDataCount.fastFor { dataSeriesIndexAsInt ->
        val seriesIndex = ReferenceEntryDataSeriesIndex(dataSeriesIndexAsInt)

        val seriesData = series[seriesIndex]
        val entryIndexSearchResult = seriesData.findEntryIndexForTimestamp(timestamp) //the entry that is relevant for the timestamp

        //Fetch the entry
        val entry: DiscreteDataEntry? = seriesData.entries.getOrNull(entryIndexSearchResult.index)

        val timestampIndex = TimestampIndex(timestampIndexAsInt)
        val previousTimestampIndex = timestampIndex.previous()


        if (entry == null) {
          referenceEntryValues[dataSeriesIndexAsInt] = ReferenceEntryId.NoValueAsInt
          referenceEntryStatuses[dataSeriesIndexAsInt] = HistoryEnumSet.NoValueAsInt
        } else {
          //Entry has been found - create valid entries
          val statusEnumSet = HistoryEnumSet.forEnumValueFromJsDouble(entry.status.sanitize())

          //Check if the previous entry has the same values - then reuse the ID
          val referenceEntryId: ReferenceEntryId = when {
            previousTimestampIndex.isNegative() -> {
              nextReferenceId()
            }

            else -> {
              val previousStatus = this@historyChunk.getReferenceEntryStatus(seriesIndex, previousTimestampIndex)
              val previousReferenceEntryId: ReferenceEntryId = this@historyChunk.getReferenceEntryId(dataSeriesIndex = seriesIndex, previousTimestampIndex)
              val previousData = this@historyChunk.getReferenceEntryData(previousReferenceEntryId)

              //necessary to create a new one?
              val newIdRequired = previousStatus != statusEnumSet || previousData?.label?.fallbackText != entry.label
              if (newIdRequired) {
                nextReferenceId()
              } else {
                previousReferenceEntryId
              }
            }
          }

          referenceEntryValues[dataSeriesIndexAsInt] = referenceEntryId.id
          referenceEntryStatuses[dataSeriesIndexAsInt] = statusEnumSet.bitset
          referenceEntryDataMap[referenceEntryId] = ReferenceEntryData(
            referenceEntryId,
            label = TextKey.simple(entry.label),
            start = entry.start,
            end = entry.end,
            payload = null,
          )
        }
      }

      addReferenceEntryValues(timestamp, referenceEntryValues, null, referenceEntryStatuses, DefaultReferenceEntriesDataMap(referenceEntryDataMap))
    }
  }

  return Pair(chunk, samplingPeriod)
}

/**
 * Returns the first start of the first entry - over all series
 */
fun DiscreteTimelineChartData.findFirstStart(): @ms Double {
  @ms val start = this.series
    .filter { it.isNotEmpty() }
    .minOf {
      it.entries.first().start
    }
  return start
}

/**
 * Returns the entry with the shorted duration
 */
fun DiscreteTimelineChartData.findShortestEntry(): @Sorted(by = "start") DiscreteDataEntry {
  return this.series
    .asSequence()
    .flatMap { it.entries.asSequence() }
    .minBy {
      it.duration
    }
}

fun DiscreteTimelineChartData.findLongestEntry(): @Sorted(by = "start") DiscreteDataEntry {
  return this.series
    .asSequence()
    .flatMap { it.entries.asSequence() }
    .maxBy {
      it.duration
    }
}

fun DiscreteTimelineChartData.findAverageEntryDuration(): @ms Double {
  return this.series
    .asSequence()
    .flatMap { it.entries.asSequence() }
    .map { it.duration }
    .average()
}

/**
 * Returns the last end value - over all series and entries
 */
fun DiscreteTimelineChartData.findLastEnd(): @ms Double {
  @ms val end = this.series
    .filter { it.isNotEmpty() }
    .maxOf {
      it.entries.last().end
    }
  return end
}

/**
 * Generates a sequence containing all timestamps for the chunk
 */
fun Array<DiscreteDataEntriesForDataSeries>.allTimestampsForChunk(
  start: @ms @Inclusive Double,
  end: @ms @Inclusive Double,
  samplingPeriod: SamplingPeriod,
): Sequence<@ms @Sorted Double> {
  //The timestamps generated from the entry bounds
  @ms val timestampsEntryBounds = timestampsFromEntryBounds()

  //All timestamps that are required for the "sampling"
  @ms val timestampsFromSamplingPeriodDistance = timestampsForSamplingPeriod(start, end, samplingPeriod)

  return (timestampsEntryBounds + timestampsFromSamplingPeriodDistance).sorted().distinct()
}

/**
 * Generates a sequence of timestamps for a given sampling period within a specified time range.
 *
 * @param start The start time of the range in milliseconds, inclusive.
 * @param end The end time of the range in milliseconds, inclusive.
 * @param samplingPeriod The sampling period to generate the timestamps.
 * @return A sorted sequence of timestamps in milliseconds within the specified time range.
 */
fun timestampsForSamplingPeriod(start: @ms @Inclusive Double, end: @ms @Inclusive Double, samplingPeriod: SamplingPeriod): Sequence<@ms @Sorted Double> {
  return generateSequence(start) { lastTimestamp ->
    @ms val next = lastTimestamp + samplingPeriod.distance

    when {
      next <= end -> { //next value is valid, use it
        next
      }

      lastTimestamp >= end -> { //the last generated timestamp has been the end - therefore, we should terminate
        null
      }

      next >= end -> { //the newly generated value is too large, return end
        end
      }

      else -> {
        null
      }
    }
  }
}

/**
 * Creates a sequence that contains all start/end for all entries of all data series
 */
fun Array<DiscreteDataEntriesForDataSeries>.timestampsFromEntryBounds(): Sequence<@ms Double> {
  return asSequence()
    .flatMap { it.entries.asSequence() }
    .flatMap { sequenceOf(it.start, it.end) }
    .sorted()
    .distinct()
}

inline fun DiscreteDataEntriesForDataSeries.isNotEmpty(): Boolean {
  return entries.isNotEmpty()
}

inline val DiscreteDataEntry.duration: @ms Double
  get() {
    return end - start
  }


/**
 * Returns the best index for the timestamp
 */
fun DiscreteDataEntriesForDataSeries.findEntryIndexForTimestamp(timestamp: @ms Double): EntryIndexSearchResult {
  return this.entries.binarySearchBy(timestamp) {
    it.start
  }.let {
    when {
      it.found -> {
        EntryIndexSearchResult(it.index)//direct hit on start
      }

      else -> {
        //No direct hit
        if (it.nearIndex <= 0) {
          return EntryIndexSearchResult.NotFound //too low, nothing there
        }

        if (it.nearIndex > entries.size) {
          return EntryIndexSearchResult.NotFound //too large, nothing there
        }

        val entry = entries[it.nearIndex - 1] //check the previous entry, which might contain the timestamp
        return if (entry.contains(timestamp)) {
          EntryIndexSearchResult(it.nearIndex - 1)
        } else {
          EntryIndexSearchResult.NotFound
        }
      }
    }
  }
}

@JvmInline
value class EntryIndexSearchResult(
  val index: Int,
) {
  val found: Boolean
    get() {
      return index >= 0
    }

  companion object {
    val NotFound: EntryIndexSearchResult = EntryIndexSearchResult(-1)
  }
}

/**
 * Returns true if this entry contains the provided timestamp
 */
fun DiscreteDataEntry.contains(timestamp: @ms Double): Boolean {
  return this.start <= timestamp && this.end > timestamp
}
