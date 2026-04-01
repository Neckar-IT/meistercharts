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
 * A queue which automatically evicts elements from the head of the queue when
 * attempting to add new elements onto the queue and it is full
 */
class EvictingQueue<E>(
  val maxSize: Int
) : MutableCollection<E> {
  private val delegate: ArrayDeque<E> = ArrayDeque()

  init {
    require(maxSize > 0) { "Max size must be > 0 but was <$maxSize>" }
  }

  /**
   * Returns the remaining capacity for new elements before the queue starts evicting.
   */
  fun remainingCapacity(): Int {
    return maxSize - size
  }

  override val size: Int
    get() = delegate.size

  override
  fun contains(element: E): Boolean {
    return delegate.contains(element)
  }

  override fun containsAll(elements: Collection<E>): Boolean {
    return delegate.containsAll(elements)
  }

  override fun isEmpty(): Boolean {
    return delegate.isEmpty()
  }

  override fun iterator(): MutableIterator<E> {
    return delegate.iterator()
  }

  override fun add(element: E): Boolean {
    while (size >= maxSize) {
      delegate.removeFirst()
    }

    return delegate.add(element)
  }

  override fun addAll(elements: Collection<E>): Boolean {
    if (elements.isEmpty()) {
      return false
    }

    //Trying to add same or more elements than the size
    if (elements.size >= maxSize) {
      clear()

      return this.delegate.addAll(
        elements
          .drop(elements.size - maxSize)
      )
    }

    //Default case - not too many elements
    while (size > (maxSize - elements.size)) {
      delegate.removeFirst()
    }
    return delegate.addAll(elements)
  }

  override fun clear() {
    return delegate.clear()
  }

  override fun remove(element: E): Boolean {
    return delegate.remove(element)
  }

  override fun removeAll(elements: Collection<E>): Boolean {
    return delegate.removeAll(elements)
  }

  override fun retainAll(elements: Collection<E>): Boolean {
    return delegate.retainAll(elements)
  }
}
