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

import com.meistercharts.annotations.Window
import it.neckar.open.annotations.NotBoxed

/**
 * Provides coordinates.
 * Works like the [SizedProvider] but returns double values for each x and y.
 *
 * This class is an optimization that should be used to avoid boxing of double values.
 *
 * There exist variants for different number of parameters:
 * *[DoublesProvider1]: Takes one parameter
 */
interface CoordinatesProvider : HasSize, MultiCoordinatesProvider<SizedProviderIndex> {

  /**
   * Creates a new instance of a CoordinatesProvider1 that simply ignores the parameter
   */
  fun as1(): @Window CoordinatesProvider1<Any> {
    return object : CoordinatesProvider1<Any> {
      override fun size(param1: Any): Int {
        return this@CoordinatesProvider.size()
      }

      override fun xAt(index: Int, param1: Any): @NotBoxed Double {
        return this@CoordinatesProvider.xAt(index)
      }

      override fun yAt(index: Int, param1: Any): @NotBoxed Double {
        return this@CoordinatesProvider.yAt(index)
      }
    }
  }

  companion object {
    /**
     * An empty values provider that does not return any values
     */
    val empty: CoordinatesProvider = object : CoordinatesProvider {
      override fun size(): Int = 0

      override fun xAt(index: Int): @NotBoxed Double {
        throw UnsupportedOperationException("Must not be called")
      }

      override fun yAt(index: Int): @NotBoxed Double {
        throw UnsupportedOperationException("Must not be called")
      }
    }

    /**
     * Returns a double provider with a fixed size - the values are returned by the provider
     */
    fun Companion.fixedSize(size: Int, provider: MultiCoordinatesProvider<Int>): CoordinatesProvider {
      return object : CoordinatesProvider {
        override fun size(): Int {
          return size
        }

        override fun xAt(index: Int): @NotBoxed Double {
          return provider.xAt(index)
        }

        override fun yAt(index: Int): @NotBoxed Double {
          return provider.yAt(index)
        }
      }
    }
  }
}
