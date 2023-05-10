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

import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachReversed
import it.neckar.open.unit.si.ms
import com.meistercharts.history.impl.HistoryChunk

/**
 * Describes one exact value within the history.
 * Represents a result of a search within the history
 */
data class HistorySearchResult(val chunk: HistoryChunk, val timeStampIndex: TimestampIndex) {
}

/**
 * Returns the best entry for the given time.
 *
 * Attention: values equal to [HistoryChunk.Pending] will not be found
 */
fun List<HistoryBucket>.search(time: @ms Double, searchConstraint: SearchConstraint): HistorySearchResult? {
  if (isEmpty()) {
    return null
  }

  when (searchConstraint) {
    is Exact -> {
      //Look for an exact hit
      fastForEach { historyBucket ->
        historyBucket.chunk.bestTimestampIndexFor(time).let {
          if (it.found) {
            if (historyBucket.chunk.isPending(TimestampIndex(it.index))) {
              return null
            }
            return HistorySearchResult(historyBucket.chunk, TimestampIndex(it.index))
          }
        }
      }
    }

    is AndBefore -> {
      @ms val earliestTimestamp = time - searchConstraint.maxDistance
      //Look from the last entries
      fastForEachReversed { historyBucket ->
        historyBucket.chunk.bestTimestampIndexFor(time).let {
          if (it.found) {
            //Exact hit -> find non-pending sample
            val nonPendingSearchResult = searchNonPendingAndBefore(historyBucket.chunk, TimestampIndex(it.index), earliestTimestamp) ?: return null //maximum distance exceeded
            if (nonPendingSearchResult.timeStampIndex < 0) {
              //continue looking in other buckets before
              return@fastForEachReversed
            }
            return nonPendingSearchResult
          }

          //The index of the element before
          val indexBefore = it.nearIndex - 1
          if (indexBefore < 0) {
            //The index is before this bucket - continue looking in other buckets before
            return@fastForEachReversed
          }

          val nonPendingSearchResult = searchNonPendingAndBefore(historyBucket.chunk, TimestampIndex(indexBefore), earliestTimestamp) ?: return null //maximum distance exceeded
          if (nonPendingSearchResult.timeStampIndex < 0) {
            //continue looking in other buckets before
            return@fastForEachReversed
          }
          return nonPendingSearchResult
        }
      }
    }
  }

  return null
}

/**
 * Returns a result that is not pending. Will return the value for the given [startIndex] if that value is not pending.
 * Else the latest value before will be returned that is not pending and younger than [earliestTimestamp].
 * @param chunk the chunk to be searched
 * @param startIndex the index to start the search from
 * @param earliestTimestamp the earliest possible timestamp; timestamps before this timestamp will be ignored
 */
private fun searchNonPendingAndBefore(chunk: HistoryChunk, startIndex: TimestampIndex, earliestTimestamp: @ms Double): HistorySearchResult? {
  var index = startIndex
  if (chunk.timestampCenter(index) < earliestTimestamp) {
    //before earliest timestamp, return immediately
    return null
  }

  //Find the first non pending entry
  while (chunk.isPending(index)) {
    --index
    if (index < 0) {
      //the chunk does not contain a non-pending entry
      return HistorySearchResult(chunk, index)
    }
    if (chunk.timestampCenter(index) < earliestTimestamp) {
      //before earliest timestamp, return immediately
      return null
    }
  }
  return HistorySearchResult(chunk, index)
}

sealed class SearchConstraint {
}

/**
 * Describes an exact hit
 */
data object Exact : SearchConstraint()

/**
 * Constraints the search to the given time stamp and the closest value before
 */
class AndBefore(val maxDistance: @ms Double) : SearchConstraint() {

}
