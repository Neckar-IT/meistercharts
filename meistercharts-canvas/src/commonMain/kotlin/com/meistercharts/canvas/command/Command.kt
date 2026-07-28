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

/**
 * One reversible change to a model of type [M].
 *
 * Interaction handling never changes the model directly - it creates commands and hands them to a [CommandExecutor].
 * That is what makes undo possible and what keeps the model change in one testable place.
 *
 * A command is executed exactly once before it is undone, and [undo] must restore the state [execute] found. A command
 * that cannot restore that state (because it dropped the information it overwrote) is a broken command.
 */
interface Command<M : Any> {
  /**
   * What the command does, for the undo entry and for logging. Written for a user, not for a developer.
   */
  val description: String

  /**
   * Applies the change.
   */
  fun execute(model: M)

  /**
   * Reverts the change applied by [execute].
   */
  fun undo(model: M)

  /**
   * Identifies the ongoing gesture this command belongs to - null for a command that always gets its own undo entry.
   *
   * A drag fires one command per mouse move, but the user made one movement and expects one undo. Commands that follow
   * each other with the same key are therefore collapsed into a single undo entry via [mergeWith]. The gesture is closed
   * by [CommandExecutor.finishGesture], so a second drag of the same element is a second undo entry.
   */
  val gestureKey: Any?
    get() = null

  /**
   * Returns the command that has the combined effect of [previous] and this one.
   *
   * Only called for two commands of the same gesture. The result must undo back to the state [previous] found, not to
   * the state this command found.
   */
  fun mergeWith(previous: Command<M>): Command<M> {
    return this
  }
}
