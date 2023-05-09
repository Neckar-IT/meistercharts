package it.neckar.open.provider

import it.neckar.open.collections.getModulo

/**
 * Provides multiple doubles.
 *
 * This is a "copy" of [MultiProvider] to avoid boxing
 *
 * Annotate [IndexContext] with [MultiProviderIndexContextAnnotation]
 */
fun interface MultiDoublesProvider<in IndexContext> {
  /**
   * Retrieves the value at the given [index].
   *
   * Please use extension methods with the correct type instead (if possible)
   */
  fun valueAt(index: Int): Double

  companion object {
    /**
     * Always returns the given value
     */
    fun <IndexContext> always(value: Double): MultiDoublesProvider<IndexContext> {
      return MultiDoublesProvider { value }
    }

    /**
     * Returns the element from the given values array using module if an index is requested,
     * that is larger than the provided array.
     */
    fun <IndexContext> forArrayModulo(values: DoubleArray): MultiDoublesProvider<IndexContext> {
      return MultiDoublesProvider { index -> values.getModulo(index) }
    }
  }
}
