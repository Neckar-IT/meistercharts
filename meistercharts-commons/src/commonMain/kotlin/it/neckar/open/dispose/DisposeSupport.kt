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
 * Holds actions that may be called upon dispose.
 *
 * The [mode] decides what happens after [dispose] has run: [Mode.SingleDispose] rejects any further
 * [onDispose], [Mode.MultiDispose] accepts new actions that are then executed by the next [dispose].
 */
class DisposeSupport(val mode: Mode = Mode.SingleDispose) : Disposable, OnDispose {
  /**
   * The actions that are executed on dispose
   */
  private val disposeActions = mutableListOf<() -> Unit>()

  /**
   * Is set to true if dispose has been called.
   * [dispose] sets it before it runs the registered actions, and it stays true afterwards - also
   * for [Mode.MultiDispose], where further actions may still be registered.
   */
  var disposed: Boolean = false
    private set

  /**
   * Registers an action that is executed when [dispose] is called.
   *
   * Throws an [IllegalStateException] if this has already been disposed and the [mode] is
   * [Mode.SingleDispose] - that includes a re-entrant call from within a running dispose action.
   */
  override fun onDispose(action: () -> Unit) {
    verifyNotDisposed()
    disposeActions.add(action)
  }

  /**
   * Executes all registered (by calling [onDispose]) actions.
   * Marks this as disposed ([disposed]).
   *
   * Every action is executed even if an earlier one throws, so a single failing action never
   * leaves the remaining resources undisposed. The first thrown exception is rethrown afterwards
   * (later ones attached as suppressed).
   */
  override fun dispose() {
    //Copy the dispose actions to avoid endless loops / recursions
    val copy = disposeActions.toList()

    //Clear the registered actions before executing them - the copy above is what runs, so a second
    //dispose (Mode.MultiDispose) never executes the same action twice
    disposeActions.clear()

    //Mark as disposed up front so a re-entrant onDispose during an action fails fast instead of
    //silently registering an action that would never run.
    disposed = true

    var firstThrown: Throwable? = null
    copy.fastForEach { action ->
      try {
        action()
      } catch (e: Throwable) {
        if (firstThrown == null) {
          firstThrown = e
        } else {
          firstThrown.addSuppressed(e)
        }
      }
    }

    firstThrown?.let { throw it }
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
    /**
     * [onDispose] throws once [dispose] has been called - the support is used up.
     */
    SingleDispose,

    /**
     * [onDispose] is accepted after [dispose] has been called. The newly registered actions are
     * executed by the next [dispose]. Used where a set of subscriptions is replaced repeatedly.
     */
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
