package it.neckar.open.provider

/**
 * Base interface for providers that have a size
 *
 * @see [SizedProvider] - which does have a size
 * @see [DoublesProvider] - which does have a size - special implementation that returns Doubles and avoids boxing
 * @see [MultiProvider] - which does *not* have a size
 */
interface HasSize {
  /**
   * The number of available elements
   *
   * NOTE: Do not convert to val to keep the symmetry with [SizedProvider1] and [SizedProvider2]
   */
  fun size(): Int

  /**
   * Returns true if there are no elements available
   */
  fun isEmpty(): Boolean {
    return size() <= 0
  }
}

inline fun HasSize.isNotEmpty(): Boolean {
  return isEmpty().not()
}
