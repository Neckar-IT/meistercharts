package it.neckar.open.collections

/**
 * JS does not require synchronization. Therefore, just return this
 */
actual fun <K, V> MutableMap<K, V>.synchronizedIfNecessary(): MutableMap<K, V> {
  return this
}
