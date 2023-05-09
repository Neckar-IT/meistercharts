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
