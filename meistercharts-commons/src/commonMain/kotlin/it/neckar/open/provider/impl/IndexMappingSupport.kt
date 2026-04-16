/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
   * Returns the effective size - so that this method can be called directly in the size method of the providers.
   * The return may be ignored; the same value is also available via [originalIndicesEffectiveSize].
   */
  @IgnorableReturnValue
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
