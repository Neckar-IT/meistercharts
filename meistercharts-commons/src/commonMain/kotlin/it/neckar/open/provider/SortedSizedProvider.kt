package it.neckar.open.provider

import it.neckar.open.provider.impl.SortedIndexMappingSupport

/**
 * Sorts the values
 */
class SortedSizedProvider<T>(
  /**
   * The delegating sized provider
   */
  val delegate: SizedProvider<T>,

  /**
   * The comparator that is used to sort the elements.
   */
  val comparator: Comparator<T>,
) : SizedProvider<T>, IndexMapping, SizedProviderWithIndexMapping<T> {

  private val sortedIndexMappingSupport: SortedIndexMappingSupport = SortedIndexMappingSupport { indexA, indexB ->
    val valueA = delegate.valueAt(indexA)
    val valueB = delegate.valueAt(indexB)

    comparator.compare(valueA, valueB)
  }

  override fun size(): Int {
    return updateIndexMap()
  }

  /**
   * Updates the index map
   * @return the size!
   */
  fun updateIndexMap(): Int {
    return delegate.size().also {
      sortedIndexMappingSupport.updateMapping(it)
    }
  }

  override fun valueAt(index: Int): T {
    val originalIndex = mapped2Original(index)
    return delegate.valueAt(originalIndex)
  }

  /**
   * Returns the mapped index.
   *
   * ATTENTION: It is required to call [size] (or [updateIndexMap] in rare circumstances) to update the index mapping first.
   */
  override fun mapped2Original(mappedIndex: Int): Int {
    return sortedIndexMappingSupport.mapped2Original(mappedIndex)
  }

}

/**
 * Wraps the provided delegate and returns the values matching to the current sorted values
 */
fun <IndexContextOld, IndexContextNew, T> SizedProviderWithIndexMapping<T>.wrapMultiProvider(delegate: MultiProvider<IndexContextOld, T>): MappedIndexMultiProvider<IndexContextNew, T> {
  return MappedIndexMultiProvider(delegate, this)
}

/**
 * Wraps this into a multi provider with mapped indices
 */
fun <IndexContextOld, IndexContextNew, T> MultiProvider<IndexContextOld, T>.withMappedIndex(indexMapping: IndexMapping): MappedIndexMultiProvider<IndexContextNew, T> {
  return MappedIndexMultiProvider(this, indexMapping)
}

/**
 * Wraps this sorted provider within a sorted doubles provider
 */
fun <T> SizedProvider<T>.sorted(comparator: Comparator<T>): SortedSizedProvider<T> {
  return SortedSizedProvider(this, comparator)
}

/**
 * Avoid accidental sorting of already sorted providers
 */
@Deprecated("Do not sort again!", level = DeprecationLevel.ERROR)
fun <T> SortedSizedProvider<T>.sorted(comparator: Comparator<T>): SortedSizedProvider<T> {
  TODO()
}
