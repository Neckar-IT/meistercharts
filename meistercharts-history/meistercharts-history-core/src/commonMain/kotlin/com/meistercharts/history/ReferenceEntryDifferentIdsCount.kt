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
