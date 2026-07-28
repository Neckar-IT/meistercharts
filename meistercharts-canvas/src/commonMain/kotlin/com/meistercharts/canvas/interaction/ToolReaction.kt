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

import com.meistercharts.canvas.command.Command
import com.meistercharts.events.EventConsumption

/**
 * What an [InteractionTool] answers to one input event: the new interaction state, the model changes it asks for, and
 * whether the event is consumed.
 *
 * A tool never changes the model itself. It returns commands, and the layer hands them to the executor - that is what
 * makes a tool testable without a canvas and what makes every change undoable.
 */
data class ToolReaction<out E : Any, M : Any>(
  /**
   * The interaction state after the event.
   */
  val state: InteractionState<E>,

  /**
   * The changes the event asks for. Executed as one undo entry.
   */
  val commands: List<Command<M>> = emptyList(),

  /**
   * Whether the event is consumed. Defaults to consumed, because a tool that reacted to an event has handled it.
   */
  val consumption: EventConsumption = EventConsumption.Consumed,
) {
  companion object {
    /**
     * The tool does not react: the state stays as it is and the event is passed on.
     */
    fun <E : Any, M : Any> ignored(state: InteractionState<E>): ToolReaction<E, M> {
      return ToolReaction(state, emptyList(), EventConsumption.Ignored)
    }
  }
}
