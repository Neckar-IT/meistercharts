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
 * A mutable sorted list
 */
class MutableSortedList<V> internal constructor(
  val comparator: Comparator<V> = naturalOrder<Comparable<Any>>() as Comparator<V>,
  private val decorated: MutableList<V> = mutableListOf()
) : MutableList<V> by decorated {

  override fun indexOf(element: V): Int {
    var index = decorated.binarySearch(element, comparator)
    return if (index < 0) -1 else index
  }

  override fun iterator(): MutableIterator<V> {
    return listIterator()
  }

  override fun lastIndexOf(element: V): Int {
    var index = decorated.binarySearch(element, comparator)
    if (index < 0) {
      return -1
    }
    while (index < decorated.size - 1 && decorated[index + 1] == element) {
      ++index
    }
    return index
  }

  fun firstIndexOf(element: V): Int {
    var index = decorated.binarySearch(element, comparator)
    if (index < 0) {
      return -1
    }
    while (index > 0 && decorated[index - 1] == element) {
      --index
    }
    return index
  }

  override fun add(element: V): Boolean {
    var index = decorated.binarySearch(element, comparator)
    if (index < 0) {
      index = -index - 1
    }
    decorated.add(index, element)
    return true
  }

  override fun add(index: Int, element: V) {
    require(index > -1) { "index must be greater than -1 but was $index" }
    require(index <= size) { "index must be smaller than $size but was $index" }
    require(index == size || comparator.compare(this[index], element) >= 0)
    require(index == 0 || comparator.compare(this[index - 1], element) <= 0)
    decorated.add(index, element)
  }

  override fun addAll(index: Int, elements: Collection<V>): Boolean {
    require(index > -1) { "index must be greater than -1 but was $index" }
    require(index <= size) { "index must be smaller than $size but was $index" }

    if (elements.isEmpty()) {
      return false
    }
    val sortedList = elements.sortedWith(comparator)
    require(index == size || comparator.compare(get(index), sortedList.last()) >= 0)
    require(index == 0 || comparator.compare(get(index - 1), sortedList.first()) <= 0)
    return decorated.addAll(index, sortedList)
  }

  override fun addAll(elements: Collection<V>): Boolean {
    if (elements.isEmpty()) {
      return false
    }
    val sortedList = elements.sortedWith(comparator)
    var indexFrom = 0
    for (element in sortedList) {
      var index = decorated.binarySearch(element, comparator, indexFrom)
      if (index < 0) {
        index = -index - 1
      }
      decorated.add(index, element)
      indexFrom = index
    }
    return true
  }

  override fun listIterator(): MutableListIterator<V> {
    return listIterator(0)
  }

  override fun listIterator(index: Int): MutableListIterator<V> {
    return object : MutableListIterator<V> {

      var cursor: Int = index
      var indexLastReturned: Int = -1

      override fun add(element: V) {
        add(cursor++, element)
        indexLastReturned = -1
      }

      override fun hasNext(): Boolean {
        return cursor != size
      }

      override fun hasPrevious(): Boolean {
        return cursor > 0
      }

      override fun next(): V {
        val result = get(cursor)
        indexLastReturned = cursor
        ++cursor
        return result
      }

      override fun nextIndex(): Int {
        return cursor
      }

      override fun previous(): V {
        val result = get(cursor - 1)
        --cursor
        indexLastReturned = cursor
        return result
      }

      override fun previousIndex(): Int {
        return cursor - 1
      }

      override fun remove() {
        check(indexLastReturned >= 0)
        removeAt(indexLastReturned)
        cursor = indexLastReturned
        indexLastReturned = -1
      }

      override fun set(element: V) {
        check(indexLastReturned < 0)
        set(indexLastReturned, element)
      }
    }
  }

  override fun set(index: Int, element: V): V {
    require(index > -1) { "index must be greater than -1 but was $index" }
    require(index < size) { "index must be smaller than $size but was $index" }
    require(index == size - 1 || comparator.compare(this[index + 1], element) >= 0)
    require(index == 0 || comparator.compare(this[index - 1], element) <= 0)
    return decorated.set(index, element)
  }

  override fun subList(fromIndex: Int, toIndex: Int): MutableList<V> {
    throw UnsupportedOperationException("not supported yet")
  }

  override fun toString(): String {
    return "MutableSortedList($decorated)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as MutableSortedList<*>

    if (decorated != other.decorated) return false

    return true
  }

  override fun hashCode(): Int {
    return decorated.hashCode()
  }

}

fun <T> mutableSortedListOf(comparator: Comparator<T>): MutableSortedList<T> = MutableSortedList(comparator)

fun <T : Comparable<T>> mutableSortedListOf(): MutableSortedList<T> = MutableSortedList()
