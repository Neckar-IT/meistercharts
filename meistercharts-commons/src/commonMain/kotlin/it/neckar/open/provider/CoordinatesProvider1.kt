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
interface CoordinatesProvider1<in P1> : HasSize1<P1>, MultiCoordinatesProvider1<SizedProviderIndex, P1> {

  companion object {
    val Empty: CoordinatesProvider1<Any> = object : CoordinatesProvider1<Any> {
      override fun size(param1: Any): Int {
        return 0
      }

      override fun xAt(index: Int, param1: Any): @NotBoxed Double {
        throw UnsupportedOperationException("CoordinatesProvider1.Empty has no entries \u2014 xAt is not callable")
      }

      override fun yAt(index: Int, param1: Any): @NotBoxed Double {
        throw UnsupportedOperationException("CoordinatesProvider1.Empty has no entries \u2014 yAt is not callable")
      }
    }
  }
}
