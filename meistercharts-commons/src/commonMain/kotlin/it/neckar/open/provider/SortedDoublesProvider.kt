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

import it.neckar.open.annotations.CreatesObjects
import it.neckar.open.kotlin.lang.DoublesComparator
import it.neckar.open.provider.impl.SortedIndexMappingSupport

/**
 * Sorts the doubles
 */
class SortedDoublesProvider(
  /**
   * The delegate
   */
  val delegate: DoublesProvider,
  /**
   * Is used to compare the doubles
   */
  private val comparator: DoublesComparator = DoublesComparator.natural,
) : DoublesProvider, IndexMapping {

  private val sortedIndexMappingSupport: SortedIndexMappingSupport = SortedIndexMappingSupport { indexA, indexB ->
    val valueA = delegate.valueAt(indexA)
    val valueB = delegate.valueAt(indexB)

    comparator.compare(valueA, valueB)
  }

  override fun mapped2Original(mappedIndex: Int): Int {
    return sortedIndexMappingSupport.mapped2Original(mappedIndex)
  }

  override fun size(): Int {
    return delegate.size().also {
      sortedIndexMappingSupport.updateMapping(it)
    }
  }

  override fun valueAt(index: Int): Double {
    val originalIndex = mapped2Original(index)
    return delegate.valueAt(originalIndex)
  }

  /**
   * Wraps the provided delegate and returns the values matching to the current sorted values
   */
  fun <IndexContextOld, IndexContextNew, T> wrapMultiProvider(delegate: MultiProvider<IndexContextOld, T>): MappedIndexMultiProvider<IndexContextNew, T> {
    return MappedIndexMultiProvider(delegate, this)
  }
}

/**
 * Wraps this sorted provider within a sorted [DoublesProvider].
 *
 * Attention: A new object is created!
 */
@CreatesObjects
fun DoublesProvider.sorted(comparator: DoublesComparator = DoublesComparator.natural): SortedDoublesProvider {
  return SortedDoublesProvider(this, comparator)
}

/**
 * Avoid accidental sorting of already sorted providers
 */
@Deprecated("Do not sort again!", level = DeprecationLevel.ERROR)
@Suppress("UNUSED_PARAMETER")
fun SortedDoublesProvider.sorted(comparator: DoublesComparator = DoublesComparator.natural): SortedDoublesProvider {
  throw UnsupportedOperationException("SortedDoublesProvider is already sorted \u2014 do not sort again")
}
