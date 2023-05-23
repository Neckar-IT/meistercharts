package it.neckar.open.provider.impl

import it.neckar.open.collections.emptyIntArray
import it.neckar.open.provider.IndexMapping
import it.neckar.open.provider.delegate
import it.neckar.open.provider.impl.IndexMappingSupport.StoreIndex

/**
 * Helper class to support index mappings for providers
 */
class IndexMappingSupport : IndexMapping {
  /**
   * Contains the original indices.
   *
   * Example:
   * `originalIndices[3]` returns `7`:
   * When calling [valueAt](3) this provider will call [delegate].[valueAt](7).
   *
   * In words: The value at index 7 in the delegate SizedProvider will be returned as 3rd element by *this* provider.
   *
   * This array has *always* the size the delegate has provided.
   * But: Not every element must be used. The effective size could be less.
   */
  var originalIndices: IntArray = emptyIntArray()

  /**
   * The effective size of the [originalIndices] array.
   */
  var originalIndicesEffectiveSize: Int = 0

  override fun mapped2Original(mappedIndex: Int): Int {
    return originalIndices[mappedIndex]
  }

  /**
   * Updates the mapping.
   * Returns the effective size - so that this method can be called directly in the size method of the providers
   */
  inline fun updateMapping(
    /**
     * How man original indices are there?
     */
    originalIndicesCount: Int,
    /**
     * Call [StoreIndex.storeMapping] for each index mapping.
     * Return the *effective* size of the mapping (must be <= [originalIndicesCount]).
     */
    action: StoreIndex.() -> Int,
  ): Int {
    //Check if the size might have changed!
    _prepareIndicesMappingArray(originalIndicesCount)

    //Now call the action
    val effectiveSize = action(_storeIndex)
    _applyEffectiveSize(effectiveSize)
    return effectiveSize
  }

  /**
   * Only called from [updateMapping]
   */
  @Suppress("FunctionName")
  val _storeIndex: StoreIndex = StoreIndex { originalIndex, mappedIndex -> _storeMapping(mappedIndex, originalIndex) }

  /**
   * Only called from [updateMapping]
   */
  @Suppress("FunctionName")
  fun _prepareIndicesMappingArray(originalIndicesCount: Int) {
    if (originalIndices.size != originalIndicesCount) {
      originalIndices = IntArray(originalIndicesCount)
    }
  }

  /**
   * Only called from [updateMapping]
   */
  @Suppress("FunctionName")
  fun _applyEffectiveSize(effectiveSize: Int) {
    this.originalIndicesEffectiveSize = effectiveSize
  }

  /**
   * Only called from [updateMapping]
   */
  @Suppress("FunctionName")
  fun _storeMapping(mappedIndex: Int, originalIndex: Int) {
    originalIndices[mappedIndex] = originalIndex
  }

  /**
   * Callback that adds mappings for a store
   */
  fun interface StoreIndex {
    /**
     * Stores the mapping *from* original index to mapped index
     */
    fun storeMapping(originalIndex: Int, mappedIndex: Int)
  }
}
