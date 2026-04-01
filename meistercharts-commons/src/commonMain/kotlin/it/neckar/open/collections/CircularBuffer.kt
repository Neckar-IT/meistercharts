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

/**
 * A circular buffer with a fixed capacity.
 * The buffer is filled from the head and emptied from the tail.
 * If the buffer is full, the oldest element is removed when a new element is added.
 */
class CircularBuffer<T>(private val capacity: Int) {
  private val buffer = arrayOfNulls<Any?>(capacity)
  private var head = 0
  private var tail = 0
  private var size = 0

  fun add(element: T) {
    buffer[head] = element
    head = (head + 1) % capacity
    if (size < capacity) {
      size++
    } else {
      tail = (tail + 1) % capacity // If the buffer is full, move the tail pointer
    }
  }

  fun get(index: Int): T {
    if (index < 0 || index >= size) {
      throw IndexOutOfBoundsException("Index: $index, Size: $size")
    }
    return buffer[(tail + index) % capacity] as T
  }

  fun size(): Int {
    return size
  }

  /**
   * Converts the entries in the buffer to a list.
   */
  fun toList(): List<T> {
    return (0 until size)
      .map {
        get(it)
      }
  }
}
