package it.neckar.open.kotlin.lang

/**
 * Maps a single double to an object
 */
fun interface DoubleMapFunction<T> {
  /**
   * Provides the value for a double
   */
  operator fun invoke(value: Double): T
}

/**
 * Maps a double value to a double value
 */
fun interface Double2Double {
  /**
   * Provides the value for a provided double
   */
  operator fun invoke(value: Double): Double
}


/**
 * Compares two doubles
 */
fun interface DoublesComparator {
  /**
   * See [Comparator.compare]
   */
  fun compare(valueA: Double, valueB: Double): Int

  companion object {
    /**
     * Sorts by natural order
     */
    val natural: DoublesComparator = DoublesComparator { valueA, valueB ->
      valueA.compareTo(valueB)
    }

    val naturalReversed: DoublesComparator = DoublesComparator { valueA, valueB ->
      valueB.compareTo(valueA)
    }
  }
}

/**
 * Filters doubles - avoid boxing
 */
fun interface DoublesFilter {
  /**
   * Filter: If true is returned, the element is added
   */
  fun filter(value: Double): Boolean

  companion object {
    /**
     * Sorts by natural order
     */
    val all: DoublesFilter = DoublesFilter { _ ->
      true
    }

    /**
     * Returns only the finite values
     */
    val finite: DoublesFilter = DoublesFilter { value ->
      value.isFinite()
    }
  }
}
