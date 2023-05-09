package it.neckar.open.collections

/**
 * Caches a single value
 */
class SingleCache<V> {

  var value: V? = null
    private set

  fun get(): V? {
    return value
  }

  fun set(value: V) {
    this.value = value
  }

  /**
   */
  fun getOrStore(provider: () -> V): V {
    return value ?: provider().also {
      this.value = it
    }
  }

  /**
   * Clears the cache
   */
  fun clear() {
    value = null
  }
}
