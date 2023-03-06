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

import kotlin.jvm.JvmInline

/**
 * Represents the number of different reference entry ids
 */
@JvmInline
value class ReferenceEntryDifferentIdsCount(val value: Int) {
  fun isPending(): Boolean {
    return this == Pending
  }

  fun isNoValue(): Boolean {
    return this == NoValue
  }

  /**
   * Adds two counts
   */
  operator fun plus(other: ReferenceEntryDifferentIdsCount): ReferenceEntryDifferentIdsCount {
    if (this.isPending()) {
      return other
    }
    if (other.isPending()) {
      return this
    }
    if (this.isNoValue()) {
      return other
    }
    if (other.isNoValue()) {
      return this
    }

    return ReferenceEntryDifferentIdsCount(this.value + other.value)
  }

  override fun toString(): String {
    if (isPending()) {
      return "?"
    }

    if (isNoValue()) {
      return "-"
    }

    return value.toString()
  }

  operator fun minus(toSubstract: Int): ReferenceEntryDifferentIdsCount {
    return ReferenceEntryDifferentIdsCount(this.value - toSubstract)
  }

  fun atLeastZero(): ReferenceEntryDifferentIdsCount {
    if (this.value < 0) {
      return zero
    }

    return this
  }

  companion object {
    val zero: ReferenceEntryDifferentIdsCount = ReferenceEntryDifferentIdsCount(value = 0)
    val one: ReferenceEntryDifferentIdsCount = ReferenceEntryDifferentIdsCount(value = 1)

    /**
     * Represents the pending state (-1) - as int value
     */
    const val PendingAsInt: @ReferenceEntryDifferentIdsCountInt Int = Int.MAX_VALUE

    /**
     * Represents the pending state (-1)
     */
    val Pending: ReferenceEntryDifferentIdsCount = ReferenceEntryDifferentIdsCount(value = PendingAsInt)

    /**
     * This value implies that a sample has been taken but the sample does not contain a valid value for the data series.
     */
    const val NoValueAsInt: @ReferenceEntryDifferentIdsCountInt Int = Int.MAX_VALUE - 1

    /**
     * This value implies that a sample has been taken but the sample does not contain a valid value for the data series.
     */
    val NoValue: ReferenceEntryDifferentIdsCount = ReferenceEntryDifferentIdsCount(value = NoValueAsInt)
  }
}


/**
 * Annotation that is used for int values that represent an [ReferenceEntryId]
 */
@Target(AnnotationTarget.TYPE)
annotation class ReferenceEntryDifferentIdsCountInt
