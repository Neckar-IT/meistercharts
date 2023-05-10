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
package com.meistercharts.time

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents a year
 */
//@JvmInline
@Serializable
@Deprecated("use [com.soywiz.klock.Year] year instead")
@JvmInline
value class Year(val year: Int) : Comparable<Year> {
  override operator fun compareTo(other: Year): Int {
    return year.compareTo(other.year)
  }

  override fun toString(): String {
    return "$year"
  }

  /**
   * Adds the given number
   */
  operator fun plus(additionalYears: Int): Year {
    return Year(year + additionalYears)
  }

  @Suppress("ObjectPropertyName")
  companion object {
    val _2020: Year = Year(2020)
    val _2021: Year = Year(2021)
    val _2022: Year = Year(2022)
    val _2023: Year = Year(2023)
    val _2024: Year = Year(2024)
    val _2025: Year = Year(2025)

    fun of(year: Int): Year {
      return Year(year)
    }
  }
}
