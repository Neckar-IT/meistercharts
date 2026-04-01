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
 * Simple implementation of a linked list
 */
class LinkedList<T> {
  /**
   * The first element
   */
  var first: Element<T>? = null
  /**
   * The last element
   */
  var last: Element<T>? = null

  /**
   * The value of the first element
   */
  val firstValue: T? get() = first?.value
  /**
   * The value of the last element
   */
  val lastValue: T? get() = last?.value

  /**
   * Calculates the list length
   */
  fun getLength(): Int {
    var length = 0
    if (first == null) {
      return length
    }

    length++

    var check = first
    while (check?.next != null) {
      length++
      check = check.next
    }

    return length
  }

  /**
   * Add an element at first position
   */
  fun addFirst(value: T) {
    when (first) {
      null -> addWhenEmpty(value)
      else -> {
        val currentFirst = first
        first = Element(value, null, currentFirst)
        currentFirst?.previous = first
      }
    }
  }

  /**
   * Adds a new value at the end
   */
  fun addLast(value: T) {
    when (last) {
      null -> addWhenEmpty(value)
      else -> {
        val currentLast = last
        last = Element(value, currentLast, null)
        currentLast?.next = last
      }
    }
  }

  private fun addWhenEmpty(value: T) {
    val newElement = Element(value, null, null)
    first = newElement
    last = newElement
  }


  /**
   * Removes and returns the last value
   */
  fun pollLast(): T? {
    val currentLast = last
    last = last?.previous
    last?.next = null

    if (last == null) {
      first = null
    }
    return currentLast?.value
  }

  /**
   * Removes and returns the first value
   */
  fun pollFirst(): T? {
    val currentFirst = first
    first = first?.next
    first?.previous = null

    if (first == null) {
      last = null
    }
    return currentFirst?.value
  }

  /**
   * Clears the list
   */
  fun clear() {
    first = null
    last = null
  }
}

/**
 * Represents one element within the linked list
 */
data class Element<T>(
  val value: T,
  var previous: Element<T>?,
  var next: Element<T>?


) {
  override fun toString(): String {
    return "element: $value, hasPrevious: ${previous != null}, hasNext: ${next != null}"
  }
}

