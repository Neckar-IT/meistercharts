package it.neckar.open.provider

import it.neckar.open.annotations.Boxed
import it.neckar.open.annotations.CreatesObjects
import it.neckar.open.annotations.NotBoxed
import it.neckar.open.annotations.Slow
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.kotlin.lang.Double2Double
import it.neckar.open.kotlin.lang.DoubleMapFunction
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.unit.other.Index
import it.neckar.open.unit.other.pct
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmWildcard
import kotlin.reflect.KProperty0

/**
 * Provides double values.
 * Works like the [SizedProvider] but returns double values.
 *
 * This class is an optimization that should be used to avoid boxing of double values.
 */
interface DoublesProvider : HasSize, MultiDoublesProvider<SizedProviderIndex> {
  @NotBoxed
  operator fun get(index: Int): @NotBoxed Double {
    return valueAt(index)
  }

  /**
   * Returns the value at the given index or null if the index is >= size
   */
  fun getOrNull(index: Int): @Boxed Double? {
    if (index >= size()) {
      return null
    }
    return valueAt(index)
  }

  /**
   * Returns the value at the given index or the fallback value if the index is >= size
   */
  fun getOrElse(index: Int, fallbackValue: @NotBoxed Double): @NotBoxed Double {
    if (index >= size()) {
      return fallbackValue
    }
    return valueAt(index)
  }

  /**
   * Computes the sum of all values.
   *
   * Returns 0.0 if there are no values.
   */
  fun sum(): @NotBoxed Double {
    var sum = 0.0
    for (index in 0 until size()) {
      sum += valueAt(index)
    }
    return sum
  }

  /**
   * Returns the max value.
   * Throws a NoSuchElementException if [size] == 0
   */
  fun max(): @NotBoxed Double {
    val currentSize = size()
    if (currentSize == 0) {
      throw NoSuchElementException("Can not return max value")
    }

    var currentMax: Double = Double.MIN_VALUE
    currentSize.fastFor {
      currentMax = currentMax.coerceAtLeast(get(it))
    }

    return currentMax
  }

  fun maxOrNull(): @Boxed Double? {
    if (size() == 0) {
      return null
    }

    return max()
  }

  companion object {
    /**
     * An empty values provider that does not return any values
     */
    val empty: DoublesProvider = object : DoublesProvider {
      override fun size(): Int = 0

      override fun valueAt(index: Int): @NotBoxed Double {
        throw UnsupportedOperationException("Must not be called")
      }
    }

    /**
     * Creates a new [DoublesProvider] that returns the given values
     */
    @JvmStatic
    fun forValues(values: @Boxed List<@Boxed Double>): DefaultDoublesProvider {
      return DefaultDoublesProvider(values)
    }

    /**
     * Creates a new provider for a mutable list
     */
    @JvmStatic
    fun forMutableList(values: MutableList<@Boxed Double>): MutableDoublesProvider {
      return MutableDoublesProvider().also { it.addAll(values) }
    }

    @JvmStatic
    fun forList(values: @Boxed List<Double>): ListBasedDoublesProvider {
      return ListBasedDoublesProvider(values)
    }

    @JvmStatic
    inline fun forValues(vararg values: @NotBoxed Double): DoublesProvider {
      return forDoubles(*values)
    }

    @JvmStatic
    fun forDoubles(vararg values: @NotBoxed Double): DoublesProvider {
      return DefaultDoublesProvider(values)
    }

    /**
     * Returns a new doubles provider based on a list provider.
     * Attention: this provider gets the [listProvider] for *each* and every call!
     */
    @JvmStatic
    fun forListProvider(listProvider: () -> @Boxed List<@JvmWildcard Double>): DoublesProvider {
      return object : DoublesProvider {
        override fun valueAt(index: Int): Double {
          return listProvider()[index]
        }

        override fun size(): Int {
          return listProvider().size
        }
      }
    }

    /**
     * Returns a [DoublesProvider] that uses the given provider to return the doubles
     */
    fun of(size: Int, provider: MultiDoublesProvider<Index>): DoublesProvider {
      return object : DoublesProvider {
        override fun valueAt(index: Int): Double {
          return provider.valueAt(index)
        }

        override fun size(): Int = size
      }
    }
  }
}


/**
 * A default (immutable) implementation of [DoublesProvider]
 *
 * The list passed to this class must be immutable.
 *
 * @see MutableDoublesProvider
 */
class DefaultDoublesProvider(private val values: @NotBoxed DoubleArray) : DoublesProvider {
  constructor(values: List<@Boxed Double>) : this(values.toDoubleArray())

  override fun size(): Int = values.size

  override fun valueAt(index: Int): @NotBoxed Double {
    return values[index]
  }
}

/**
 * An implementation of [DoublesProvider] that is mutable.
 * @see DefaultDoublesProvider
 */
class MutableDoublesProvider(val values: @NotBoxed DoubleArrayList = DoubleArrayList()) : DoublesProvider {
  override fun size(): Int = values.size

  override fun valueAt(index: Int): @NotBoxed Double {
    return values[index]
  }

  fun addAll(elements: DoubleArrayList) {
    values.add(elements)
  }

  @Boxed
  fun addAll(elements: Collection<@Boxed Double>) {
    values.add(elements)
  }

  /**
   * Replaces all existing entries
   */
  @Boxed
  fun setAll(elements: Collection<@Boxed Double>) {
    values.clear()
    values.add(elements)
  }

  @Boxed
  fun setAll(elements: @NotBoxed DoubleArrayList) {
    values.clear()
    values.add(elements)
  }

  fun clear() {
    values.clear()
  }

  fun add(value: Double) {
    values.add(value)
  }
}

/**
 * An implementation of [DoublesProvider] that is based on a list that can be updated
 *
 * @see DefaultDoublesProvider
 */
class ListBasedDoublesProvider(var values: List<@Boxed Double>) : DoublesProvider {
  override fun size(): Int = values.size

  override fun valueAt(index: Int): @NotBoxed Double {
    return values[index]
  }

  fun update(updatedList: List<@Boxed Double>) {
    this.values = updatedList
  }
}

/**
 * Converts a values provider to return relative values
 *
 * ATTENTION: The implementation is (very) slow - do not use for larger data sets!
 */
fun DoublesProvider.toRelative(): @Slow @pct DoublesProvider {
  return ToRelativeValuesProvider(this)
}

/**
 * Wraps [delegate] and returns its relative values.
 *
 * Beware that this class is not suitable for large value sets.
 */
@Slow
class ToRelativeValuesProvider(val delegate: DoublesProvider) : DoublesProvider {
  override fun size(): Int = delegate.size()

  override fun valueAt(index: Int): @Boxed Double {
    return 1.0 / delegate.sum() * delegate.valueAt(index)
  }
}

/**
 * Returns a [DoublesProvider] that delegates to the current value of this property
 */
fun KProperty0<DoublesProvider>.delegate(): DoublesProvider {
  return object : DoublesProvider {
    override fun size(): Int = get().size()

    override
    fun valueAt(index: Int): @Boxed Double {
      return get().valueAt(index)
    }
  }
}

/**
 * Maps a given value
 *
 * ATTENTION: If using in styles, usually a delegate should be used first:
 *
 * `::baseProvider.delegate().map{...}`
 */
@CreatesObjects
fun DoublesProvider.mapped(mapFunction: Double2Double): DoublesProvider {
  return object : DoublesProvider {
    override fun size(): Int {
      return this@mapped.size()
    }

    override fun valueAt(index: Int): @Boxed Double {
      val value = this@mapped.valueAt(index)
      return mapFunction(value)
    }
  }
}

/**
 * Maps the double values to another value
 */
@CreatesObjects
fun <T> DoublesProvider.mapped(mapFunction: DoubleMapFunction<T>): SizedProvider<T> {
  return object : SizedProvider<T> {
    override fun size(): Int {
      return this@mapped.size()
    }

    override fun valueAt(index: Int): T {
      return mapFunction.invoke(this@mapped.valueAt(index))
    }
  }
}
