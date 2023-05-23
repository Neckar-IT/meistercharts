package it.neckar.open.provider

/**
 * Base interface for providers that have a size
 *
 * @see [SizedProvider1] - which does have a size
 * @see [DoublesProvider1] - which does have a size - special implementation that returns Doubles and avoids boxing
 * @see [MultiProvider1] - which does *not* have a size
 */
interface HasSize1<in P1> {
  /**
   * The number of available elements
   */
  fun size(param1: P1): Int

  /**
   * Returns true if there are no elements available
   */
  fun isEmpty(param1: P1): Boolean {
    return size(param1) <= 0
  }
}
