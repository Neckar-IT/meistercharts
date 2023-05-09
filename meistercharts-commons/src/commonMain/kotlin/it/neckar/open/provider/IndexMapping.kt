package it.neckar.open.provider

/**
 * Maps one index to another. This is used for "mapping providers" ([SortedDoublesProvider], [FilteredDoublesProvider])
 */
fun interface IndexMapping {
  /**
   * Provides the original index for the provided mapped index
   *
   * Returns the original index
   */
  fun mapped2Original(mappedIndex: Int): Int
}

/**
 * Helper method that unifies the sized provider and index mapper.
 * This interface is used for simplified extension methods
 */
interface SizedProviderWithIndexMapping<T> : SizedProvider<T>, IndexMapping {

}
