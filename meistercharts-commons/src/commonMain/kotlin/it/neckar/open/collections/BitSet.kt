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

import it.neckar.open.collections.*
import kotlin.collections.Collection
import kotlin.collections.Iterator
import kotlin.collections.any
import kotlin.collections.contentEquals
import kotlin.collections.contentHashCode
import kotlin.collections.map

/**
 * Fixed size [BitSet]. Similar to a [BooleanArray] but tightly packed to reduce memory usage.
 */
class BitSet(override val size: Int) : Collection<Boolean> {
  val data: IntArray = IntArray(size divCeil 32)

  private fun part(index: Int) = index ushr 5
  private fun bit(index: Int) = index and 0x1f

  operator fun get(index: Int): Boolean = ((data[part(index)] ushr (bit(index))) and 1) != 0
  operator fun set(index: Int, value: Boolean) {
    val i = part(index)
    val b = bit(index)
    if (value) {
      data[i] = data[i] or (1 shl b)
    } else {
      data[i] = data[i] and (1 shl b).inv()
    }
  }

  fun set(index: Int): Unit = set(index, true)
  fun unset(index: Int): Unit = set(index, false)

  fun clear(): Unit = data.fill(0)

  override fun contains(element: Boolean): Boolean = (0 until size).any { this[it] == element }
  override fun containsAll(elements: Collection<Boolean>): Boolean = when {
    elements.contains(true) && !this.contains(true) -> false
    elements.contains(false) && !this.contains(false) -> false
    else -> true
  }

  override fun isEmpty(): Boolean = size == 0
  override fun iterator(): Iterator<Boolean> = (0 until size).map { this[it] }.iterator()

  override fun hashCode(): Int = data.contentHashCode() + size
  override fun equals(other: Any?): Boolean = (other is BitSet) && this.size == other.size && this.data.contentEquals(other.data)
}
