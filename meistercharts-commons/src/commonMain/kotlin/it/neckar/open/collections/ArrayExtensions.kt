/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
