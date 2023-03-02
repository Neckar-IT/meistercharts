package com.meistercharts.history

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents an object value id - that is used in the history.
 */
@Serializable
@JvmInline
value class ReferenceEntryId(val id: Int) {
  fun isPending(): Boolean {
    return this == Pending
  }

  fun isNoValue(): Boolean {
    return this == NoValue
  }

  override fun toString(): String {
    if (isPending()) {
      return "?"
    }

    if (isNoValue()) {
      return "-"
    }

    return id.toString()
  }


  companion object {
    /**
     * Represents the pending state (-1) - as int value
     */
    const val PendingAsInt: @ReferenceEntryIdInt Int = Int.MAX_VALUE

    /**
     * Represents the pending state (-1)
     */
    val Pending: ReferenceEntryId = ReferenceEntryId(PendingAsInt)

    /**
     * This value implies that a sample has been taken but the sample does not contain a valid value for the data series.
     */
    const val NoValueAsInt: @ReferenceEntryIdInt Int = Int.MAX_VALUE - 1

    /**
     * This value implies that a sample has been taken but the sample does not contain a valid value for the data series.
     */
    val NoValue: ReferenceEntryId = ReferenceEntryId(NoValueAsInt)
  }
}

/**
 * Annotation that is used for int values that represent an [ReferenceEntryId]
 */
@Target(AnnotationTarget.TYPE)
annotation class ReferenceEntryIdInt
