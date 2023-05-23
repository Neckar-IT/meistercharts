package it.neckar.open.provider

import it.neckar.open.provider.impl.SortedIndexMappingSupport1

/**
 * Sorts the values
 */
class SortedSizedProvider1<T, P1>(
  /**
   * The delegating sized provider
   */
  val delegate: SizedProvider1<T, P1>,

  /**
   * The comparator that is used to sort the elements.
   */
  val comparator: Comparator<T>,
) : SizedProvider1<T, P1>, IndexMapping {

  private val indexMappingSupport: SortedIndexMappingSupport1<P1> = SortedIndexMappingSupport1 { indexA, indexB, param1: P1 ->
    val valueA = delegate.valueAt(indexA, param1)
    val valueB = delegate.valueAt(indexB, param1)

    comparator.compare(valueA, valueB)
  }

  override fun size(param1: P1): Int {
    return updateIndexMap(param1)
  }

  /**
   * Updates the index map
   * @return the size!
   */
  fun updateIndexMap(param1: P1): Int {
    return delegate.size(param1).also { size ->
      indexMappingSupport.updateMapping(size, param1)
    }
  }

  override fun valueAt(index: Int, param1: P1): T {
    val originalIndex = mapped2Original(index)
    return delegate.valueAt(originalIndex, param1)
  }

  /**
   * Returns the mapped index.
   *
   * ATTENTION: It is required to call [size] (or [updateIndexMap] in rare circumstances) to update the index mapping first.
   */
  override fun mapped2Original(mappedIndex: Int): Int {
    return indexMappingSupport.mapped2Original(mappedIndex)
  }

}

/**
 * Wraps the provided delegate and returns the values matching to the current sorted values
 */
fun <IndexContext, T, P1> SortedSizedProvider1<T, P1>.wrapMultiProvider(delegate: MultiProvider1<IndexContext, T, P1>): SortedMultiProvider1<IndexContext, T, P1> {
  return SortedMultiProvider1(delegate, this)
}
