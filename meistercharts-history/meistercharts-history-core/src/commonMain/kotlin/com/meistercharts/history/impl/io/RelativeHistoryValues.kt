package com.meistercharts.history.impl.io

import com.meistercharts.history.impl.HistoryValues
import it.neckar.open.collections.DoubleArray2
import it.neckar.open.collections.IntArray2
import it.neckar.open.kotlin.serializers.DoubleArray2Serializer
import it.neckar.open.kotlin.serializers.IntArray2Serializer
import kotlinx.serialization.Serializable

/**
 * Contains *RELATIVE* history values.
 *
 * This is an optimized variant of [HistoryValues].
 * Only the first value for each data series is absolute. All other values are relative to that one
 *
 * Should not be used directly. Instead, convert to absolute [#makeAbsolute].
 *
 */
@Deprecated("Do not use anymore, does not make sense")
@Serializable()
class RelativeHistoryValues(
  val decimalValues: @Serializable(with = DoubleArray2Serializer::class) DoubleArray2,
  val enumValues: @Serializable(with = IntArray2Serializer::class) IntArray2,
  val referenceEntryHistoryValues: @Serializable(with = IntArray2Serializer::class) IntArray2,
) {

  init {
    //TODO improve require - also check referenceEntryHistoryValues
    require(enumValues.isEmpty || decimalValues.isEmpty || decimalValues.height == enumValues.height) {
      "Different timestampCounts. Significant: ${decimalValues.height} - enumValues: ${enumValues.height}"
    }
  }

  /**
   * The amount of data series
   */
  val decimalsDataSeriesCount: Int
    get() {
      return decimalValues.width
    }

  val enumsDataSeriesCount: Int
    get() {
      return enumValues.width
    }

  val timeStampsCount: Int
    get() {
      //Same as enum values height
      return decimalValues.height
    }

  /**
   * Returns the absolute history values
   */
  @Deprecated("no longer needed, serialize directly")
  fun makeAbsolute(): HistoryValues {
    val absoluteSignificantValues: DoubleArray2 = decimalValues.makeAbsolute()
    val absoluteEnumValues: IntArray2 = enumValues.makeAbsolute()
    val referenceEntryHistoryValues: IntArray2 = referenceEntryHistoryValues.makeAbsolute()

    TODO("not implemented yet!")
    //return HistoryValues(absoluteSignificantValues, absoluteEnumValues, referenceEntryHistoryValues, )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as RelativeHistoryValues

    if (decimalValues != other.decimalValues) {
      return false
    }
    if (enumValues != other.enumValues) {
      return false
    }
    return referenceEntryHistoryValues == other.referenceEntryHistoryValues
  }

  override fun hashCode(): Int {
    var result = decimalValues.hashCode()
    result = 31 * result + enumValues.hashCode()
    result = 30 * result + referenceEntryHistoryValues.hashCode()
    return result
  }
}

/**
 * Converts the int array to an int array with relative values
 */
fun IntArray2.makeAbsolute(): IntArray2 {
  if (width == 0 || height == 0) {
    return IntArray2(width, height, 0)
  }

  val absolute = IntArray2(width, height, 0)

  //Iterate over cols first - we want to calculate relative values for each data series
  for (x in 0 until this.width) {
    //copy the first entry
    absolute[x, 0] = this[x, 0]

    for (y in 1 until this.height) {
      val previous = absolute[x, y - 1] //read the previous *absolute* value
      val current = this[x, y]

      val absoluteValue = current + previous
      absolute[x, y] = absoluteValue
    }
  }

  return absolute
}

fun DoubleArray2.makeAbsolute(): DoubleArray2 {
  if (width == 0 || height == 0) {
    return DoubleArray2(width, height, 0.0)
  }

  val absolute = DoubleArray2(width, height, 0.0)

  //Iterate over cols first - we want to calculate relative values for each data series
  for (x in 0 until this.width) {
    //copy the first entry
    absolute[x, 0] = this[x, 0]

    for (y in 1 until this.height) {
      val previous = absolute[x, y - 1] //read the previous *absolute* value
      val current = this[x, y]

      val absoluteValue = current + previous
      absolute[x, y] = absoluteValue
    }
  }

  return absolute
}

/**
 * Creates a relative history values object
 */
@Deprecated("No longer required")
fun HistoryValues.makeRelative(): RelativeHistoryValues {
  return RelativeHistoryValues(
    decimalHistoryValues.values.makeRelative(),
    enumHistoryValues.values.makeRelative(),
    referenceEntryHistoryValues.values.makeRelative(),
  )
}

/**
 * Returns a copy with relative values (relative to the previous value)
 */
fun IntArray2.makeRelative(): IntArray2 {
  if (width == 0 || height == 0) {
    return IntArray2(width, height, 0)
  }

  val relative = IntArray2(width, height, 0)

  //Iterate over cols first - we want to calculate relative values for each data series
  for (x in 0 until this.width) {
    //copy the first entry
    relative[x, 0] = this[x, 0]

    for (y in 1 until this.height) {
      val previous = this[x, y - 1]
      val current = this[x, y]

      val delta = current - previous
      relative[x, y] = delta
    }
  }

  return relative
}

fun DoubleArray2.makeRelative(): DoubleArray2 {
  if (width == 0 || height == 0) {
    return DoubleArray2(width, height, 0.0)
  }

  val relative = DoubleArray2(width, height, 0.0)

  //Iterate over cols first - we want to calculate relative values for each data series
  for (x in 0 until this.width) {
    //copy the first entry
    relative[x, 0] = this[x, 0]

    for (y in 1 until this.height) {
      val previous = this[x, y - 1]
      val current = this[x, y]

      val delta = current - previous
      relative[x, y] = delta
    }
  }

  return relative
}
