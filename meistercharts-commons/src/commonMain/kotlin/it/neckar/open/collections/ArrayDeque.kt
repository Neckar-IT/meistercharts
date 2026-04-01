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

class ByteArrayDeque(val initialBits: Int = 10, val allowGrow: Boolean = true) {
  private var ring = RingBuffer(initialBits)
  private val tempBuffer = ByteArray(1024)

  var written: Long = 0; private set
  var read: Long = 0; private set
  val availableWriteWithoutAllocating get() = ring.availableWrite
  val availableRead get() = ring.availableRead

  @JvmOverloads
  fun writeHead(buffer: ByteArray, offset: Int = 0, size: Int = buffer.size - offset): Int {
    val out = ensureWrite(size).ring.writeHead(buffer, offset, size)
    if (out > 0) written += out
    return out
  }

  @JvmOverloads
  fun write(buffer: ByteArray, offset: Int = 0, size: Int = buffer.size - offset): Int {
    val out = ensureWrite(size).ring.write(buffer, offset, size)
    if (out > 0) written += out
    return out
  }

  @JvmOverloads
  fun read(buffer: ByteArray, offset: Int = 0, size: Int = buffer.size - offset): Int {
    val out = ring.read(buffer, offset, size)
    if (out > 0) read += out
    return out
  }

  fun readByte(): Int = ring.readByte()
  fun writeByte(v: Int): Boolean = ensureWrite(1).ring.writeByte(v)

  private fun ensureWrite(count: Int): ByteArrayDeque {
    if (count > ring.availableWrite) {
      if (!allowGrow) {
        val message = "Can't grow ByteArrayDeque. Need to write $count, but only ${ring.availableWrite} is available"
        println("ERROR: $message")
        error(message)
      }
      val minNewSize = ring.availableRead + count
      val newBits = ilog2(minNewSize) + 2
      val newRing = RingBuffer(newBits)
      while (ring.availableRead > 0) {
        val read = ring.read(tempBuffer, 0, tempBuffer.size)
        newRing.write(tempBuffer, 0, read)
      }
      this.ring = newRing
    }
    return this
  }

  fun clear() {
    ring.clear()
  }

  val hasMoreToWrite get() = ring.availableWrite > 0
  val hasMoreToRead get() = ring.availableRead > 0
  fun readOne() = run {
    read(tempBuffer, 0, 1)
    tempBuffer[0]
  }

  fun writeOne(value: Byte) {
    tempBuffer[0] = value
    write(tempBuffer, 0, 1)
  }

  override fun hashCode(): Int = ring.contentHashCode()
  override fun equals(other: Any?): Boolean = (other is ByteArrayDeque) && this.ring == other.ring
}

class ShortArrayDeque(val initialBits: Int = 10) {
  private var ring = ShortRingBuffer(initialBits)
  private val tempBuffer = ShortArray(1024)

  var written: Long = 0; private set
  var read: Long = 0; private set
  val availableWriteWithoutAllocating get() = ring.availableWrite
  val availableRead get() = ring.availableRead

  @JvmOverloads
  fun writeHead(buffer: ShortArray, offset: Int = 0, size: Int = buffer.size - offset): Int {
    val out = ensureWrite(size).ring.writeHead(buffer, offset, size)
    if (out > 0) written += out
    return out
  }

  @JvmOverloads
  fun write(buffer: ShortArray, offset: Int = 0, size: Int = buffer.size - offset): Int {
    val out = ensureWrite(size).ring.write(buffer, offset, size)
    if (out > 0) written += out
    return out
  }

  @JvmOverloads
  fun read(buffer: ShortArray, offset: Int = 0, size: Int = buffer.size - offset): Int {
    val out = ring.read(buffer, offset, size)
    if (out > 0) read += out
    return out
  }

  private fun ensureWrite(count: Int): ShortArrayDeque {
    if (count > ring.availableWrite) {
      val minNewSize = ring.availableRead + count
      val newBits = ilog2(minNewSize) + 2
      val newRing = ShortRingBuffer(newBits)
      while (ring.availableRead > 0) {
        val read = ring.read(tempBuffer, 0, tempBuffer.size)
        newRing.write(tempBuffer, 0, read)
      }
      this.ring = newRing
    }
    return this
  }

  fun clear() {
    ring.clear()
  }

  val hasMoreToWrite get() = ring.availableWrite > 0
  val hasMoreToRead get() = ring.availableRead > 0
  fun readOne(): Short {
    read(tempBuffer, 0, 1)
    return tempBuffer[0]
  }

  fun writeOne(value: Short) {
    tempBuffer[0] = value
    write(tempBuffer, 0, 1)
  }

  override fun hashCode(): Int = ring.contentHashCode()
  override fun equals(other: Any?): Boolean = (other is ShortArrayDeque) && this.ring == other.ring
}


class IntArrayDeque(val initialBits: Int = 10) {
  private var ring = IntRingBuffer(initialBits)
  private val tempBuffer = IntArray(1024)

  var written: Long = 0; private set
  var read: Long = 0; private set
  val availableWriteWithoutAllocating get() = ring.availableWrite
  val availableRead get() = ring.availableRead

  @JvmOverloads
  fun writeHead(buffer: IntArray, offset: Int = 0, size: Int = buffer.size - offset): Int {
    val out = ensureWrite(size).ring.writeHead(buffer, offset, size)
    if (out > 0) written += out
    return out
  }

  @JvmOverloads
  fun write(buffer: IntArray, offset: Int = 0, size: Int = buffer.size - offset): Int {
    val out = ensureWrite(size).ring.write(buffer, offset, size)
    if (out > 0) written += out
    return out
  }

  @JvmOverloads
  fun read(buffer: IntArray, offset: Int = 0, size: Int = buffer.size - offset): Int {
    val out = ring.read(buffer, offset, size)
    if (out > 0) read += out
    return out
  }

  private fun ensureWrite(count: Int): IntArrayDeque {
    if (count > ring.availableWrite) {
      val minNewSize = ring.availableRead + count
      val newBits = ilog2(minNewSize) + 2
      val newRing = IntRingBuffer(newBits)
      while (ring.availableRead > 0) {
        val read = ring.read(tempBuffer, 0, tempBuffer.size)
        newRing.write(tempBuffer, 0, read)
      }
      this.ring = newRing
    }
    return this
  }

  fun clear() {
    ring.clear()
  }

  val hasMoreToWrite get() = ring.availableWrite > 0
  val hasMoreToRead get() = ring.availableRead > 0
  fun readOne() = run {
    read(tempBuffer, 0, 1)
    tempBuffer[0]
  }

  fun writeOne(value: Int) {
    tempBuffer[0] = value
    write(tempBuffer, 0, 1)
  }

  override fun hashCode(): Int = ring.contentHashCode()
  override fun equals(other: Any?): Boolean = (other is IntArrayDeque) && this.ring == other.ring
}


class FloatArrayDeque(val initialBits: Int = 10) {
  private var ring = FloatRingBuffer(initialBits)
  private val tempBuffer = FloatArray(1024)

  var written: Long = 0; private set
  var read: Long = 0; private set
  val availableWriteWithoutAllocating get() = ring.availableWrite
  val availableRead get() = ring.availableRead

  @JvmOverloads
  fun writeHead(buffer: FloatArray, offset: Int = 0, size: Int = buffer.size - offset): Int {
    val out = ensureWrite(size).ring.writeHead(buffer, offset, size)
    if (out > 0) written += out
    return out
  }

  @JvmOverloads
  fun write(buffer: FloatArray, offset: Int = 0, size: Int = buffer.size - offset): Int {
    val out = ensureWrite(size).ring.write(buffer, offset, size)
    if (out > 0) written += out
    return out
  }

  @JvmOverloads
  fun read(buffer: FloatArray, offset: Int = 0, size: Int = buffer.size - offset): Int {
    val out = ring.read(buffer, offset, size)
    if (out > 0) read += out
    return out
  }

  private fun ensureWrite(count: Int): FloatArrayDeque {
    if (count > ring.availableWrite) {
      val minNewSize = ring.availableRead + count
      val newBits = ilog2(minNewSize) + 2
      val newRing = FloatRingBuffer(newBits)
      while (ring.availableRead > 0) {
        val read = ring.read(tempBuffer, 0, tempBuffer.size)
        newRing.write(tempBuffer, 0, read)
      }
      this.ring = newRing
    }
    return this
  }

  fun clear() {
    ring.clear()
  }

  val hasMoreToWrite get() = ring.availableWrite > 0
  val hasMoreToRead get() = ring.availableRead > 0
  fun readOne() = run {
    read(tempBuffer, 0, 1)
    tempBuffer[0]
  }

  fun writeOne(value: Float) {
    tempBuffer[0] = value
    write(tempBuffer, 0, 1)
  }

  override fun hashCode(): Int = ring.contentHashCode()
  override fun equals(other: Any?): Boolean = (other is FloatArrayDeque) && this.ring == other.ring
}
