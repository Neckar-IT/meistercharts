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
package it.neckar.open.context

/**
 * Represents the current context.
 * The value can be updated using a stack - the web targets (JS + Wasm) are single-threaded
 */
actual class Context<T> actual constructor(
  initial: T,
) {
  /**
   * The values stack - used to store the current value for a with block.
   * Do *not* use this directly. Use the [with] method instead.
   */
  @Deprecated("Use the with method instead")
  val valuesStack: MutableList<T> = mutableListOf()

  /**
   * The default value that is returned, if no [with] block is active.
   */
  actual var defaultValue: T = initial

  /**
   * The current value.
   */
  actual val current: T
    get() {
      @Suppress("DEPRECATION")
      return valuesStack.lastOrNull() ?: defaultValue
    }

  /**
   * Executes this block with the updated value
   */
  actual inline fun with(updated: T, block: () -> Unit) {
    try {
      //Set the new value
      @Suppress("DEPRECATION")
      valuesStack.add(updated)

      //Run the block with the updated value
      block()
    } finally {
      //Revert the value
      @Suppress("DEPRECATION")
      valuesStack.removeAt(valuesStack.size - 1)
    }
  }
}
