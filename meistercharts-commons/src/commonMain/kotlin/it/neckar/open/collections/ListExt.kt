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
 * Contains extension methods for List
 */
fun <T> List<T>.shifted(shiftCount: Int): List<T> {
  val size = this.size
  if (size == 0 || shiftCount % size == 0) return this

  // Normalize into 0 until size: Kotlin's % keeps the dividend's sign, so a negative shiftCount
  // would otherwise make drop()/take() throw. A negative shift rotates the other way.
  val effectiveShift = ((shiftCount % size) + size) % size
  return this.drop(effectiveShift) + this.take(effectiveShift)
}

/**
 * Removes the first element that matches the predicate and returns it or null if no element was found
 */
fun <T> MutableList<T>.removeIfOrNull(predicate: (T) -> Boolean): T? {
  val index = this.indexOfFirst(predicate)
  return if (index != -1) {
    this.removeAt(index)
  } else {
    null
  }
}
