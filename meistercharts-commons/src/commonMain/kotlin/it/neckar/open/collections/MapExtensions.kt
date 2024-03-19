package it.neckar.open.collections

/**
 * Contains extension functions for Maps
 *
 * Copied from https://github.com/LukasForst/katlib/blob/master/ (MIT License)
 */


/**
 * Joins two maps together using the given [join] function.
 */
fun <K, V1, V2, VR> Map<K, V1>.join(other: Map<K, V2>, join: (V1?, V2?) -> VR): Map<K, VR> {
  return joinTo(LinkedHashMap(this.size + other.size), other, join)
}

/**
 * Joins two maps together using the given [join] function into the given [destination].
 */
fun <K, V1, V2, VR, M : MutableMap<K, VR>> Map<K, V1>.joinTo(destination: M, other: Map<K, V2>, join: (V1?, V2?) -> VR): M {
  val keys = this.keys + other.keys
  for (key in keys) {
    destination[key] = join(this[key], other[key])
  }
  return destination
}


/**
 * Swaps dimensions in two dimensional map. The returned map has keys from the second dimension as primary keys and primary keys are used in the second
 * dimension.
 */
fun <K1, K2, V> Map<K1, Map<K2, V>>.swapKeys(): Map<K2, Map<K1, V>> = swapKeysTo(LinkedHashMap()) { LinkedHashMap<K1, V>() }

/**
 * Swaps dimensions in two dimensional map. The returned map has keys from the second dimension as primary keys and primary keys are stored in the second
 * dimension. [topDestination] specifies which map should be used to store the new primary keys and [bottomDestination] is used to store the new secondary keys.
 */
fun <K1, K2, V, M2 : MutableMap<K1, V>, M1 : MutableMap<K2, M2>> Map<K1, Map<K2, V>>.swapKeysTo(
  topDestination: M1,
  bottomDestination: () -> M2,
): M1 {
  for ((key1, map) in this) {
    for ((key2, value) in map) {
      topDestination.getOrPut(key2, bottomDestination)[key1] = value
    }
  }
  return topDestination
}
