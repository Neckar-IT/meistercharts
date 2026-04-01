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
 * A weak map implementation for JS/JVM - with weak *keys*
 */
expect class WeakMap<K : Any, V>() {
  /**
   * Returns true if this map contains a value for the given key
   */
  operator fun contains(key: K): Boolean

  /**
   * Stores a value with the given key
   */
  operator fun set(key: K, value: V)

  /**
   * Returns the value for the given key
   */
  operator fun get(key: K): V?
}

/**
 * A weak set, meaning references to elements are held weakly.
 *
 * Note that the JavaScript implementation supports only objects as elements.
 */
expect class WeakSet<T>() {
  /**
   * Adds the given [element] to this set
   */
  fun add(element: T)

  /**
   * Removes the specified [element] from this set
   *
   * @return true if [element] in the set has been removed successfully else false.
   */
  fun remove(element: T): Boolean

  /**
   * Returns true if this set contains the given [element] else false
   */
  operator fun contains(element: T): Boolean
}

/**
 * Gets the value that is stored in the map or puts the given value
 */
fun <K : Any, V> WeakMap<K, V>.getOrPut(key: K, value: (K) -> V): V {
  if (key !in this) this[key] = value(key)
  return this[key]!!
}

@Deprecated("not supported by Safari", ReplaceWith("use WeakMap or WeakSet"))
expect class WeakReference<T>(referent: T) {
  /**
   * Returns the referent or null if the referent has been garbage collected
   */
  fun get(): T?
}
