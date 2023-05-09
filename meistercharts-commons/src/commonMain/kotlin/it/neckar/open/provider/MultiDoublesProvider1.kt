package it.neckar.open.provider

import it.neckar.open.collections.getModulo

/**
 * Provides multiple doubles.
 *
 * This is a "copy" of [MultiProvider] to avoid boxing
 *
 * Annotate [IndexContext] with [MultiProviderIndexContextAnnotation]
 */
fun interface MultiDoublesProvider1<in IndexContext, in P1> {
  /**
   * Retrieves the value at the given [index].
   *
   * Please use extension methods with the correct type instead (if possible)
   */
  fun valueAt(index: Int, param1: P1): Double

  companion object {
    /**
     * Always returns the given value
     */
    fun <IndexContext, P1> always(value: Double): MultiDoublesProvider1<IndexContext, P1> {
      return MultiDoublesProvider1 { _, _ -> value }
    }

    /**
     * Returns the element from the given values array using module if an index is requested,
     * that is larger than the provided array.
     */
    fun <IndexContext, P1> forArrayModulo(values: DoubleArray): MultiDoublesProvider1<IndexContext, P1> {
      return MultiDoublesProvider1 { index, _ -> values.getModulo(index) }
    }
  }
}
