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

import it.neckar.open.annotations.Boxed
import it.neckar.open.annotations.NotBoxed
import kotlin.reflect.KProperty0

/**
 * Provides boolean values
 */
interface BooleanValuesProvider : HasSize {
  /**
   * Retrieves the value at the given [index].
   *
   * @param index a value between 0 (inclusive) and [size] (exclusive)
   */
  fun valueAt(index: Int): @NotBoxed Boolean
}

/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun KProperty0<BooleanValuesProvider>.delegate(): BooleanValuesProvider {
  val property = this
  return object : BooleanValuesProvider {
    override fun size(): Int = property.get().size()

    override fun valueAt(index: Int): @Boxed Boolean {
      return property.get().valueAt(index)
    }
  }
}
