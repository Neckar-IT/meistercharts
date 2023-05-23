package it.neckar.open.provider

import com.meistercharts.annotations.Domain
import it.neckar.open.kotlin.lang.DoubleMapFunction
import kotlin.reflect.KProperty0

/**
 * A provider that takes one parameter and provides multiple doubles
 */
interface DoublesProvider1<in P1> : HasSize1<P1>, MultiDoublesProvider1<SizedProviderIndex, P1> {
  /**
   * Computes the sum of all values.
   *
   * Returns 0.0 if there are no values.
   */
  fun sum(param1: P1): Double {
    var sum = 0.0
    for (index in 0 until size(param1)) {
      sum += valueAt(index, param1)
    }
    return sum
  }

  /**
   * Creates a new doubles provider with a fixed parameter
   */
  fun asDoublesProvider(param: P1): DoublesProvider {
    return FixedParamsDoublesProvider(param, this)
  }

  companion object {
    /**
     * An empty values provider that does not return any values
     */
    fun <P1> empty(): DoublesProvider1<P1> {
      return object : DoublesProvider1<P1> {
        override fun size(param1: P1): Int = 0

        override fun valueAt(index: Int, param1: P1): Double {
          throw UnsupportedOperationException("Must not be called")
        }
      }
    }
  }
}

/**
 * Converts a [DoublesProvider] to a [DoublesProvider1]
 */
fun <P1> DoublesProvider.asDoublesProvider1(): DoublesProvider1<P1> {
  val delegate = this

  return object : DoublesProvider1<P1> {
    override fun valueAt(index: Int, param1: P1): Double {
      return delegate.valueAt(index)
    }

    override fun size(param1: P1): Int {
      return delegate.size()
    }
  }
}

/**
 * Creates a fixed params doubles provider that always uses the current value for this property
 */
fun <P1> KProperty0<DoublesProvider1<P1>>.asDoublesProvider(param1: P1): @Domain DoublesProvider {
  return FixedParamsDoublesProvider(param1) {
    get()
  }
}

/**
 * Delegates calls to a [DoublesProvider1] with a fixed parameter
 */
class FixedParamsDoublesProvider<P1>(
  val param1: P1,
  /**
   * Provides the delegate.
   * ATTENTION: This method is called for each call to [size] and [valueAt].
   * It must be ensured that always the correct delegate is returned.
   */
  val delegate: () -> DoublesProvider1<P1>,
) : DoublesProvider {

  constructor(
    param1: P1,
    delegate: DoublesProvider1<P1>,
  ) : this(param1, { delegate })

  override fun size(): Int {
    return delegate().size(param1)
  }

  override fun valueAt(index: Int): Double {
    return delegate().valueAt(index, param1)
  }
}

/**
 * Maps the value.
 *
 * ATTENTION: Creates a new instance!
 */
fun <R, P1> DoublesProvider1<P1>.mapped(function: DoubleMapFunction<R>): SizedProvider1<R, P1> {
  @Suppress("DuplicatedCode")
  return object : SizedProvider1<R, P1> {
    override fun size(param1: P1): Int {
      return this@mapped.size(param1)
    }

    override fun valueAt(index: Int, param1: P1): R {
      return function(this@mapped.valueAt(index, param1))
    }
  }
}

/**
 * Returns a [MultiProvider] that delegates all calls to the current value of this property
 */
fun <P1> KProperty0<DoublesProvider1<P1>>.delegate(): DoublesProvider1<P1> {
  return object : DoublesProvider1<P1> {
    override fun size(param1: P1): Int {
      return get().size(param1)
    }

    override fun valueAt(index: Int, param1: P1): Double {
      return get().valueAt(index, param1)
    }
  }
}
