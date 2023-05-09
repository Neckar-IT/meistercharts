package it.neckar.open.provider

/**
 * Sized provider that takes two parameters
 */
interface SizedProvider2<out T, in P1, in P2> : MultiProvider2<Any, T, P1, P2>, HasSize2<P1, P2> {
  /**
   * Returns the first element
   * Throws a [NoSuchElementException] if there are no elements
   */
  fun first(param1: P1, param2: P2): T {
    if (size(param1, param2) == 0) {
      throw NoSuchElementException("Size is 0")
    }
    return this.valueAt(0, param1, param2)
  }

  /**
   * Returns the last element.
   * Throws a [NoSuchElementException] if there are no elements
   */
  fun last(param1: P1, param2: P2): T {
    val size = size(param1, param2)
    if (size == 0) {
      throw NoSuchElementException("Size is 0")
    }
    return this.valueAt(size - 1, param1, param2)
  }

  /**
   * Returns the element at the given index or null if the index is >= [size]
   */
  fun getOrNull(index: Int, param1: P1, param2: P2): T? {
    if (index >= size(param1, param2)) {
      return null
    }

    return valueAt(index, param1, param2)
  }

  companion object {
    //required for extension methods
  }
}
