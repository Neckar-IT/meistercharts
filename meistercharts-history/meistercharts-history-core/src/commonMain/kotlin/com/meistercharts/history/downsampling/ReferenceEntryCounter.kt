package com.meistercharts.history.downsampling

import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.impl.RecordingType

/**
 * Holds count of how often a given [ReferenceEntryId] occurred.
 */
class ReferenceEntryCounter {
  /**
   * Keeps the counts per id. The key is the reference entry id, the value is the count.
   *
   * Every value in this map contains at least 1. There will be no zeros in this map.
   * This map is used to calculate the "most-of-the-time" reference ID.
   *
   * When calculating from [RecordingType.Measured] this will also be used to calculate the count of different IDs
   */
  private val countsPerId: MutableMap<ReferenceEntryId, Int> = mutableMapOf()

  /**
   * Counter for different IDs.
   * This variable is only used when calculating from [RecordingType.Measured]
   */
  private var differentIdsCount: ReferenceEntryDifferentIdsCount = ReferenceEntryDifferentIdsCount.Pending

  /**
   * Will be set to true if there is at least one entry with [ReferenceEntryId.NoValue]
   */
  var containsNoValue: Boolean = false
    private set

  /**
   * Returns the number of different IDs - used for downsampling
   */
  fun differentIdsCount(): @MayBeNoValueOrPending ReferenceEntryDifferentIdsCount {
    if (differentIdsCount != ReferenceEntryDifferentIdsCount.Pending) {
      //the method [addFromCalculated] has been called, use this value
      return differentIdsCount
    }

    //only [addFromMeasured] has been called, calculate the different IDs
    if (countsPerId.isEmpty()) {
      if (containsNoValue) {
        return ReferenceEntryDifferentIdsCount.NoValue
      }

      return ReferenceEntryDifferentIdsCount.Pending
    }

    return ReferenceEntryDifferentIdsCount(countsPerId.size)
  }

  /**
   * Returns the winner that has been measured the most.
   *
   * If there is a tie, one of the winners is selected randomly - depending on the map implementation
   */
  fun winnerMostOfTheTime(): @MayBeNoValueOrPending ReferenceEntryId {
    if (countsPerId.isEmpty()) {
      //No entries!
      return if (containsNoValue) {
        ReferenceEntryId.NoValue
      } else {
        ReferenceEntryId.Pending
      }
    }

    //We have at least one value
    //find the one with the highest count

    //The best key so far
    var bestKey: ReferenceEntryId = ReferenceEntryId.Pending

    //The count of the current best
    var bestCount: Int = -1

    countsPerId.forEach { (key, count) ->
      if (count > bestCount) {
        bestKey = key
        bestCount = count
      }
    }

    return bestKey
  }

  /**
   * Adds the values.
   * If [differentIdsCount] is null, the method delegates to [addFromMeasured].
   * Else it delegates to [addFromCalculated]
   */
  fun add(referenceEntryId: ReferenceEntryId, differentIdsCount: ReferenceEntryDifferentIdsCount?) {
    if (differentIdsCount == null) {
      addFromMeasured(referenceEntryId)
    } else {
      addFromCalculated(referenceEntryId, differentIdsCount)
    }
  }

  /**
   * Adds a value to the counter. Use this method when calculating from a [RecordingType.Measured].
   *
   * Both, counts and most-of-the-time reference IDs are calculated from the provided [referenceEntryId]
   */
  fun addFromMeasured(referenceEntryId: ReferenceEntryId) {
    if (referenceEntryId.isPending()) {
      //Ignore pending values
      return
    }

    if (referenceEntryId.isNoValue()) {
      //Mark as contains no value
      containsNoValue = true
      return
    }

    //Update the count for the given ID, necessary to be able to calculate "most-of-the-time"
    val currentCountForId = countsPerId.getOrElse(referenceEntryId) { 0 }
    countsPerId[referenceEntryId] = currentCountForId + 1
  }

  /**
   * Use this method when calculating the reference counters from a calculated source ([RecordingType.Calculated]).
   * The provided [referenceEntryId] is used to calculate the *most of the time* only.
   * The counts are calculated using [differentIdsCount].
   */
  fun addFromCalculated(
    /**
     * Used to calculate the "most-of-the-time" reference entry id
     */
    referenceEntryId: ReferenceEntryId,
    /**
     * The number of different IDs. Used to calculate the different IDs
     */
    differentIdsCount: ReferenceEntryDifferentIdsCount,
  ) {
    if (referenceEntryId.isPending()) {
      //do nothing with pending
      return
    }

    if (referenceEntryId.isNoValue()) {
      require(differentIdsCount == ReferenceEntryDifferentIdsCount.NoValue) {
        "Inconsistent differentIdsCount. Was $differentIdsCount but expected NoValue"
      }

      containsNoValue = true
      return
    }

    //Calculate the sum over all [differentIdsCount]s

    //Assuming that the ID does not change from one value to another exactly at the boundary of a chunk, we can conclude that most of the time, a single ID is contained in both chunks.
    //Therefore, we remove 1 from the total sum of different values
    if (this.differentIdsCount == ReferenceEntryDifferentIdsCount.zero || lastReferenceEntryId.isPending()) {
      //Special case initially: use the new count
      this.differentIdsCount = differentIdsCount
    } else {
      //check if the most-of-the-time ID has changed

      if (lastReferenceEntryId == referenceEntryId) {
        //The reference ID has not changed. Therefore, we assume the ID occurred in both
        this.differentIdsCount = this.differentIdsCount + (differentIdsCount - 1).atLeastZero()
      } else {
        this.differentIdsCount = this.differentIdsCount.plus(differentIdsCount)
      }
    }

    lastReferenceEntryId = referenceEntryId

    //Add the count to find the most-of-the-time ID
    val currentCountForId = countsPerId.getOrElse(referenceEntryId) { 0 }
    countsPerId[referenceEntryId] = currentCountForId + 1 //just add 1 (not differentIdsCount!)
  }

  /**
   * Remember the last reference entry id - to be able to guess the count
   */
  private var lastReferenceEntryId: ReferenceEntryId = ReferenceEntryId.Pending


  fun reset() {
    countsPerId.clear()
    containsNoValue = false
    differentIdsCount = ReferenceEntryDifferentIdsCount.Pending
  }
}
