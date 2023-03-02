package com.meistercharts.history

import kotlin.jvm.JvmInline

/**
 * Represents a timestamp index
 */
@JvmInline
value class TimestampIndex(val value: Int) : Comparable<TimestampIndex> {

  override fun compareTo(other: TimestampIndex): Int {
    return value.compareTo(other.value)
  }

  operator fun compareTo(other: Int): Int {
    return value.compareTo(other)
  }

  operator fun plus(other: Int): TimestampIndex {
    return TimestampIndex(value + other)
  }

  operator fun inc(): TimestampIndex {
    return TimestampIndex(value + 1)
  }

  operator fun dec(): TimestampIndex {
    return TimestampIndex(value - 1)
  }

  /**
   * Returns true if the index is negative
   */
  fun isNegative(): Boolean {
    return value == -1
  }

  override fun toString(): String {
    return value.toString()
  }


  companion object {
    val zero: TimestampIndex = TimestampIndex(0)
    val one: TimestampIndex = TimestampIndex(1)
    val two: TimestampIndex = TimestampIndex(2)
    val three: TimestampIndex = TimestampIndex(3)
    val four: TimestampIndex = TimestampIndex(4)
  }
}
