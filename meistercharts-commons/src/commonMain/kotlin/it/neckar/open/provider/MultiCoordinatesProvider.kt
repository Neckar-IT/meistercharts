package it.neckar.open.provider

import it.neckar.open.annotations.NotBoxed

/**
 * Provides coordinates - as one x and one y double.
 *
 * This is a "copy" of [MultiProvider] to avoid boxing
 *
 * Annotate [IndexContext] with [MultiProviderIndexContextAnnotation]
 */
interface MultiCoordinatesProvider<in IndexContext> {
  /**
   * Retrieves the x-value at the given [index].
   *
   * Please use extension methods with the correct type instead (if possible)
   */
  fun xAt(index: Int): @NotBoxed Double

  /**
   * Retrieves the y-value at the given [index].
   *
   * Please use extension methods with the correct type instead (if possible)
   */
  fun yAt(index: Int): @NotBoxed Double

  companion object {
    /**
     * Always returns the given value
     */
    fun <IndexContext> always(x: Double, y: Double): MultiCoordinatesProvider<IndexContext> {
      return object : MultiCoordinatesProvider<IndexContext> {
        override fun xAt(index: Int): @NotBoxed Double {
          return x
        }

        override fun yAt(index: Int): @NotBoxed Double {
          return y
        }
      }
    }
  }
}
