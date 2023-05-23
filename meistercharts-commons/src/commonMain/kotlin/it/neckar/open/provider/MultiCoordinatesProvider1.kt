package it.neckar.open.provider

/**
 * Provides coordinates - as one x and one y double.
 *
 * This is a "copy" of [MultiProvider] to avoid boxing
 *
 * Annotate [IndexContext] with [MultiProviderIndexContextAnnotation]
 */
interface MultiCoordinatesProvider1<in IndexContext, in P1> {
  /**
   * Retrieves the x-value at the given [index].
   *
   * Please use extension methods with the correct type instead (if possible)
   */
  fun xAt(index: Int, param1: P1): Double

  /**
   * Retrieves the y-value at the given [index].
   *
   * Please use extension methods with the correct type instead (if possible)
   */
  fun yAt(index: Int, param1: P1): Double
}
