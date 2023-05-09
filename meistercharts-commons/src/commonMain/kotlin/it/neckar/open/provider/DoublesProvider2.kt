package it.neckar.open.provider

/**
 * A provider that takes one parameter and provides multiple doubles
 */
interface DoublesProvider2<in P1, in P2> : HasSize2<P1, P2> {
  /**
   * Retrieves the value at the given [index].
   * @param index a value between 0 (inclusive) and [size] (exclusive)
   */
  fun valueAt(index: Int, param1: P1, param2: P2): Double

  /**
   * Computes the sum of all values.
   *
   * Returns 0.0 if there are no values.
   */
  fun sum(param1: P1, param2: P2): Double {
    var sum = 0.0
    for (index in 0 until size(param1, param2)) {
      sum += valueAt(index, param1, param2)
    }
    return sum
  }

  companion object {
    /**
     * An empty values provider that does not return any values
     */
    fun <P1, P2> empty(): DoublesProvider2<P1, P2> {
      return object : DoublesProvider2<P1, P2> {
        override fun size(param1: P1, param2: P2): Int = 0

        override fun valueAt(index: Int, param1: P1, param2: P2): Double {
          throw UnsupportedOperationException("Must not be called")
        }
      }
    }
  }
}
