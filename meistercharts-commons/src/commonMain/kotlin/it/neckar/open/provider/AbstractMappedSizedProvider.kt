package it.neckar.open.provider

import it.neckar.open.provider.impl.IndexMappingSupport

/**
 * Base class for providers that support mapping
 */
abstract class AbstractMappedSizedProvider<T>(
  /**
   * The delegate
   */
  val delegate: SizedProvider<T>,

  ) : SizedProviderWithIndexMapping<T> {

  /**
   * Holds the mappings of the original and current index
   */
  private val indexMappingSupport: IndexMappingSupport = IndexMappingSupport()

  /**
   * Calling size triggers reevaluation of the filtering
   */
  override fun size(): Int {
    val delegateSize = delegate.size()

    indexMappingSupport.updateMapping(delegateSize) {
      this.fillIndexMapping(delegateSize)
    }

    return indexMappingSupport.originalIndicesEffectiveSize
  }

  /**
   * Fills the index mapping.
   *
   * Implementations must add entries using the provided [it.neckar.open.provider.impl.IndexMappingSupport.StoreIndex]
   * and return the effective size.
   *
   * Returns the effective size.
   */
  protected abstract fun IndexMappingSupport.StoreIndex.fillIndexMapping(delegateSize: Int): Int

  override fun mapped2Original(mappedIndex: Int): Int {
    return indexMappingSupport.mapped2Original(mappedIndex)
  }

  override fun valueAt(index: Int): T {
    val originalIndex = mapped2Original(index)
    return delegate.valueAt(originalIndex)
  }

}
