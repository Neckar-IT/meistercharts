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

open class CacheMap<K, V> private constructor(
  private val map: LinkedHashMap<K, V> = LinkedHashMap(),
  maxSize: Int = 16,
  val free: (K, V) -> Unit = { _, _ -> }
) : MutableMap<K, V> by map {
  constructor(
    maxSize: Int = 16,
    free: (K, V) -> Unit = { _, _ -> }
  ) : this(LinkedHashMap(), maxSize, free)

  override val size: Int get() = map.size

  var maxSize: Int = maxSize
    private set
  fun updateMaxSize(newMaxSize: Int) {
    this.maxSize = newMaxSize

    //Reduce the size of the map
    while (size >= maxSize) remove(map.keys.first())
  }

  /**
   * Marks the entry with the given [key] as new
   */
  fun markAsNew(key: K) {
    // do not call 'free' here
    val value = map.remove(key)
    value?.let {
      map[key] = value
    }
  }

  override fun remove(key: K): V? {
    val value = map.remove(key)
    if (value != null) free(key, value)
    return value
  }

  override fun putAll(from: Map<out K, V>) = run { for ((k, v) in from) put(k, v) }
  override fun put(key: K, value: V): V? {
    while (size >= maxSize && map.containsKey(key).not()) {
      val keys = map.keys
      require(keys.size == size){
        "Expect same size for keys: <${keys.size}> and map: <$size>"
      }
      remove(keys.first())
    }

    val oldValue = map[key]
    if (oldValue != value) {
      remove(key) // remove entry first to force a refresh when the new value is put into the map
      map[key] = value
    }
    return oldValue
  }

  override fun clear() {
    val keys = map.keys.toList()
    for (key in keys) remove(key)
  }

  /**
   * Removes all entries the provided predicate returns true for
   */
  fun removeIf(predicate: (K) -> Boolean) {
    val keysToRemove = map.keys.filter(predicate)

    keysToRemove.fastForEach {
      map.remove(it)
    }
  }

  override fun toString(): String = map.toString()

  override fun equals(other: Any?): Boolean = (other is CacheMap<*, *>) && (this.map == other.map) && (this.free == other.free)
  override fun hashCode(): Int = map.hashCode() * 31 + free.hashCode()
}
