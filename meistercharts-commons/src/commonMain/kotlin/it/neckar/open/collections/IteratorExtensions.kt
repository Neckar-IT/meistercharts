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
 * Contains extension functions for Iterators
 *
 * Copied from https://github.com/LukasForst/katlib/blob/master/ (MIT License)
 */


/**
 * Returns the largest value of the given function or `null` if there are no elements.
 */
inline fun <T, R : Comparable<R>> Iterable<T>.maxValueBy(selector: (T) -> R): R? {
  val iterator = iterator()
  if (!iterator.hasNext()) return null
  var maxValue = selector(iterator.next())
  while (iterator.hasNext()) {
    val v = selector(iterator.next())
    if (maxValue < v) {
      maxValue = v
    }
  }
  return maxValue
}

/**
 * Returns the smallest value of the given function or `null` if there are no elements.
 */
inline fun <T, R : Comparable<R>> Iterable<T>.minValueBy(selector: (T) -> R): R? {
  val iterator = iterator()
  if (!iterator.hasNext()) return null
  var minValue = selector(iterator.next())
  while (iterator.hasNext()) {
    val v = selector(iterator.next())
    if (minValue > v) {
      minValue = v
    }
  }
  return minValue
}


/**
 * Performs the given [action] on each element that is not null.
 */
inline fun <T : Any> Iterable<T?>.forEachNotNull(action: (T) -> Unit) {
  for (element in this) element?.let(action)
}


/**
 * Returns the single element matching the given [predicate], or `null` if element was not found.
 *
 * Throws [IllegalArgumentException] when multiple elements are matching predicate.
 */
inline fun <T> Iterable<T>.singleOrEmpty(predicate: (T) -> Boolean): T? {
  var single: T? = null
  var found = false
  for (element in this) {
    if (predicate(element)) {
      if (found) {
        throw IllegalArgumentException("Collection contains more than one matching element.")
      }
      single = element
      found = true
    }
  }
  return single
}


/**
 * Returns single element, or `null` if the collection is empty.
 * Throws [IllegalArgumentException] when multiple elements are matching predicate.
 */
fun <T> Iterable<T>.singleOrEmpty(): T? =
  when (this) {
    is List ->
      when (size) {
        0 -> null
        1 -> this[0]
        else -> throw IllegalArgumentException("Collection contains more than one element.")
      }

    else -> {
      val iterator = iterator()
      if (!iterator.hasNext()) {
        null
      } else {
        val single = iterator.next()
        if (iterator.hasNext()) {
          throw IllegalArgumentException("Collection contains more than one element.")
        }
        single
      }
    }
  }
