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
package com.meistercharts.canvas.command

import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachReversed

/**
 * Several commands the user thinks of as one action.
 *
 * Undo reverts them in reverse order, so a later command never sees a model the earlier one has already been taken back
 * from.
 */
class CompositeCommand<M : Any>(
  val commands: List<Command<M>>,
  override val description: String = commands.firstOrNull()?.description.orEmpty(),
) : Command<M> {

  init {
    require(commands.isNotEmpty()) { "A composite command needs at least one command" }
  }

  override fun execute(model: M) {
    commands.fastForEach { it.execute(model) }
  }

  override fun undo(model: M) {
    commands.fastForEachReversed { it.undo(model) }
  }

  override fun toString(): String {
    return "CompositeCommand(${commands.joinToString { it.description }})"
  }
}
