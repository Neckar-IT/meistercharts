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

import kotlin.jvm.JvmInline

class ByteArrayReader(val data: ByteArray, val start: Int, val size: Int = 0) {
  private var offset = start
  val remaining get() = size - offset
  val hasMore get() = remaining > 0

  private fun <T> move(count: Int, callback: ByteArray.(Int) -> T): T {
    val res = callback(data, this.offset)
    this.offset += count
    return res
  }

  fun u8() = move(1) { readU8(it) }
  fun s8() = move(1) { readS8(it) }

  fun u16(little: Boolean) = move(2) { readU16(it, little) }
  fun s16(little: Boolean) = move(2) { readS16(it, little) }
  fun u16LE() = move(2) { readU16LE(it) }
  fun s16LE() = move(2) { readS16LE(it) }
  fun u16BE() = move(2) { readU16BE(it) }
  fun s16BE() = move(2) { readS16BE(it) }

  fun u24(little: Boolean) = move(3) { readU24(it, little) }
  fun s24(little: Boolean) = move(3) { readS24(it, little) }
  fun u24LE() = move(3) { readU24LE(it) }
  fun s24LE() = move(3) { readS24LE(it) }
  fun u24BE() = move(3) { readU24BE(it) }
  fun s24BE() = move(3) { readS24BE(it) }

  fun u32(little: Boolean) = move(4) { readU32(it, little) }
  fun s32(little: Boolean) = move(4) { readS32(it, little) }
  fun u32LE() = move(4) { readU32LE(it) }
  fun s32LE() = move(4) { readS32LE(it) }
  fun u32BE() = move(4) { readU32BE(it) }
  fun s32BE() = move(4) { readS32BE(it) }

  fun f32(little: Boolean) = move(4) { readF32(it, little) }
  fun f32LE() = move(4) { readF32LE(it) }
  fun f32BE() = move(4) { readF32BE(it) }
  fun f64(little: Boolean) = move(8) { readF64(it, little) }
  fun f64LE() = move(8) { readF64LE(it) }
  fun f64BE() = move(8) { readF64BE(it) }
}

@JvmInline
value class ByteArrayReaderLE(val bar: ByteArrayReader)

val ByteArrayReaderLE.size get() = bar.size
val ByteArrayReaderLE.remaining get() = bar.remaining
val ByteArrayReaderLE.hasMore get() = bar.hasMore
fun ByteArrayReaderLE.u8() = bar.u8()
fun ByteArrayReaderLE.s8() = bar.s8()
fun ByteArrayReaderLE.u16() = bar.u16LE()
fun ByteArrayReaderLE.s16() = bar.s16LE()
fun ByteArrayReaderLE.u24() = bar.u24LE()
fun ByteArrayReaderLE.s24() = bar.s24LE()
fun ByteArrayReaderLE.u32() = bar.u32LE()
fun ByteArrayReaderLE.s32() = bar.s32LE()
fun ByteArrayReaderLE.f32() = bar.f32LE()
fun ByteArrayReaderLE.f64() = bar.f64LE()

@JvmInline
value class ByteArrayReaderBE(val bar: ByteArrayReader)

val ByteArrayReaderBE.size get() = bar.size
val ByteArrayReaderBE.remaining get() = bar.remaining
val ByteArrayReaderBE.hasMore get() = bar.hasMore
fun ByteArrayReaderBE.u8() = bar.u8()
fun ByteArrayReaderBE.s8() = bar.s8()
fun ByteArrayReaderBE.u16() = bar.u16BE()
fun ByteArrayReaderBE.s16() = bar.s16BE()
fun ByteArrayReaderBE.u24() = bar.u24BE()
fun ByteArrayReaderBE.s24() = bar.s24BE()
fun ByteArrayReaderBE.u32() = bar.u32BE()
fun ByteArrayReaderBE.s32() = bar.s32BE()
fun ByteArrayReaderBE.f32() = bar.f32BE()
fun ByteArrayReaderBE.f64() = bar.f64BE()

fun ByteArray.reader(offset: Int = 0, size: Int = this.size) = ByteArrayReader(this, offset, size)
fun ByteArray.readerLE(offset: Int = 0, size: Int = this.size) = ByteArrayReaderLE(reader(offset, size))
fun ByteArray.readerBE(offset: Int = 0, size: Int = this.size) = ByteArrayReaderBE(reader(offset, size))

fun <T> ByteArray.read(offset: Int = 0, size: Int = this.size, callback: ByteArrayReader.() -> T): T =
  callback(reader(offset, size))

fun <T> ByteArray.readLE(offset: Int = 0, size: Int = this.size, callback: ByteArrayReaderLE.() -> T): T =
  callback(readerLE(offset, size))

fun <T> ByteArray.readBE(offset: Int = 0, size: Int = this.size, callback: ByteArrayReaderBE.() -> T): T =
  callback(readerBE(offset, size))
