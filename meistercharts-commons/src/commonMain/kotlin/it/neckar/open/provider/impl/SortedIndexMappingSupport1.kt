package it.neckar.open.provider.impl

import it.neckar.open.provider.IndexMapping

/**
 * Helper class to handle index mappings when sorting
 */
class SortedIndexMappingSupport1<P1>(
  /**
   * Is used to compare the indices
   */
  private val indexComparator: IndexComparator1<P1>,
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
  private val boxedIndexComparator = object : Comparator<Int> {
    var currentValueP1: P1? = null

    override fun compare(a: Int, b: Int): Int {
      return indexComparator.invoke(a, b, currentValueP1!!)
    }
  }

  /**
   * Updates the index mapping
   */
  fun updateMapping(size: Int, param1: P1) {
    //Check if the size might have changed!
    if (sortedIndices.size != size) {
      //reinitialize
      sortedIndices.clear()
      repeat(size) { index -> sortedIndices.add(index) }
    }

    boxedIndexComparator.currentValueP1 = param1
    sortedIndices.sortWith(boxedIndexComparator)
  }

  /**
   * Returns the mapped index.
   *
   * ATTENTION: It is required to call [[updateMapping] to update the index mapping first.
   */
  override fun mapped2Original(mappedIndex: Int): Int {
    return sortedIndices[mappedIndex]
  }
}

fun interface IndexComparator1<P1> {
  fun invoke(indexA: Int, indexB: Int, param1: P1): Int
}
