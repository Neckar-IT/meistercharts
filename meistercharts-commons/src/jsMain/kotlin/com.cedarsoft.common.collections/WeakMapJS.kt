package it.neckar.open.collections

/**
 * A weak hash map implementation for JS
 */

@JsName("WeakMap")
external class WeakMapJS {
  fun has(k: dynamic): Boolean
  fun set(k: dynamic, v: dynamic)
  fun get(k: dynamic): dynamic
}

@Suppress("MatchingDeclarationName")
actual class WeakMap<K : Any, V> {
  private val backingMap = WeakMapJS()

  actual operator fun contains(key: K): Boolean = backingMap.has(key)

  actual operator fun set(key: K, value: V) {
    if (key is String) error("Can't use String as WeakMap keys")
    backingMap.set(key, value)
  }

  actual operator fun get(key: K): V? = backingMap.get(key).unsafeCast<V?>()
}

@JsName("WeakRef")
external class WeakRefJS(referent: dynamic) {
  fun deref(): dynamic
}


actual class WeakReference<T> actual constructor(referent: T) {
  private val backingRef = WeakRefJS(referent)

  /**
   * Returns the referent or null if the referent has been garbage collected
   */
  actual fun get(): T? {
    return backingRef.deref().unsafeCast<T>()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is WeakReference<*>) return false

    if (backingRef != other.backingRef) return false

    return true
  }

  override fun hashCode(): Int {
    return backingRef.hashCode()
  }
}
