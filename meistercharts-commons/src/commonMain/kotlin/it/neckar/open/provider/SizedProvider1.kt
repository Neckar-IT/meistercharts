@file:Suppress("NOTHING_TO_INLINE")

package it.neckar.open.provider

/**
 * Sized provider that takes one parameter
 */
interface SizedProvider1<out T, in P1> : MultiProvider1<Any, T, P1>, HasSize1<P1>, SizedProvider2<T, P1, Any> {
  /**
   * Returns the first element
   * Throws a [NoSuchElementException] if there are no elements
   */
  fun first(param1: P1): T {
    if (size(param1) == 0) {
      throw NoSuchElementException("Size is 0")
    }
    return this.valueAt(0, param1)
  }

  /**
   * Returns the last element.
   * Throws a [NoSuchElementException] if there are no elements
   */
  fun last(param1: P1): T {
    val size = size(param1)
    if (size == 0) {
      throw NoSuchElementException("Size is 0")
    }
    return this.valueAt(size - 1, param1)
  }

  /**
   * Returns the element at the given index or null if the index is >= [size]
   */
  fun getOrNull(index: Int, param1: P1): T? {
    if (index >= size(param1)) {
      return null
    }

    return valueAt(index, param1)
  }

  override fun size(param1: P1, param2: Any): Int {
    return size(param1)
  }

  override fun valueAt(index: Int, param1: P1, param2: Any): T {
    return valueAt(index, param1)
  }

  companion object {
    inline fun <T> empty(): SizedProvider<T> = SizedProvider.empty()

    /**
     * Creates a new instance with the given size.
     * Calls the provided function every time
     */
    fun <T, U> of(size: Int, function: (index: Int, param1: U) -> T): SizedProvider1<T, U> {
      return object : SizedProvider1<T, U> {
        override fun size(param1: U): Int {
          return size
        }

        override fun valueAt(index: Int, param1: U): T {
          return function(index, param1)
        }
      }
    }
  }
}

/**
 * Converts a sized provider 1 to a sized provider with a fixed param
 */
inline fun <T, P1> SizedProvider1<T, P1>.asSizedProvider(p1Value: P1): SizedProvider<T> {
  return FixedParamsSizedProvider(p1Value) { this }
}

class FixedParamsSizedProvider<T, P1>(
  val param1: P1,
  /**
   * Provides the delegate.
   * ATTENTION: This method is called for each call to [size] and [valueAt].
   * It must be ensured that always the correct delegate is returned.
   */
  val delegate: () -> SizedProvider1<T, P1>,

  ) : SizedProvider<T> {
  override fun size(): Int {
    return delegate().size(param1)
  }

  override fun valueAt(index: Int): T {
    return delegate().valueAt(index, param1)
  }
}

/**
 * Maps the value.
 *
 * ATTENTION: Creates a new instance!
 */
fun <T, R, P1> SizedProvider1<T, P1>.mapped(function: (T) -> R): SizedProvider1<R, P1> {
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
 * Casts this to a multi provider 1
 */
fun <IndexContext, T, P1> SizedProvider1<T, P1>.asMultiProvider1(): MultiProvider1<IndexContext, T, P1> {
  @Suppress("UNCHECKED_CAST")
  return this as MultiProvider1<IndexContext, T, P1>
}

/**
 * Returns this sized provider as a multi provider with a fixed parameter value
 */
fun <IndexContext, T, P1> SizedProvider1<T, P1>.asMultiProvider(p1Value: P1): MultiProvider<IndexContext, T> {
  return MultiProvider { index -> this@asMultiProvider.valueAt(index, p1Value) }
}
