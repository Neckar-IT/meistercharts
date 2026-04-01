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

import kotlin.jvm.JvmOverloads
import kotlin.math.min

class RingBuffer(bits: Int) : ByteRingBuffer(bits)

open class ByteRingBuffer(val bits: Int) {
  val totalSize = 1 shl bits
  private val mask = totalSize - 1
  private val buffer = ByteArray(totalSize)
  private var readPos = 0
  private var writePos = 0
  var availableWrite = totalSize; private set
  var availableRead = 0; private set

  @JvmOverloads
  fun writeHead(data: ByteArray, offset: Int = 0, size: Int = data.size - offset): Int {
    val toWrite = min(availableWrite, size)
    for (n in 0 until toWrite) {
      readPos = (readPos - 1) and mask
      buffer[readPos] = data[offset + size - n - 1]
    }
    availableRead += toWrite
    availableWrite -= toWrite
    return toWrite
  }

  @JvmOverloads
  fun write(data: ByteArray, offset: Int = 0, size: Int = data.size - offset): Int {
    val toWrite = min(availableWrite, size)
    for (n in 0 until toWrite) {
      buffer[writePos] = data[offset + n]
      writePos = (writePos + 1) and mask
    }
    availableRead += toWrite
    availableWrite -= toWrite
    return toWrite
  }

  @JvmOverloads
  fun read(data: ByteArray, offset: Int = 0, size: Int = data.size - offset): Int {
    val toRead = min(availableRead, size)
    for (n in 0 until toRead) {
      data[offset + n] = buffer[readPos]
      readPos = (readPos + 1) and mask
    }
    availableWrite += toRead
    availableRead -= toRead
    return toRead
  }

  fun readByte(): Int {
    if (availableRead <= 0) return -1
    val out = buffer[readPos].toInt() and 0xFF
    readPos = (readPos + 1) and mask
    availableRead--
    availableWrite++
    return out
  }

  fun writeByte(v: Int): Boolean {
    if (availableWrite <= 0) return false
    buffer[writePos] = v.toByte()
    writePos = (writePos + 1) and mask
    availableWrite--
    availableRead++
    return true
  }

  fun clear() {
    readPos = 0
    writePos = 0
    availableRead = 0
    availableWrite = totalSize
  }

  fun peek(offset: Int = 0) = buffer[(readPos + offset) and mask]
  override fun equals(other: Any?): Boolean = (other is ByteRingBuffer) && this.availableRead == other.availableRead && equaler(availableRead) { this.peek(it) == other.peek(it) }
  override fun hashCode(): Int = contentHashCode()
  fun contentHashCode(): Int = hashCoder(availableRead) { peek(it).toInt() }
}

class ShortRingBuffer(val bits: Int) {
  val totalSize = 1 shl bits
  private val mask = totalSize - 1
  private val buffer = ShortArray(totalSize)
  private var readPos = 0
  private var writePos = 0
  var availableWrite = totalSize; private set
  var availableRead = 0; private set

  @JvmOverloads
  fun writeHead(data: ShortArray, offset: Int = 0, size: Int = data.size - offset): Int {
    val toWrite = min(availableWrite, size)
    for (n in 0 until toWrite) {
      readPos = (readPos - 1) and mask
      buffer[readPos] = data[offset + size - n - 1]
    }
    availableRead += toWrite
    availableWrite -= toWrite
    return toWrite
  }

  @JvmOverloads
  fun write(data: ShortArray, offset: Int = 0, size: Int = data.size - offset): Int {
    val toWrite = min(availableWrite, size)
    for (n in 0 until toWrite) {
      buffer[writePos] = data[offset + n]
      writePos = (writePos + 1) and mask
    }
    availableRead += toWrite
    availableWrite -= toWrite
    return toWrite
  }

  @JvmOverloads
  fun read(data: ShortArray, offset: Int = 0, size: Int = data.size - offset): Int {
    val toRead = min(availableRead, size)
    for (n in 0 until toRead) {
      data[offset + n] = buffer[readPos]
      readPos = (readPos + 1) and mask
    }
    availableWrite += toRead
    availableRead -= toRead
    return toRead
  }

  private val temp = ShortArray(1)
  fun readOne(): Short {
    read(temp, 0, 1)
    return temp[0]
  }

  fun writeOne(value: Short) {
    temp[0] = value
    write(temp, 0, 1)
  }

  fun clear() {
    readPos = 0
    writePos = 0
    availableRead = 0
    availableWrite = totalSize
  }

  fun peek(offset: Int = 0) = buffer[(readPos + offset) and mask]
  override fun equals(other: Any?): Boolean = (other is ShortRingBuffer) && this.availableRead == other.availableRead && equaler(availableRead) { this.peek(it) == other.peek(it) }
  override fun hashCode(): Int = contentHashCode()
  fun contentHashCode(): Int = hashCoder(availableRead) { peek(it).toInt() }
}

class IntRingBuffer(val bits: Int) {
  val totalSize = 1 shl bits
  private val mask = totalSize - 1
  private val buffer = IntArray(totalSize)
  private var readPos = 0
  private var writePos = 0
  var availableWrite = totalSize; private set
  var availableRead = 0; private set

  @JvmOverloads
  fun writeHead(data: IntArray, offset: Int = 0, size: Int = data.size - offset): Int {
    val toWrite = min(availableWrite, size)
    for (n in 0 until toWrite) {
      readPos = (readPos - 1) and mask
      buffer[readPos] = data[offset + size - n - 1]
    }
    availableRead += toWrite
    availableWrite -= toWrite
    return toWrite
  }

  @JvmOverloads
  fun write(data: IntArray, offset: Int = 0, size: Int = data.size - offset): Int {
    val toWrite = min(availableWrite, size)
    for (n in 0 until toWrite) {
      buffer[writePos] = data[offset + n]
      writePos = (writePos + 1) and mask
    }
    availableRead += toWrite
    availableWrite -= toWrite
    return toWrite
  }

  @JvmOverloads
  fun read(data: IntArray, offset: Int = 0, size: Int = data.size - offset): Int {
    val toRead = min(availableRead, size)
    for (n in 0 until toRead) {
      data[offset + n] = buffer[readPos]
      readPos = (readPos + 1) and mask
    }
    availableWrite += toRead
    availableRead -= toRead
    return toRead
  }

  fun clear() {
    readPos = 0
    writePos = 0
    availableRead = 0
    availableWrite = totalSize
  }

  fun peek(offset: Int = 0) = buffer[(readPos + offset) and mask]
  override fun equals(other: Any?): Boolean = (other is IntRingBuffer) && this.availableRead == other.availableRead && equaler(availableRead) { this.peek(it) == other.peek(it) }
  override fun hashCode(): Int = contentHashCode()
  fun contentHashCode(): Int = hashCoder(availableRead) { peek(it).toInt() }
}

class FloatRingBuffer(val bits: Int) {
  val totalSize = 1 shl bits
  private val mask = totalSize - 1
  private val buffer = FloatArray(totalSize)
  private var readPos = 0
  private var writePos = 0
  var availableWrite = totalSize; private set
  var availableRead = 0; private set

  @JvmOverloads
  fun writeHead(data: FloatArray, offset: Int = 0, size: Int = data.size - offset): Int {
    val toWrite = min(availableWrite, size)
    for (n in 0 until toWrite) {
      readPos = (readPos - 1) and mask
      buffer[readPos] = data[offset + size - n - 1]
    }
    availableRead += toWrite
    availableWrite -= toWrite
    return toWrite
  }

  @JvmOverloads
  fun write(data: FloatArray, offset: Int = 0, size: Int = data.size - offset): Int {
    val toWrite = min(availableWrite, size)
    for (n in 0 until toWrite) {
      buffer[writePos] = data[offset + n]
      writePos = (writePos + 1) and mask
    }
    availableRead += toWrite
    availableWrite -= toWrite
    return toWrite
  }

  @JvmOverloads
  fun read(data: FloatArray, offset: Int = 0, size: Int = data.size - offset): Int {
    val toRead = min(availableRead, size)
    for (n in 0 until toRead) {
      data[offset + n] = buffer[readPos]
      readPos = (readPos + 1) and mask
    }
    availableWrite += toRead
    availableRead -= toRead
    return toRead
  }

  fun clear() {
    readPos = 0
    writePos = 0
    availableRead = 0
    availableWrite = totalSize
  }

  fun peek(offset: Int = 0) = buffer[(readPos + offset) and mask]
  override fun equals(other: Any?): Boolean = (other is FloatRingBuffer) && this.availableRead == other.availableRead && equaler(availableRead) { this.peek(it) == other.peek(it) }
  override fun hashCode(): Int = contentHashCode()
  fun contentHashCode(): Int = hashCoder(availableRead) { peek(it).toBits() }
}
