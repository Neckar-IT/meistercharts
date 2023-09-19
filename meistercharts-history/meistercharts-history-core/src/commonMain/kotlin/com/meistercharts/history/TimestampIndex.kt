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
@file:Suppress("NOTHING_TO_INLINE")

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

  /**
   * Returns the previous timestamp index.
   */
  inline fun previous(): TimestampIndex {
    return TimestampIndex(value - 1)
  }


  companion object {
    val zero: TimestampIndex = TimestampIndex(0)
    val one: TimestampIndex = TimestampIndex(1)
    val two: TimestampIndex = TimestampIndex(2)
    val three: TimestampIndex = TimestampIndex(3)
    val four: TimestampIndex = TimestampIndex(4)
  }
}
