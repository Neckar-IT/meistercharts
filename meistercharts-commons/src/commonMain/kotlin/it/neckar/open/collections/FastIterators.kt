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


inline fun IntArrayList.fastForEach(callback: (Int) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(this.getAt(n++))
  }
}

inline fun FloatArrayList.fastForEach(callback: (Float) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(this.getAt(n++))
  }
}

inline fun DoubleArrayList.fastForEach(callback: (Double) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(this.getAt(n++))
  }
}

inline fun DoubleArrayList.fastForEachReversed(callback: (value: Double) -> Unit) {
  var n = lastIndex
  while (n >= 0) {
    callback(this.getAt(n))
    n--
  }
}

/**
 * Returns true if at least one of the elements matches the given [predicate].
 * [predicate] is called for all elements until it returns true.
 */
inline fun DoubleArrayList.fastFindAny(predicate: (Double) -> Boolean): Boolean {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    if (predicate(this.getAt(n++))) {
      return true
    }
  }

  return false
}

inline fun IntArrayList.fastForEachIndexed(callback: (index: Int, value: Int) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(n, this.getAt(n))
    n++
  }
}

inline fun FloatArrayList.fastForEachIndexed(callback: (index: Int, value: Float) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(n, this.getAt(n))
    n++
  }
}

inline fun DoubleArrayList.fastForEachIndexed(callback: (index: Int, value: Double) -> Unit) {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    callback(n, this.getAt(n))
    n++
  }
}

inline fun DoubleArrayList.fastForEachIndexed(iterationOrder: IterationOrder, callback: (index: Int, value: Double) -> Unit) {
  when (iterationOrder) {
    IterationOrder.Ascending -> fastForEachIndexed(callback)
    IterationOrder.Descending -> fastForEachIndexedReversed(callback)
  }
}

inline fun DoubleArrayList.fastForEachIndexedReversed(callback: (index: Int, value: Double) -> Unit) {
  var n = lastIndex
  while (n >= 0) {
    callback(n, this.getAt(n))
    n--
  }
}

fun DoubleArrayList.fastAny(predicate: DoublePredicate): Boolean {
  var n = 0
  val currentSize = size
  while (n < currentSize) {
    if (predicate(this.getAt(n))) {
      return true
    }
    n++
  }

  return false
}
