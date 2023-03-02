package com.meistercharts.history.downsampling

import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import it.neckar.open.collections.fastForEachIndexed

/**
 * Holds count of how often an ordinal value occurred
 */
class HistoryEnumOrdinalCounter {
  /**
   * Keeps the counts.
   * The ordinal of an enum corresponds to the position within the array
   */
  private val counts: IntArray = IntArray(HistoryEnumSet.maxSupportedEnumValuesCount) { 0 }

  var containsNoValue: Boolean = false


  /**
   * Returns the winner that has been measured the most.
   *
   * If there is a tie, the lowest ordinal will be returned.
   */
  fun winner(): HistoryEnumOrdinal {
    //find the highest count

    //The index of the current best
    var bestIndex = -1

    //The count of the current best
    var bestCount = 0

    counts.fastForEachIndexed { index, count ->
      if (count > bestCount) {
        bestIndex = index
        bestCount = count
      }
    }

    if (bestIndex == -1) {
      //nothing found with count > 0
      if (containsNoValue) {
        return HistoryEnumOrdinal.NoValue
      }

      return HistoryEnumOrdinal.Pending
    }

    return HistoryEnumOrdinal(bestIndex)
  }

  /**
   * Adds a value to the counter
   */
  fun add(historyEnumOrdinal: HistoryEnumOrdinal) {
    if (historyEnumOrdinal.isPending()) {
      //do nothing with pending
      return
    }

    if (historyEnumOrdinal.isNoValue()) {
      containsNoValue = true
      return
    }

    counts[historyEnumOrdinal.value]++
  }

  /**
   * Adds all ordinals from the provided bit set
   */
  fun addAll(enumSet: HistoryEnumSet) {
    require(enumSet.hasValidValue()) {
      "Valid value required but was $enumSet"
    }

    enumSet.fastForSetBits {
      add(it)
    }
  }

  /**
   * Returns the count for the given ordinal.
   * Only for tests
   */
  internal fun count(historyEnumOrdinal: HistoryEnumOrdinal): Int {
    return counts[historyEnumOrdinal.value]
  }

  fun reset() {
    counts.fastForEachIndexed { index, _ ->
      counts[index] = 0
    }
  }
}
