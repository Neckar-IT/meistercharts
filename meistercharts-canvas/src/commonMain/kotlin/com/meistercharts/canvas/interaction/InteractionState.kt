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
package com.meistercharts.canvas.interaction

import com.meistercharts.annotations.Window
import it.neckar.geometry.Coordinates

/**
 * What the user is currently doing with the elements of a chart - as plain data.
 *
 * There is no behaviour here and no reference to a model, a layer or a canvas: an [InteractionTool] returns a new
 * instance, painting reads it, and a test constructs one as a literal.
 */
data class InteractionState<out E : Any>(
  /**
   * The region under the mouse, or null if the mouse is over nothing (or outside the canvas).
   *
   * A region with [HandleRole.Body] means the element itself is hovered; any other role means the button of that
   * element is armed and a release triggers it.
   */
  val hovered: InteractionTarget<E>? = null,

  /**
   * The selected element, or null if nothing is selected.
   */
  val selected: E? = null,

  /**
   * The element being dragged, or null if no drag is in progress.
   */
  val dragged: E? = null,

  /**
   * The element being resized, or null if no resize is in progress.
   */
  val resized: E? = null,

  /**
   * Where the mouse is while the user builds something that follows the pointer - a string being wired up, for
   * example. Null when nothing follows the pointer.
   */
  val pendingPoint: @Window Coordinates? = null,
) {
  /**
   * The hovered element, regardless of which of its regions the mouse is over.
   */
  val hoveredElement: E?
    get() = hovered?.element

  /**
   * The role of the region under the mouse, or null if the mouse is over nothing.
   */
  val armedRole: HandleRole?
    get() = hovered?.role

  /**
   * Returns true while the user drags or resizes something.
   */
  val changingGeometry: Boolean
    get() = dragged != null || resized != null

  /**
   * A short form for debug overlays and logs - the elements themselves can have very long descriptions.
   */
  override fun toString(): String {
    return buildString {
      append("InteractionState(")
      append("hovered=").append(hovered?.let { "${it.element::class.simpleName}/${it.role}" } ?: "-")
      append(", selected=").append(selected?.let { it::class.simpleName } ?: "-")
      append(", dragged=").append(dragged?.let { it::class.simpleName } ?: "-")
      append(", resized=").append(resized?.let { it::class.simpleName } ?: "-")
      append(", pendingPoint=").append(pendingPoint ?: "-")
      append(")")
    }
  }

  companion object {
    private val Empty: InteractionState<Nothing> = InteractionState()

    /**
     * Nothing hovered, nothing selected, no gesture in progress.
     */
    fun <E : Any> empty(): InteractionState<E> = Empty
  }
}
