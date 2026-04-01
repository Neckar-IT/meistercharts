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
