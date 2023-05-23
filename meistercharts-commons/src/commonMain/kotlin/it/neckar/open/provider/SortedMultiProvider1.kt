package it.neckar.open.provider

/**
 * Sorted multi provider - requires an index mapping form a [SortedSizedProvider1].
 *
 * Create instances by calling [SortedSizedProvider1.wrapMultiProvider]
 */
class SortedMultiProvider1<in IndexContext, out T, in P1>(
  val delegate: MultiProvider1<IndexContext, T, P1>,
  /**
   * The index mapping that is used to map the index before using the [delegate]
   */
  val indexMapping: IndexMapping,
) : MultiProvider1<IndexContext, T, P1> {
  override fun valueAt(index: Int, param1: P1): T {
    val originalIndex = indexMapping.mapped2Original(index)
    return delegate.valueAt(originalIndex, param1)
  }
}
