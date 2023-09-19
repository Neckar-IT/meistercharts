package it.neckar.open.collections

import java.util.Collections

/**
 * Wraps this map with a synchronized object
 */
actual fun <K, V> MutableMap<K, V>.synchronizedIfNecessary(): MutableMap<K, V> {
  return Collections.synchronizedMap(this)
}
