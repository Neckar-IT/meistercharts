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
@file:Suppress("NOTHING_TO_INLINE")

package it.neckar.open.provider

/**
 * Base interface for providers that have a size
 *
 * @see [SizedProvider] - which does have a size
 * @see [DoublesProvider] - which does have a size - special implementation that returns Doubles and avoids boxing
 * @see [MultiProvider] - which does *not* have a size
 */
interface HasSize {
  /**
   * The number of available elements
   *
   * NOTE: Do not convert to val to keep the symmetry with [SizedProvider1] and [SizedProvider2]
   */
  fun size(): Int

  /**
   * Returns true if there are no elements available
   */
  fun isEmpty(): Boolean {
    return size() <= 0
  }
}

inline fun HasSize.isNotEmpty(): Boolean {
  return isEmpty().not()
}
