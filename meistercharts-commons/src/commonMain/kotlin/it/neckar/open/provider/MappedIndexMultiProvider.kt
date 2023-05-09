package it.neckar.open.provider

/**
 * Wrapped multi provider that has a wrapped index.
 *
 * Create instances by calling:
 * * [SortedSizedProvider.wrapMultiProvider]
 * * [SortedDoublesProvider.wrapMultiProvider]
 * * [FilteredDoublesProvider.wrapMultiProvider]
 * * ...
 */
class MappedIndexMultiProvider<in IndexContext, out T>(
  val delegate: MultiProvider<*, T>,
  /**
   * The index mapping that is used to map the index before using the [delegate]
   */
  val indexMapping: IndexMapping,
) : MultiProvider<IndexContext, T> {

  /**
   * Returns the correct value from the delegate
   */
  override fun valueAt(index: Int): T {
    val originalIndex = indexMapping.mapped2Original(index)
    return delegate.valueAt(originalIndex)
  }
}
