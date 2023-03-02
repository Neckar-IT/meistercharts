package com.meistercharts.history

import it.neckar.open.provider.HasSize
import it.neckar.open.provider.IndexProvider
import it.neckar.open.provider.IntProvider
import it.neckar.open.provider.MultiProvider
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlin.reflect.KProperty0

/**
 * Represents a data series index - for *enum* values
 * If an index has to be used as [Int] (e.g. in a provider), [EnumDataSeriesIndexInt] should be used to annotate the [Int]s
 */
@JvmInline
value class EnumDataSeriesIndex(val value: Int) : DataSeriesIndex {
  companion object {
    val zero: EnumDataSeriesIndex = EnumDataSeriesIndex(0)
    val one: EnumDataSeriesIndex = EnumDataSeriesIndex(1)
    val two: EnumDataSeriesIndex = EnumDataSeriesIndex(2)
    val three: EnumDataSeriesIndex = EnumDataSeriesIndex(3)
  }
}

/**
 * This is a copy of [IndexProvider] - to avoid unnecessary boxing
 */
interface EnumDataSeriesIndexProvider : HasSize {
  /**
   * Retrieves the index at the given [index].
   *
   * @param index a value between 0 (inclusive) and [size] (exclusive)
   */
  fun valueAt(index: Int): EnumDataSeriesIndex

  companion object {
    /**
     * An empty provider that does not return any values
     */
    fun empty(): EnumDataSeriesIndexProvider {
      return empty
    }

    private val empty: EnumDataSeriesIndexProvider = object : EnumDataSeriesIndexProvider {
      override fun size(): Int = 0

      override fun valueAt(index: Int): EnumDataSeriesIndex {
        throw UnsupportedOperationException("Must not be called")
      }
    }

    /**
     * Creates a new index with the provided count of indices
     */
    fun indices(count: () -> Int): EnumDataSeriesIndexProvider {
      return object : EnumDataSeriesIndexProvider {
        override fun valueAt(index: Int): EnumDataSeriesIndex {
          return EnumDataSeriesIndex(index)
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
    fun forList(values: List<EnumDataSeriesIndex>): EnumDataSeriesIndexProvider {
      return object : EnumDataSeriesIndexProvider {
        override fun valueAt(index: Int): EnumDataSeriesIndex {
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
inline fun EnumDataSeriesIndexProvider.fastForEachIndexed(callback: (i: Int, value: EnumDataSeriesIndex) -> Unit) {
  val currentSize = size()
  fastForEachIndexed(currentSize, callback)
}

/**
 * Iterates over the provided indices - but with the provided max size
 */
inline fun EnumDataSeriesIndexProvider.fastForEachIndexed(maxSize: Int, callback: (i: Int, value: EnumDataSeriesIndex) -> Unit) {
  val size = size().coerceAtMost(maxSize)

  var n = 0
  while (n < size) {
    callback(n, this.valueAt(n))
    n++
  }
}

inline fun EnumDataSeriesIndexProvider.fastForEach(callback: (index: EnumDataSeriesIndex) -> Unit) {
  var n = 0
  val currentSize = size()
  while (n < currentSize) {
    callback(this.valueAt(n++))
  }
}


/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun KProperty0<EnumDataSeriesIndexProvider>.delegate(): EnumDataSeriesIndexProvider {
  return DelegatingEnumDataSeriesIndexProvider { get() }
}

/**
 * A sized provider that delegates the calls
 */
open class DelegatingEnumDataSeriesIndexProvider(
  val delegateProvider: () -> EnumDataSeriesIndexProvider,
) : EnumDataSeriesIndexProvider {
  override fun size(): Int = delegateProvider().size()

  override fun valueAt(index: Int): EnumDataSeriesIndex {
    return delegateProvider().valueAt(index)
  }
}

inline fun <T> MultiProvider<EnumDataSeriesIndex, T>.valueAt(index: EnumDataSeriesIndex): T {
  return this.valueAt(index.value)
}


/**
 * Returns a new instance of [EnumDataSeriesIndexProvider] that has a size that is not greater than the provided value
 */
fun EnumDataSeriesIndexProvider.atMost(maxSizeProvider: IntProvider): EnumDataSeriesIndexProvider {
  val delegateProvider = { this }

  return atMost(delegateProvider, maxSizeProvider)
}

/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun KProperty0<EnumDataSeriesIndexProvider>.atMost(maxSizeProvider: IntProvider): EnumDataSeriesIndexProvider {
  return atMost({ get() }, maxSizeProvider)
}

private fun atMost(delegateProvider: () -> EnumDataSeriesIndexProvider, maxSizeProvider: IntProvider): DelegatingEnumDataSeriesIndexProvider {
  return object : DelegatingEnumDataSeriesIndexProvider(delegateProvider) {
    override fun size(): Int {
      return super.size().coerceAtMost(maxSizeProvider())
    }
  }
}
