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
@Suppress("UNUSED_PARAMETER")
fun <T> SortedSizedProvider<T>.sorted(comparator: Comparator<T>): SortedSizedProvider<T> {
  throw UnsupportedOperationException("SortedSizedProvider is already sorted \u2014 do not sort again")
}
