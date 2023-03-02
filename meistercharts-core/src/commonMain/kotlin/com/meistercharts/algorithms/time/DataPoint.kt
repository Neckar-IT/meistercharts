package com.meistercharts.algorithms.time

import it.neckar.open.unit.si.ms

/**
 * Contains the data for one point in time
 */
data class DataPoint<T>(
  /**
   * The time for the data point in milliseconds
   */
  @ms
  val time: Double,
  /**
   * The value for that given point in time
   */
  val value: T
) : Comparable<DataPoint<T>> {

  override fun compareTo(other: DataPoint<T>): Int {
    return when {
      time > other.time  -> 1
      time == other.time -> 0
      else               -> -1
    }
  }
}


