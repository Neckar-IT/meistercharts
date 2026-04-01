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
package it.neckar.open.kotlin.bytearray

import it.neckar.open.kotlin.lang.reinterpretAsFloat
import it.neckar.open.kotlin.lang.reinterpretAsInt
import kotlin.jvm.JvmInline

/**
 */

/** Copies [size] elements of [src] starting at [srcPos] into [dst] at [dstPos]  */
inline fun <T> arraycopy(src: Array<T>, srcPos: Int, dst: Array<T>, dstPos: Int, size: Int): Unit =
  run { src.copyInto(dst, dstPos, srcPos, srcPos + size) }

/** Copies [size] elements of [src] starting at [srcPos] into [dst] at [dstPos]  */
inline fun arraycopy(src: BooleanArray, srcPos: Int, dst: BooleanArray, dstPos: Int, size: Int): Unit =
  run { src.copyInto(dst, dstPos, srcPos, srcPos + size) }

/** Copies [size] elements of [src] starting at [srcPos] into [dst] at [dstPos]  */
inline fun arraycopy(src: LongArray, srcPos: Int, dst: LongArray, dstPos: Int, size: Int): Unit =
  run { src.copyInto(dst, dstPos, srcPos, srcPos + size) }

/** Copies [size] elements of [src] starting at [srcPos] into [dst] at [dstPos]  */
inline fun arraycopy(src: ByteArray, srcPos: Int, dst: ByteArray, dstPos: Int, size: Int): Unit =
  run { src.copyInto(dst, dstPos, srcPos, srcPos + size) }

/** Copies [size] elements of [src] starting at [srcPos] into [dst] at [dstPos]  */
inline fun arraycopy(src: ShortArray, srcPos: Int, dst: ShortArray, dstPos: Int, size: Int): Unit =
  run { src.copyInto(dst, dstPos, srcPos, srcPos + size) }

/** Copies [size] elements of [src] starting at [srcPos] into [dst] at [dstPos]  */
inline fun arraycopy(src: CharArray, srcPos: Int, dst: CharArray, dstPos: Int, size: Int): Unit =
  run { src.copyInto(dst, dstPos, srcPos, srcPos + size) }

/** Copies [size] elements of [src] starting at [srcPos] into [dst] at [dstPos]  */
inline fun arraycopy(src: IntArray, srcPos: Int, dst: IntArray, dstPos: Int, size: Int): Unit =
  run { src.copyInto(dst, dstPos, srcPos, srcPos + size) }

/** Copies [size] elements of [src] starting at [srcPos] into [dst] at [dstPos]  */
inline fun arraycopy(src: FloatArray, srcPos: Int, dst: FloatArray, dstPos: Int, size: Int): Unit =
  run { src.copyInto(dst, dstPos, srcPos, srcPos + size) }

/** Copies [size] elements of [src] starting at [srcPos] into [dst] at [dstPos]  */
inline fun arraycopy(src: DoubleArray, srcPos: Int, dst: DoubleArray, dstPos: Int, size: Int): Unit =
  run { src.copyInto(dst, dstPos, srcPos, srcPos + size) }




internal expect fun <T> _arrayfill(array: Array<T>, value: T, start: Int, end: Int): Unit
internal expect fun _arrayfill(array: BooleanArray, value: Boolean, start: Int, end: Int): Unit
internal expect fun _arrayfill(array: LongArray, value: Long, start: Int, end: Int): Unit
internal expect fun _arrayfill(array: ByteArray, value: Byte, start: Int, end: Int): Unit
internal expect fun _arrayfill(array: ShortArray, value: Short, start: Int, end: Int): Unit
internal expect fun _arrayfill(array: IntArray, value: Int, start: Int, end: Int): Unit
internal expect fun _arrayfill(array: FloatArray, value: Float, start: Int, end: Int): Unit
internal expect fun _arrayfill(array: DoubleArray, value: Double, start: Int, end: Int): Unit

/** Fills the [array] with the [value] starting a [start] end ending at [end] (end is not inclusive) */
fun <T> arrayfill(array: Array<T>, value: T, start: Int = 0, end: Int = array.size): Unit = _arrayfill(array, value, start, end)
/** Fills the [array] with the [value] starting a [start] end ending at [end] (end is not inclusive) */
fun arrayfill(array: BooleanArray, value: Boolean, start: Int = 0, end: Int = array.size): Unit = _arrayfill(array, value, start, end)
/** Fills the [array] with the [value] starting a [start] end ending at [end] (end is not inclusive) */
fun arrayfill(array: LongArray, value: Long, start: Int = 0, end: Int = array.size): Unit = _arrayfill(array, value, start, end)
/** Fills the [array] with the [value] starting a [start] end ending at [end] (end is not inclusive) */
fun arrayfill(array: ByteArray, value: Byte, start: Int = 0, end: Int = array.size): Unit = _arrayfill(array, value, start, end)
/** Fills the [array] with the [value] starting a [start] end ending at [end] (end is not inclusive) */
fun arrayfill(array: ShortArray, value: Short, start: Int = 0, end: Int = array.size): Unit = _arrayfill(array, value, start, end)
/** Fills the [array] with the [value] starting a [start] end ending at [end] (end is not inclusive) */
fun arrayfill(array: IntArray, value: Int, start: Int = 0, end: Int = array.size): Unit = _arrayfill(array, value, start, end)
/** Fills the [array] with the [value] starting a [start] end ending at [end] (end is not inclusive) */
fun arrayfill(array: FloatArray, value: Float, start: Int = 0, end: Int = array.size): Unit = _arrayfill(array, value, start, end)
/** Fills the [array] with the [value] starting a [start] end ending at [end] (end is not inclusive) */
fun arrayfill(array: DoubleArray, value: Double, start: Int = 0, end: Int = array.size): Unit = _arrayfill(array, value, start, end)

/** Fills [this] array with the [value] starting a [start] end ending at [end] (end is not inclusive) */
inline fun <T> Array<T>.fill(value: T, start: Int = 0, end: Int = this.size): Unit = arrayfill(this, value, start, end)
/** Fills [this] array with the [value] starting a [start] end ending at [end] (end is not inclusive) */
inline fun BooleanArray.fill(value: Boolean, start: Int = 0, end: Int = this.size): Unit = arrayfill(this, value, start, end)
/** Fills [this] array with the [value] starting a [start] end ending at [end] (end is not inclusive) */
inline fun LongArray.fill(value: Long, start: Int = 0, end: Int = this.size): Unit = arrayfill(this, value, start, end)
/** Fills [this] array with the [value] starting a [start] end ending at [end] (end is not inclusive) */
inline fun ByteArray.fill(value: Byte, start: Int = 0, end: Int = this.size): Unit = arrayfill(this, value, start, end)
/** Fills [this] array with the [value] starting a [start] end ending at [end] (end is not inclusive) */
inline fun ShortArray.fill(value: Short, start: Int = 0, end: Int = this.size): Unit = arrayfill(this, value, start, end)
/** Fills [this] array with the [value] starting a [start] end ending at [end] (end is not inclusive) */
inline fun IntArray.fill(value: Int, start: Int = 0, end: Int = this.size): Unit = arrayfill(this, value, start, end)
/** Fills [this] array with the [value] starting a [start] end ending at [end] (end is not inclusive) */
inline fun FloatArray.fill(value: Float, start: Int = 0, end: Int = this.size): Unit = arrayfill(this, value, start, end)
/** Fills [this] array with the [value] starting a [start] end ending at [end] (end is not inclusive) */
inline fun DoubleArray.fill(value: Double, start: Int = 0, end: Int = this.size): Unit = arrayfill(this, value, start, end)


/** View of [bytes] [ByteArray] reinterpreted as [Int] */
@JvmInline
value class UByteArrayInt(val bytes: ByteArray) {
  val size: Int get() = bytes.size
  operator fun get(index: Int) = bytes[index].toInt() and 0xFF
  operator fun set(index: Int, value: Int) = run { bytes[index] = value.toByte() }
}

/** Creates a new [UByteArrayInt] view of [size] bytes */
fun UByteArrayInt(size: Int) = UByteArrayInt(ByteArray(size))

/** Creates a view of [this] reinterpreted as [Int] */
fun ByteArray.asUByteArrayInt() = UByteArrayInt(this)
/** Gets the underlying array of [this] */
fun UByteArrayInt.asByteArray() = this.bytes

/** View of [base] [IntArray] reinterpreted as [Float] */
@JvmInline
value class FloatArrayFromIntArray(val base: IntArray) {
  operator fun get(i: Int) = base[i].reinterpretAsFloat()
  operator fun set(i: Int, v: Float) = run { base[i] = v.reinterpretAsInt() }
}

/** Creates a view of [this] reinterpreted as [Float] */
fun IntArray.asFloatArray(): FloatArrayFromIntArray = FloatArrayFromIntArray(this)
/** Gets the underlying array of [this] */
fun FloatArrayFromIntArray.asIntArray(): IntArray = base
