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

import java.util.WeakHashMap

/**
 * A weak map implementation for JS/JVM with weak *keys*
 */
@Suppress("MatchingDeclarationName")
actual class WeakMap<K : Any, V> {
  val backingMap: WeakHashMap<K, V> = WeakHashMap()

  actual operator fun contains(key: K): Boolean = backingMap.containsKey(key)

  actual operator fun set(key: K, value: V): Unit = run {
    if (key is String) error("Can't use String as WeakMap keys")
    backingMap[key] = value
  }

  actual operator fun get(key: K): V? = backingMap[key]

  fun isEmpty(): Boolean {
    return backingMap.isEmpty()
  }

  val size: Int
    get() {
      return backingMap.size
    }
}


actual typealias WeakReference<T> = java.lang.ref.WeakReference<T>
