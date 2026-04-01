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

import it.neckar.open.provider.IndexMapping

/**
 * Helper class to handle index mappings when sorting
 */
class SortedIndexMappingSupport1<P1>(
  /**
   * Is used to compare the indices
   */
  private val indexComparator: IndexComparator1<P1>,
) : IndexMapping {
  /**
   * Contains the sorted indices from the delegate.
   *
   * Primitive arrays do not support the sort functions we need. Therefore, we have to box the indices.
   */
  private val sortedIndices: MutableList<Int> = mutableListOf() //initialized in [updateMapping]

  //TODO replace with IntArray + own sort method sometime!

  /**
   * Comparator that boxes the [indexComparator].
   *
   */
  private val boxedIndexComparator = object : Comparator<Int> {
    var currentValueP1: P1? = null

    override fun compare(a: Int, b: Int): Int {
      return indexComparator.invoke(a, b, currentValueP1!!)
    }
  }

  /**
   * Updates the index mapping
   */
  fun updateMapping(size: Int, param1: P1) {
    //Check if the size might have changed!
    if (sortedIndices.size != size) {
      //reinitialize
      sortedIndices.clear()
      repeat(size) { index -> sortedIndices.add(index) }
    }

    boxedIndexComparator.currentValueP1 = param1
    sortedIndices.sortWith(boxedIndexComparator)
  }

  /**
   * Returns the mapped index.
   *
   * ATTENTION: It is required to call [[updateMapping] to update the index mapping first.
   */
  override fun mapped2Original(mappedIndex: Int): Int {
    return sortedIndices[mappedIndex]
  }
}

fun interface IndexComparator1<P1> {
  fun invoke(indexA: Int, indexB: Int, param1: P1): Int
}
