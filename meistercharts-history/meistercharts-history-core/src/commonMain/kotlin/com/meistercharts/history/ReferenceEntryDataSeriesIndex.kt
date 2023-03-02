package com.meistercharts.history

import it.neckar.open.provider.HasSize
import it.neckar.open.provider.IndexProvider
import it.neckar.open.provider.IntProvider
import it.neckar.open.provider.MultiProvider
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlin.reflect.KProperty0

/**
 * Represents a data series index - for [ReferenceEntryId] values
 * If an index has to be used as [Int] (e.g. in a provider), [ReferenceEntryDataSeriesIndex] should be used to annotate the [Int]s
 */
@JvmInline
value class ReferenceEntryDataSeriesIndex(val value: Int) : DataSeriesIndex {
  companion object {
    val zero: ReferenceEntryDataSeriesIndex = ReferenceEntryDataSeriesIndex(0)
    val one: ReferenceEntryDataSeriesIndex = ReferenceEntryDataSeriesIndex(1)
    val two: ReferenceEntryDataSeriesIndex = ReferenceEntryDataSeriesIndex(2)
    val three: ReferenceEntryDataSeriesIndex = ReferenceEntryDataSeriesIndex(3)
  }
}

/**
 * This is a copy of [IndexProvider] - to avoid unnecessary boxing
 */
interface ReferenceEntryDataSeriesIndexProvider : HasSize {
  /**
   * Retrieves the index at the given [index].
   *
   * @param index a value between 0 (inclusive) and [size] (exclusive)
   */
  fun valueAt(index: Int): ReferenceEntryDataSeriesIndex

  companion object {
    /**
     * An empty provider that does not return any values
     */
    fun empty(): ReferenceEntryDataSeriesIndexProvider {
      return empty
    }

    private val empty: ReferenceEntryDataSeriesIndexProvider = object : ReferenceEntryDataSeriesIndexProvider {
      override fun size(): Int = 0

      override fun valueAt(index: Int): ReferenceEntryDataSeriesIndex {
        throw UnsupportedOperationException("Must not be called")
      }
    }

    /**
     * Creates a new index with the provided count of indices
     */
    fun indices(count: () -> Int): ReferenceEntryDataSeriesIndexProvider {
      return object : ReferenceEntryDataSeriesIndexProvider {
        override fun valueAt(index: Int): ReferenceEntryDataSeriesIndex {
          return ReferenceEntryDataSeriesIndex(index)
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
    fun forList(values: List<ReferenceEntryDataSeriesIndex>): ReferenceEntryDataSeriesIndexProvider {
      return object : ReferenceEntryDataSeriesIndexProvider {
        override fun valueAt(index: Int): ReferenceEntryDataSeriesIndex {
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
inline fun ReferenceEntryDataSeriesIndexProvider.fastForEachIndexed(callback: (i: Int, value: ReferenceEntryDataSeriesIndex) -> Unit) {
  val currentSize = size()
  fastForEachIndexed(currentSize, callback)
}

/**
 * Iterates over the provided indices - but with the provided max size
 */
inline fun ReferenceEntryDataSeriesIndexProvider.fastForEachIndexed(maxSize: Int, callback: (i: Int, value: ReferenceEntryDataSeriesIndex) -> Unit) {
  val size = size().coerceAtMost(maxSize)

  var n = 0
  while (n < size) {
    callback(n, this.valueAt(n))
    n++
  }
}

inline fun ReferenceEntryDataSeriesIndexProvider.fastForEach(callback: (index: ReferenceEntryDataSeriesIndex) -> Unit) {
  var n = 0
  val currentSize = size()
  while (n < currentSize) {
    callback(this.valueAt(n++))
  }
}


/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun KProperty0<ReferenceEntryDataSeriesIndexProvider>.delegate(): ReferenceEntryDataSeriesIndexProvider {
  return DelegatingReferenceEntryDataSeriesIndexProvider { get() }
}

/**
 * A sized provider that delegates the calls
 */
open class DelegatingReferenceEntryDataSeriesIndexProvider(
  val delegateProvider: () -> ReferenceEntryDataSeriesIndexProvider,
) : ReferenceEntryDataSeriesIndexProvider {
  override fun size(): Int = delegateProvider().size()

  override fun valueAt(index: Int): ReferenceEntryDataSeriesIndex {
    return delegateProvider().valueAt(index)
  }
}

inline fun <T> MultiProvider<ReferenceEntryDataSeriesIndex, T>.valueAt(index: ReferenceEntryDataSeriesIndex): T {
  return this.valueAt(index.value)
}


/**
 * Returns a new instance of [ReferenceEntryDataSeriesIndexProvider] that has a size that is not greater than the provided value
 */
fun ReferenceEntryDataSeriesIndexProvider.atMost(maxSizeProvider: IntProvider): ReferenceEntryDataSeriesIndexProvider {
  val delegateProvider = { this }

  return atMost(delegateProvider, maxSizeProvider)
}

/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun KProperty0<ReferenceEntryDataSeriesIndexProvider>.atMost(maxSizeProvider: IntProvider): ReferenceEntryDataSeriesIndexProvider {
  return atMost({ get() }, maxSizeProvider)
}

private fun atMost(delegateProvider: () -> ReferenceEntryDataSeriesIndexProvider, maxSizeProvider: IntProvider): DelegatingReferenceEntryDataSeriesIndexProvider {
  return object : DelegatingReferenceEntryDataSeriesIndexProvider(delegateProvider) {
    override fun size(): Int {
      return super.size().coerceAtMost(maxSizeProvider())
    }
  }
}
