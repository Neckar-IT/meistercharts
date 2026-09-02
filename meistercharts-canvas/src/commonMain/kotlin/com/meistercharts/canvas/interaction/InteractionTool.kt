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
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.MouseCursor
import it.neckar.events.KeyStroke
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Distance

/**
 * Decides what the mouse does to the elements of type [E] of a model of type [M].
 *
 * A tool answers input events with a [ToolReaction]: the new [InteractionState] and the commands the event asks for. It
 * receives the region under the mouse from the [InteractionTargets] registry and never queries the canvas itself, so a
 * tool is tested by handing it a target and reading the commands it returns.
 *
 * ```
 * val reaction = tool.onPress(InteractionTarget(module, HandleRole.Body, bounds), state, model)
 * assertThat(reaction.commands).containsExactly(RemoveModuleFromString(stringB.uuid, module))
 * ```
 */
interface InteractionTool<E : Any, M : Any> {
  /**
   * How the tool is named in the user interface.
   */
  val description: String

  /**
   * The mouse moved. [target] is the region under the mouse, or null if the mouse is over nothing or left the canvas.
   */
  fun onHover(target: InteractionTarget<E>?, location: @Window Coordinates?, state: InteractionState<E>, model: M): ToolReaction<E, M>

  /**
   * A mouse button went down over [target] - null if the press happened over nothing.
   */
  fun onPress(target: InteractionTarget<E>?, location: @Window Coordinates, state: InteractionState<E>, model: M): ToolReaction<E, M>

  /**
   * The mouse moved by [totalDistance] since the drag started.
   *
   * The distance is the total of the gesture, not the delta since the last event, so a tool can produce one command for
   * the whole drag and the executor keeps one undo entry. Apply it to [InteractionState.Drag.from].
   */
  fun onDrag(totalDistance: @Zoomed Distance, state: InteractionState<E>, model: M): ToolReaction<E, M> {
    return ToolReaction.ignored(state)
  }

  /**
   * The mouse button was released, ending a drag if one was in progress.
   */
  fun onRelease(state: InteractionState<E>, model: M): ToolReaction<E, M> {
    return ToolReaction(state)
  }

  /**
   * A key was released.
   */
  fun onKey(keyStroke: KeyStroke, state: InteractionState<E>, model: M): ToolReaction<E, M> {
    return ToolReaction.ignored(state)
  }

  /**
   * The cursor the tool wants for the given state - null for the default cursor.
   */
  fun cursorFor(state: InteractionState<E>): MouseCursor?
}
