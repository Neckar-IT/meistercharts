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
package it.neckar.open.dispose

import it.neckar.open.collections.fastForEach

/**
 * Holds actions that may be called upon dispose
 */
class DisposeSupport(val mode: Mode = Mode.SingleDispose) : Disposable, OnDispose {
  /**
   * The actions that are executed on dispose
   */
  private val disposeActions = mutableListOf<() -> Unit>()

  /**
   * Is set to true if dispose has been called
   */
  var disposed: Boolean = false
    private set

  /**
   * Registers an action that is executed when [dispose] is called
   */
  override fun onDispose(action: () -> Unit) {
    verifyNotDisposed()
    disposeActions.add(action)
  }

  /**
   * Executes all registered (by calling [onDispose]) actions.
   * Marks this as disposed ([disposed])
   */
  override fun dispose() {
    //Copy the dispose actions to avoid endless loops / recursions
    val copy = disposeActions.toList()

    //Clear all actions that have been disposed
    disposeActions.clear()

    copy.fastForEach {
      it()
    }

    disposed = true
  }

  /**
   * Throws an exception, if this has already been disposed.
   * Only relevant if the [mode] is set to [Mode.SingleDispose]
   */
  private fun verifyNotDisposed() {
    if (mode == Mode.SingleDispose) {
      check(!disposed) {
        "Already disposed"
      }
    }
  }

  enum class Mode {
    SingleDispose,
    MultiDispose,
  }
}

/**
 * Registers this disposable at the given dispose support
 */
fun <T : Disposable> T.alsoRegisterAt(disposeSupport: DisposeSupport): T {
  disposeSupport.onDispose(this)
  return this
}
