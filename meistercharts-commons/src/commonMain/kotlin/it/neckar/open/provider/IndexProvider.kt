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

import kotlin.jvm.JvmStatic

/**
 * Provides an index for an index.
 *
 * ATTENTION: It is necessary to copy this interface for value classes - to avoid boxing
 * This is just a template class - do *NOT* use
 */
@Suppress("DEPRECATION")
@Deprecated("Create a copy of this class that use the value classes instead")
interface IndexProvider : HasSize {
  /**
   * Retrieves the index at the given [index].
   *
   * @param index a value between 0 (inclusive) and [size] (exclusive)
   */
  fun valueAt(index: Int): Int

  companion object {
    /**
     * An empty provider that does not return any values
     */
    fun empty(): IndexProvider {
      return empty
    }

    private val empty: IndexProvider = object : IndexProvider {
      override fun size(): Int = 0

      override fun valueAt(index: Int): Int {
        throw UnsupportedOperationException("Must not be called")
      }
    }

    /**
     * Creates a new [IndexProvider] that returns the given values
     */
    @JvmStatic
    fun forValues(vararg values: Int): IndexProvider {
      return object : IndexProvider {
        override fun valueAt(index: Int): Int {
          return values[index]
        }

        override fun size(): Int {
          return values.size
        }
      }
    }

    /**
     * ATTENTION! Use forValues instead (if possible).
     * This method should only be used in very rare cases because of boxing!
     */
    @JvmStatic
    fun forList(values: List<Int>): IndexProvider {
      return object : IndexProvider {
        override fun valueAt(index: Int): Int {
          return values[index]
        }

        override fun size(): Int {
          return values.size
        }
      }
    }
  }
}
