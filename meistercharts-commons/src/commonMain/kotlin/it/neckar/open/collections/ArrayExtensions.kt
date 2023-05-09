package it.neckar.open.collections

import it.neckar.open.annotations.Slow
import it.neckar.open.kotlin.lang.wrapAround
import it.neckar.open.unit.other.Exclusive
import it.neckar.open.unit.other.Inclusive

/**
 *
 */


/**
 * Returns the n th element from this [Array]. Uses modulo if the index is larger than the size of the array
 */
fun <T> Array<T>.getModulo(index: Int): T {
  //This calculation produces a "wrap around" effect for negative indices
  return this[index.wrapAround(size)]
}

fun DoubleArray.getModulo(index: Int): Double {
  return this[index.wrapAround(size)]
}

/**
 * Maps each element of this array to an int
 */
fun <T> Array<T>.mapToIntArray(function: (T) -> Int): IntArray {
  return IntArray(size) {
    val value = get(it)
    function(value)
  }
}

/**
 * An [ByteArray] of size 0
 */
private val emptyArrayOfBytes: ByteArray = ByteArray(0)

/**
 * Returns an [ByteArray] of size 0
 */
fun emptyByteArray(): ByteArray = emptyArrayOfBytes

/**
 * An [IntArray] of size 0
 */
private val emptyArrayOfInts: IntArray = IntArray(0)

/**
 * Returns an [IntArray] of size 0
 */
fun emptyIntArray(): IntArray = emptyArrayOfInts

/**
 * A [DoubleArray] of size 0
 */
private val emptyArrayOfDoubles: DoubleArray = DoubleArray(0)

/**
 * Returns a [DoubleArray] of size 0
 */
fun emptyDoubleArray(): DoubleArray = emptyArrayOfDoubles

/**
 * A [FloatArray] of size 0
 */
private val emptyArrayOfFloats: FloatArray = FloatArray(0)

/**
 * Returns a [FloatArray] of size 0
 */
fun emptyFloatArray(): FloatArray = emptyArrayOfFloats


/**
 * Returns an array that contains this (if not null) or is empty (if this == null)
 */
inline fun <reified T> T?.arrayOfNotNull(): Array<T> {
  return if (this != null) arrayOf(this) else emptyArray()
}


/**
 * Converts the int array to a double array
 */
@Slow
fun IntArray.asDoubles(): DoubleArray {
  return map {
    it.toDouble()
  }.toDoubleArray()
}

/**
 * Helper methods that simplifies addition of additional checks
 */
fun DoubleArray.safeCopyInto(destination: DoubleArray, destinationOffset: Int = 0, startIndex: @Inclusive Int = 0, endIndex: @Exclusive Int = size) {
  if (false) {
    //Verify
    require(startIndex < endIndex) {
      "startIndex $startIndex must be smaller than endIndex $endIndex"
    }

    val countToCopy = endIndex - startIndex
    require(countToCopy > 0) {
      "countToCopy too small: $countToCopy"
    }

    require(startIndex < this.size) {
      "startIndex $startIndex too large for size ${this.size}"
    }
    require(endIndex <= this.size) {
      "startIndex $endIndex too large for size ${this.size}"
    }

    require(destination.size >= destinationOffset + countToCopy) {
      "destination too small (size: ${destination.size} to insert $countToCopy starting at index $destinationOffset"
    }
  }

  this.copyInto(destination, destinationOffset, startIndex, endIndex)
}
