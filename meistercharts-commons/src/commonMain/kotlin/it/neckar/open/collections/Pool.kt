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
 * Structure containing a set of reusable objects.
 *
 * The method [alloc] retrieves from the pool or allocates a new object,
 * while the [free] method pushes back one element to the pool and resets it to reuse it.
 */
class Pool<T>(private val reset: (T) -> Unit = {}, preallocate: Int = 0, private val gen: (Int) -> T) {
  companion object {
    fun <T : Poolable> fromPoolable(preallocate: Int = 0, gen: (Int) -> T): Pool<T> =
      Pool(reset = { it.reset() }, preallocate = preallocate, gen = gen)
  }

  constructor(preallocate: Int = 0, gen: (Int) -> T) : this({}, preallocate, gen)

  private val items = Stack<T>()
  private var lastId = 0

  val totalAllocatedItems get() = lastId
  val totalItemsInUse get() = totalAllocatedItems - itemsInPool
  val itemsInPool: Int get() = items.size

  init {
    for (n in 0 until preallocate) items.push(gen(lastId++))
  }

  fun alloc(): T {
    return if (items.isNotEmpty()) items.pop() else gen(lastId++)
  }

  interface Poolable {
    fun reset()
  }

  fun free(element: T) {
    reset(element)
    items.push(element)
  }

  fun free(vararg elements: T) {
    elements.fastForEach { free(it) }
  }

  fun free(elements: Iterable<T>) {
    for (element in elements) free(element)
  }

  inline operator fun <R> invoke(callback: (T) -> R): R = alloc(callback)

  inline fun <R> alloc(callback: (T) -> R): R {
    val temp = alloc()
    try {
      return callback(temp)
    } finally {
      free(temp)
    }
  }

  inline fun <R> allocThis(callback: T.() -> R): R {
    val temp = alloc()
    try {
      return callback(temp)
    } finally {
      free(temp)
    }
  }

  override fun hashCode(): Int = items.hashCode()
  override fun equals(other: Any?): Boolean = (other is Pool<*>) && this.items == other.items && this.itemsInPool == other.itemsInPool
}
