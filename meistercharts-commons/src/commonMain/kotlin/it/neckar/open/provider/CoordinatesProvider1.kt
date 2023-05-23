package it.neckar.open.provider

/**
 * Provides coordinates.
 * Works like the [SizedProvider] but returns double values for each x and y.
 *
 * This class is an optimization that should be used to avoid boxing of double values.
 *
 * There exist variants for different number of parameters:
 * *[DoublesProvider1]: Takes one parameter
 */
interface CoordinatesProvider1<in P1> : HasSize1<P1>, MultiCoordinatesProvider1<SizedProviderIndex, P1> {

  companion object {
    val Empty: CoordinatesProvider1<Any> = object : CoordinatesProvider1<Any> {
      override fun size(param1: Any): Int {
        return 0
      }

      override fun xAt(index: Int, param1: Any): Double {
        throw UnsupportedOperationException()
      }

      override fun yAt(index: Int, param1: Any): Double {
        throw UnsupportedOperationException()
      }
    }
  }
}
