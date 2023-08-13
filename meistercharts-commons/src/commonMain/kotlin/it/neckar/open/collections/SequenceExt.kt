package it.neckar.open.collections


/**
 * Returns a sequence only containing the values
 */
inline fun <K, V> Sequence<Map.Entry<K, V>>.values(): Sequence<V> {
  return this.map { it.value }
}
