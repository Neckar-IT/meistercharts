package it.neckar.open.provider

import com.meistercharts.annotations.Window
import it.neckar.open.annotations.NotBoxed

/**
 * Provides coordinates.
 * Works like the [SizedProvider] but returns double values for each x and y.
 *
 * This class is an optimization that should be used to avoid boxing of double values.
 *
 * There exist variants for different number of parameters:
 * *[DoublesProvider1]: Takes one parameter
 */
interface CoordinatesProvider : HasSize, MultiCoordinatesProvider<SizedProviderIndex> {

  /**
   * Creates a new instance of a CoordinatesProvider1 that simply ignores the parameter
   */
  fun as1(): @Window CoordinatesProvider1<Any> {
    return object : CoordinatesProvider1<Any> {
      override fun size(param1: Any): Int {
        return this@CoordinatesProvider.size()
      }

      override fun xAt(index: Int, param1: Any): @NotBoxed Double {
        return this@CoordinatesProvider.xAt(index)
      }

      override fun yAt(index: Int, param1: Any): @NotBoxed Double {
        return this@CoordinatesProvider.yAt(index)
      }
    }
  }

  companion object {
    /**
     * An empty values provider that does not return any values
     */
    val empty: CoordinatesProvider = object : CoordinatesProvider {
      override fun size(): Int = 0

      override fun xAt(index: Int): @NotBoxed Double {
        throw UnsupportedOperationException("Must not be called")
      }

      override fun yAt(index: Int): @NotBoxed Double {
        throw UnsupportedOperationException("Must not be called")
      }
    }

    /**
     * Returns a double provider with a fixed size - the values are returned by the provider
     */
    fun Companion.fixedSize(size: Int, provider: MultiCoordinatesProvider<Int>): CoordinatesProvider {
      return object : CoordinatesProvider {
        override fun size(): Int {
          return size
        }

        override fun xAt(index: Int): @NotBoxed Double {
          return provider.xAt(index)
        }

        override fun yAt(index: Int): @NotBoxed Double {
          return provider.yAt(index)
        }
      }
    }
  }
}
