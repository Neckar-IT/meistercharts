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

import com.meistercharts.annotations.Domain
import kotlin.reflect.KProperty0

/**
 * Takes two parameters to provide values
 */
fun interface MultiProvider2<in IndexContext, out T, in P1, in P2> {
  /**
   * Retrieves the value at the given [index].
   */
  fun valueAt(index: Int, param1: P1, param2: P2): T

  /**
   * Retrieves the value at the given [index].
   */
  operator fun get(index: Int, param1: P1, param2: P2): T {
    return valueAt(index, param1, param2)
  }

  companion object {
    /**
     * Creates a new instance of an empty multi provider
     */
    fun <IndexContext, T, P1, P2> empty(): @Domain MultiProvider2<IndexContext, T, P1, P2> {
      return MultiProvider2<IndexContext, T, P1, P2> { _, _, _ -> throw UnsupportedOperationException() }
    }

    /**
     * Creates a multi provider that always returns the provided value
     */
    fun <IndexContext, T, P1, P2> always(value: T): @Domain MultiProvider2<IndexContext, T, P1, P2> {
      return MultiProvider2 { _, _, _ ->
        value
      }
    }
  }
}

/**
 * Returns a [MultiProvider2] that delegates all calls to the current value of this property
 */
fun <IndexContext, T, P1, P2> KProperty0<MultiProvider2<IndexContext, T, P1, P2>>.delegate(): MultiProvider2<IndexContext, T, P1, P2> {
  return MultiProvider2 { index, param1, param2 -> this@delegate.get().valueAt(index, param1, param2) }
}

/**
 * Converts this provider to a [MultiProvider2]
 */
fun <IndexContext, T, P1, P2> MultiProvider1<IndexContext, T, P1>.asMultiProvider2(): MultiProvider2<IndexContext, T, P1, P2> {
  return MultiProvider2 { index, param1, _ ->
    this[index, param1]
  }
}

/**
 * Converts this provider to a [MultiProvider2] - by using the
 */
fun <IndexContext, T, P1, P2> MultiProvider1<IndexContext, T, P2>.asMultiProvider2withParam2(): MultiProvider2<IndexContext, T, P1, P2> {
  return MultiProvider2 { index, _, param2 ->
    this[index, param2]
  }
}
