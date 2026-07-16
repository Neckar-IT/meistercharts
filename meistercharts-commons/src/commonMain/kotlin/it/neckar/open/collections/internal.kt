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

import it.neckar.open.annotations.Hot

@PublishedApi
internal infix fun Int.divCeil(that: Int): Int = if (this % that != 0) (this / that) + 1 else (this / that)

@PublishedApi
internal infix fun Int.umod(other: Int): Int {
  val remainder = this % other
  return when {
    remainder < 0 -> remainder + other
    else -> remainder
  }
}

@PublishedApi
internal fun ilog2(v: Int): Int = kotlin.math.log2(v.toDouble()).toInt()

@PublishedApi
internal fun ilog2Ceil(v: Int): Int = kotlin.math.ceil(kotlin.math.log2(v.toDouble())).toInt()

//private val ilog2_tab32 = intArrayOf(
//    0, 9, 1, 10, 13, 21, 2, 29,
//    11, 14, 16, 18, 22, 25, 3, 30,
//    8, 12, 20, 28, 15, 17, 24, 7,
//    19, 27, 23, 6, 26, 5, 4, 31
//)
//
//internal fun ilog2(value: Int): Int {
//    var v = value
//    v = v or (v ushr 1)
//    v = v or (v ushr 2)
//    v = v or (v ushr 4)
//    v = v or (v ushr 8)
//    v = v or (v ushr 16)
//    return ilog2_tab32[(v * 0x07C4ACDD) ushr 27]
//}


// arraycopy wrappers delegate to kotlin.collections.copyInto, which returns the
// destination array purely as a convenience. All call sites discard the return.

@IgnorableReturnValue
internal fun <T> arraycopy(src: Array<T>, srcPos: Int, dst: Array<T>, dstPos: Int, size: Int) =
  src.copyInto(dst, dstPos, srcPos, srcPos + size)

@IgnorableReturnValue
internal fun arraycopy(src: ByteArray, srcPos: Int, dst: ByteArray, dstPos: Int, size: Int) =
  src.copyInto(dst, dstPos, srcPos, srcPos + size)

@Hot
@IgnorableReturnValue
internal fun arraycopy(src: IntArray, srcPos: Int, dst: IntArray, dstPos: Int, size: Int) =
  src.copyInto(dst, dstPos, srcPos, srcPos + size)

@IgnorableReturnValue
internal fun arraycopy(src: FloatArray, srcPos: Int, dst: FloatArray, dstPos: Int, size: Int) =
  src.copyInto(dst, dstPos, srcPos, srcPos + size)

@IgnorableReturnValue
internal fun arraycopy(src: DoubleArray, srcPos: Int, dst: DoubleArray, dstPos: Int, size: Int) =
  src.copyInto(dst, dstPos, srcPos, srcPos + size)

internal fun <T> Array<T>.fill(value: T) = run { for (n in 0 until this.size) this[n] = value }
internal fun IntArray.fill(value: Int) = run { for (n in 0 until this.size) this[n] = value }

internal inline fun <T> contentHashCode(size: Int, gen: (index: Int) -> T): Int {
  var result = 1
  for (n in 0 until size) result = 31 * result + gen(n).hashCode()
  return result
}

internal inline fun hashCoder(count: Int, gen: (index: Int) -> Int): Int {
  var out = 0
  for (n in 0 until count) {
    out *= 7
    out += gen(n)
  }
  return out
}

internal fun <T> Array<T>.contentHashCode(src: Int, dst: Int): Int = hashCoder(dst - src) { this[src + it].hashCode() }
internal fun IntArray.contentHashCode(src: Int, dst: Int): Int = hashCoder(dst - src) { this[src + it].toInt() }
internal fun ShortArray.contentHashCode(src: Int, dst: Int): Int = hashCoder(dst - src) { this[src + it].toInt() }
internal fun FloatArray.contentHashCode(src: Int, dst: Int): Int = hashCoder(dst - src) { this[src + it].toRawBits() }
internal fun DoubleArray.contentHashCode(src: Int, dst: Int): Int = hashCoder(dst - src) { this[src + it].toInt() } // Do not want to use Long (.toRawBits) to prevent boxing on JS

internal fun <T> Array<out T>.contentEquals(that: Array<T>, src: Int, dst: Int): Boolean = equaler(dst - src) { this[src + it] == that[src + it] }
internal fun IntArray.contentEquals(that: IntArray, src: Int, dst: Int): Boolean = equaler(dst - src) { this[src + it] == that[src + it] }
internal fun ShortArray.contentEquals(that: ShortArray, src: Int, dst: Int): Boolean = equaler(dst - src) { this[src + it] == that[src + it] }
internal fun FloatArray.contentEquals(that: FloatArray, src: Int, dst: Int): Boolean = equaler(dst - src) { this[src + it] == that[src + it] }
internal fun DoubleArray.contentEquals(that: DoubleArray, src: Int, dst: Int): Boolean = equaler(dst - src) { this[src + it] == that[src + it] } // Do not want to use Long (.toRawBits) to prevent boxing on JS

internal inline fun equaler(count: Int, gen: (index: Int) -> Boolean): Boolean {
  for (n in 0 until count) if (!gen(n)) return false
  return true
}
