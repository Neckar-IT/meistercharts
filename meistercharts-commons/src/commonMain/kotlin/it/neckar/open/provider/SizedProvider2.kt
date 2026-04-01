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
package it.neckar.open.provider

/**
 * Sized provider that takes two parameters
 */
interface SizedProvider2<out T, in P1, in P2> : MultiProvider2<Any, T, P1, P2>, HasSize2<P1, P2> {
  /**
   * Returns the first element
   * Throws a [NoSuchElementException] if there are no elements
   */
  fun first(param1: P1, param2: P2): T {
    if (size(param1, param2) == 0) {
      throw NoSuchElementException("Size is 0")
    }
    return this.valueAt(0, param1, param2)
  }

  /**
   * Returns the last element.
   * Throws a [NoSuchElementException] if there are no elements
   */
  fun last(param1: P1, param2: P2): T {
    val size = size(param1, param2)
    if (size == 0) {
      throw NoSuchElementException("Size is 0")
    }
    return this.valueAt(size - 1, param1, param2)
  }

  /**
   * Returns the element at the given index or null if the index is >= [size]
   */
  fun getOrNull(index: Int, param1: P1, param2: P2): T? {
    if (index >= size(param1, param2)) {
      return null
    }

    return valueAt(index, param1, param2)
  }

  companion object {
    //required for extension methods
  }
}
