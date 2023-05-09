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
