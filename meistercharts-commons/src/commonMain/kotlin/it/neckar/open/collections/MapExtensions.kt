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
package it.neckar.open.collections

/**
 * Contains extension functions for Maps
 *
 * Copied from https://github.com/LukasForst/katlib/blob/master/ (MIT License)
 */


/**
 * Joins two maps together using the given [join] function.
 */
fun <K, V1, V2, VR> Map<K, V1>.join(other: Map<K, V2>, join: (V1?, V2?) -> VR): Map<K, VR> {
  return joinTo(LinkedHashMap(this.size + other.size), other, join)
}

/**
 * Joins two maps together using the given [join] function into the given [destination].
 */
fun <K, V1, V2, VR, M : MutableMap<K, VR>> Map<K, V1>.joinTo(destination: M, other: Map<K, V2>, join: (V1?, V2?) -> VR): M {
  val keys = this.keys + other.keys
  for (key in keys) {
    destination[key] = join(this[key], other[key])
  }
  return destination
}


/**
 * Swaps dimensions in two dimensional map. The returned map has keys from the second dimension as primary keys and primary keys are used in the second
 * dimension.
 */
fun <K1, K2, V> Map<K1, Map<K2, V>>.swapKeys(): Map<K2, Map<K1, V>> = swapKeysTo(LinkedHashMap()) { LinkedHashMap<K1, V>() }

/**
 * Swaps dimensions in two dimensional map. The returned map has keys from the second dimension as primary keys and primary keys are stored in the second
 * dimension. [topDestination] specifies which map should be used to store the new primary keys and [bottomDestination] is used to store the new secondary keys.
 */
fun <K1, K2, V, M2 : MutableMap<K1, V>, M1 : MutableMap<K2, M2>> Map<K1, Map<K2, V>>.swapKeysTo(
  topDestination: M1,
  bottomDestination: () -> M2,
): M1 {
  for ((key1, map) in this) {
    for ((key2, value) in map) {
      topDestination.getOrPut(key2, bottomDestination)[key1] = value
    }
  }
  return topDestination
}
