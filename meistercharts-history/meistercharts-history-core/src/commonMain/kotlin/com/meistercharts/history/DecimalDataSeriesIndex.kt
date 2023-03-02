package com.meistercharts.history

import it.neckar.open.provider.HasSize
import it.neckar.open.provider.IndexProvider
import it.neckar.open.provider.IntProvider
import it.neckar.open.provider.MultiProvider
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlin.reflect.KProperty0

/**
 * Represents a data series index - for *decimal* values.
 *
 * If an index has to be used as [Int] (e.g. in a provider), [DecimalDataSeriesIndexInt] should be used to annotate the [Int]s
 */
@JvmInline
value class DecimalDataSeriesIndex(val value: Int) : DataSeriesIndex {
  override fun toString(): String {
    return value.toString()
  }

  companion object {
    val zero: DecimalDataSeriesIndex = DecimalDataSeriesIndex(0)
    val one: DecimalDataSeriesIndex = DecimalDataSeriesIndex(1)
    val two: DecimalDataSeriesIndex = DecimalDataSeriesIndex(2)
    val three: DecimalDataSeriesIndex = DecimalDataSeriesIndex(3)
  }
}

/**
 * This is a copy of [IndexProvider] - to avoid unnecessary boxing
 */
interface DecimalDataSeriesIndexProvider : HasSize {
  /**
   * Retrieves the index at the given [index].
   *
   * @param index a value between 0 (inclusive) and [size] (exclusive)
   */
  fun valueAt(index: Int): DecimalDataSeriesIndex

  companion object {
    /**
     * An empty provider that does not return any values
     */
    fun empty(): DecimalDataSeriesIndexProvider {
      return empty
    }

    private val empty: DecimalDataSeriesIndexProvider = object : DecimalDataSeriesIndexProvider {
      override fun size(): Int = 0

      override fun valueAt(index: Int): DecimalDataSeriesIndex {
        throw UnsupportedOperationException("Must not be called")
      }
    }

    /**
     * Creates a new index with the provided count of indices
     */
    fun indices(count: () -> Int): DecimalDataSeriesIndexProvider {
      return object : DecimalDataSeriesIndexProvider {
        override fun valueAt(index: Int): DecimalDataSeriesIndex {
          return DecimalDataSeriesIndex(index)
        }

        override fun size(): Int {
          return count()
        }
      }
    }

    /**
     * ATTENTION! Use forValues instead (if possible).
     * This method should only be used in very rare cases because of boxing!
     */
    @JvmStatic
    fun forList(values: List<DecimalDataSeriesIndex>): DecimalDataSeriesIndexProvider {
      return object : DecimalDataSeriesIndexProvider {
        override fun valueAt(index: Int): DecimalDataSeriesIndex {
          return values[index]
        }

        override fun size(): Int {
          return values.size
        }
      }
    }
  }
}

/**
 * Iterates over the provided indices.
 * This method is only useful in very few cases!
 *
 * ATTENTION: "i" is very different to the provided index
 */
inline fun DecimalDataSeriesIndexProvider.fastForEachIndexed(callback: (i: Int, value: DecimalDataSeriesIndex) -> Unit) {
  var n = 0
  val currentSize = size()
  while (n < currentSize) {
    callback(n, this.valueAt(n))
    n++
  }
}

inline fun DecimalDataSeriesIndexProvider.fastForEach(callback: (index: DecimalDataSeriesIndex) -> Unit) {
  var n = 0
  val currentSize = size()
  while (n < currentSize) {
    callback(this.valueAt(n++))
  }
}


/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun KProperty0<DecimalDataSeriesIndexProvider>.delegate(): DecimalDataSeriesIndexProvider {
  return DelegatingDecimalDataSeriesIndexProvider { get() }
}

/**
 * A sized provider that delegates the calls
 */
open class DelegatingDecimalDataSeriesIndexProvider(
  val delegateProvider: () -> DecimalDataSeriesIndexProvider,
) : DecimalDataSeriesIndexProvider {
  override fun size(): Int = delegateProvider().size()

  override fun valueAt(index: Int): DecimalDataSeriesIndex {
    return delegateProvider().valueAt(index)
  }
}

inline fun <T> MultiProvider<DecimalDataSeriesIndex, T>.valueAt(index: DecimalDataSeriesIndex): T {
  return this.valueAt(index.value)
}

/**
 * Returns a new instance of [DecimalDataSeriesIndexProvider] that has a size that is not greater than the provided value
 */
fun DecimalDataSeriesIndexProvider.atMost(maxSizeProvider: IntProvider): DecimalDataSeriesIndexProvider {
  val delegateProvider = { this }

  return atMost(delegateProvider, maxSizeProvider)
}

/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun KProperty0<DecimalDataSeriesIndexProvider>.atMost(maxSizeProvider: IntProvider): DecimalDataSeriesIndexProvider {
  return atMost({ get() }, maxSizeProvider)
}

private fun atMost(delegateProvider: () -> DecimalDataSeriesIndexProvider, maxSizeProvider: IntProvider): DelegatingDecimalDataSeriesIndexProvider {
  return object : DelegatingDecimalDataSeriesIndexProvider(delegateProvider) {
    override fun size(): Int {
      return super.size().coerceAtMost(maxSizeProvider())
    }
  }
}
