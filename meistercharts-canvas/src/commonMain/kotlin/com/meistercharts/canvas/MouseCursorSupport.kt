/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.canvas

import it.neckar.open.collections.removeIfOrNull
import it.neckar.open.observable.ObservableObject
import it.neckar.open.unit.other.Order
import it.neckar.open.unit.other.Sorted
import kotlin.jvm.JvmInline

/**
 * Supports mouse cursor management for the canvas
 */
class MouseCursorSupport(
  private val mouseCursor: ObservableObject<MouseCursor>,
) {
  /**
   * Contains the current cursor properties. Sorted by [Entry.priority].
   *
   * Different layers can request a cursor in different properties.
   * The first property that contains a non-null mouse cursor is used.
   */
  private val requestedCursors: @Sorted(by = "Entry.priority", value = Order.DESC) MutableList<Entry> = mutableListOf()

  /**
   * Returns the cursor property for the given key (which usually represents a layer)
   */
  fun cursorProperty(key: Any, priority: Priority): ObservableObject<MouseCursor?> {
    val found = requestedCursors.find { it.key == key }
    if (found != null) {
      return found.cursorProperty
    }

    //No entry found, create a new one. The self-subscription is tracked so [clearProperty]'s
    //dispose() on the cursorProperty releases the listener along with the property itself.
    val cursorProperty: ObservableObject<MouseCursor?> = ObservableObject(null)
    cursorProperty.addUpstreamSubscription(cursorProperty.consumeImmediately {
      updateRequestedCursors()
    })

    requestedCursors.add(Entry(key, priority, cursorProperty))
    requestedCursors.sortByDescending { it.priority }

    return cursorProperty
  }

  /**
   * Clears the property for the given key
   */
  fun clearProperty(key: Any): ObservableObject<MouseCursor?>? {
    val found = requestedCursors.removeIfOrNull { it.key == key }
    if (found != null) {
      found.cursorProperty.dispose()

      //Recalculate the requested cursors
      updateRequestedCursors()
    }

    return found?.cursorProperty
  }

  /**
   * This method is called, whenever one of the cursor properties has changed.
   * It recalculates the requested cursors and updates the mouse cursor.
   */
  private fun updateRequestedCursors() {
    mouseCursor.value = calculateRequestedCursors() ?: MouseCursor.Default
  }

  /**
   * Recalculates the requested cursors
   */
  private fun calculateRequestedCursors(): MouseCursor? {
    return requestedCursors
      .asSequence()
      .map {
        it.cursorProperty.value
      }.filterNotNull()
      .filter { it != MouseCursor.Default } //Skip default mouse cursors
      .firstOrNull()
  }

  /**
   * Represents an entry in the cursor stack.
   */
  data class Entry(
    /**
     * The key that has been used to register the property
     */
    val key: Any,

    /**
     * The priority of the cursor
     */
    val priority: Priority,

    /**
     * The cursor property itself.
     */
    val cursorProperty: ObservableObject<MouseCursor?>,
  )


  /**
   * Represents a mouse cursor priority.
   * The higher the value, the higher the priority.
   */
  @JvmInline
  value class Priority(val value: Int) : Comparable<Priority> {
    override fun compareTo(other: Priority): Int {
      return value.compareTo(other.value)
    }

    companion object {
      /**
       * The lowest priority for mouse cursors - should be used for "background" layers
       */
      val Low: Priority = Priority(-10)

      /**
       * The default priority for mouse cursors
       */
      val Default: Priority = Priority(0)

      /**
       * Should be used for layers that have a high precision when calculating the mouse cursor (e.g. interactive components)
       */
      val High: Priority = Priority(10)
    }
  }
}

