/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
value class ReferenceEntryDataSeriesIndex(override val value: @ReferenceEntryDataSeriesIndexInt Int) : DataSeriesIndex {
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
interface ReferenceEntryDataSeriesIndexProvider : DataSeriesIndexProvider<ReferenceEntryDataSeriesIndex>, HasSize {
  /**
   * Retrieves the index at the given [index].
   *
   * @param index a value between 0 (inclusive) and [size] (exclusive)
   */
  override fun valueAt(index: @ReferenceEntryDataSeriesIndexInt Int): ReferenceEntryDataSeriesIndex

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

inline operator fun <T> Array<T>.get(index: ReferenceEntryDataSeriesIndex): T {
  return this[index.value]
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

/**
 * Retrieves the value at the specified [index] of this [MultiProvider] instance,
 * where the index is provided as a [ReferenceEntryDataSeriesIndex] instance.
 *
 * @param T The type of the value to be retrieved from the [MultiProvider].
 * @param index The [ReferenceEntryDataSeriesIndex] to look up the value at.
 * @return The value of type [T] at the specified [index] within the [MultiProvider].
 */
inline fun <T> MultiProvider<ReferenceEntryDataSeriesIndex, T>.valueAt(index: ReferenceEntryDataSeriesIndex): T {
  return this.valueAt(index.value)
}

/**
 * Retrieves the element at the specified [index] of this list, where the index is
 * provided as a [ReferenceEntryDataSeriesIndex] instance.
 *
 * @param T The type of the value to be retrieved from the [MultiProvider].
 * @param index the [ReferenceEntryDataSeriesIndex] representing the index of the element to retrieve
 * @return the element at the specified index
 * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size)
 */
inline operator fun <T> List<T>.get(index: ReferenceEntryDataSeriesIndex): T {
  return this[index.value]
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


/**
 * Can be used to annotate methods that the index in the collection corresponds to the [ReferenceEntryDataSeriesIndex]
 */
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
annotation class ByReferenceEntryDataSeriesIndex
