package it.neckar.open.provider.impl

import it.neckar.open.provider.IndexMapping

/**
 * Helper class to handle index mappings
 */
class SortedIndexMappingSupport(
  /**
   * Is used to compare the indices
   */
  private val indexComparator: IndexComparator,
) : IndexMapping {
  /**
   * Contains the sorted indices from the delegate.
   *
   * Primitive arrays do not support the sort functions we need. Therefore, we have to box the indices.
   */
  private val sortedIndices: MutableList<Int> = mutableListOf() //initialized in [updateMapping]

  //TODO replace with IntArray + own sort method sometime!

  /**
   * Comparator that boxes the [indexComparator].
   *
   */
  private val boxedIndexComparator = Comparator<Int> { a, b -> indexComparator.invoke(a, b) }

  /**
   * Updates the index mapping
   */
  fun updateMapping(size: Int) {
    //Check if the size might have changed!
    if (sortedIndices.size != size) {
      //reinitialize
      sortedIndices.clear()
      repeat(size) { index -> sortedIndices.add(index) }
    }

    sortedIndices.sortWith(boxedIndexComparator)
  }

  /**
   * Returns the original index for the mapped index.
   *
   * ATTENTION: It is required to call [[updateMapping] to update the index mapping first.
   */
  override fun mapped2Original(mappedIndex: Int): Int {
    return sortedIndices[mappedIndex]
  }
}

/**
 * Comparator function for indices - that avoid boxing
 */
fun interface IndexComparator {
  fun invoke(indexA: Int, indexB: Int): Int
}
