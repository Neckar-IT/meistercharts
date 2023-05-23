package it.neckar.open.provider

/**
 * Base interface for providers that have a size
 *
 * @see [SizedProvider2] - which does have a size
 * @see [MultiProvider2] - which does *not* have a size
 */
interface HasSize2<in P1, in P2> {
  /**
   * The number of available elements
   */
  fun size(param1: P1, param2: P2): Int

  /**
   * Returns true if there are no elements available
   */
  fun isEmpty(param1: P1, param2: P2): Boolean {
    return size(param1, param2) <= 0
  }
}
