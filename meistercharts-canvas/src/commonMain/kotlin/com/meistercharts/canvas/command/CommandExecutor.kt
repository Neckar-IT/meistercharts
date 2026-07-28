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

import it.neckar.open.kotlin.lang.requireNotNull
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableObject

/**
 * Executes [Command]s against one model and keeps the undo and redo stacks.
 *
 * ```
 * val executor = CommandExecutor(roofPlanningModel)
 * executor.execute(DeleteModuleArea(moduleArea))
 * executor.undo()  //the module area is back
 * executor.redo()  //and gone again
 * ```
 */
class CommandExecutor<M : Any>(
  /**
   * The model every command is applied to.
   */
  val model: M,

  /**
   * How many commands can be undone. Older commands drop off the stack.
   */
  val undoLimit: Int = DefaultUndoLimit,

  /**
   * Is notified after every change to the model - both by execute and by undo/redo.
   */
  val onModelChanged: (Command<M>) -> Unit = {},
) {
  init {
    require(undoLimit > 0) { "undoLimit must be > 0 but was <$undoLimit>" }
  }

  /**
   * The commands that have been executed, oldest first.
   */
  private val executedCommands: ArrayDeque<Command<M>> = ArrayDeque()

  /**
   * The commands that have been undone, in the order they were undone.
   */
  private val undoneCommands: ArrayDeque<Command<M>> = ArrayDeque()

  val canUndoProperty: ObservableBoolean = ObservableBoolean(false)

  /**
   * Whether there is a command to undo.
   */
  val canUndo: Boolean by canUndoProperty

  val canRedoProperty: ObservableBoolean = ObservableBoolean(false)

  /**
   * Whether there is an undone command to redo.
   */
  val canRedo: Boolean by canRedoProperty

  /**
   * The description of the command [undo] would revert - null if there is nothing to undo.
   */
  val undoDescriptionProperty: ObservableObject<String?> = ObservableObject(null)

  /**
   * The description of the command [redo] would apply again - null if there is nothing to redo.
   */
  val redoDescriptionProperty: ObservableObject<String?> = ObservableObject(null)

  /**
   * Whether the command on top of the undo stack may still absorb further commands of its gesture.
   */
  private var gestureOpen: Boolean = false

  /**
   * Applies the command and puts it on the undo stack. Discards the redo stack, because the history the undone
   * commands belong to no longer exists.
   *
   * A command that continues the gesture of the command on top of the stack replaces it, so one drag stays one undo
   * entry. [finishGesture] closes the gesture.
   */
  fun execute(command: Command<M>) {
    command.execute(model)

    val previous = executedCommands.lastOrNull()
    val continuesGesture = gestureOpen &&
      previous != null &&
      command.gestureKey != null &&
      command.gestureKey == previous.gestureKey

    if (continuesGesture) {
      executedCommands.removeLast()
      executedCommands.addLast(command.mergeWith(previous.requireNotNull { "A continued gesture needs a previous command" }))
    } else {
      executedCommands.addLast(command)
    }

    gestureOpen = command.gestureKey != null

    while (executedCommands.size > undoLimit) {
      executedCommands.removeFirst()
    }
    undoneCommands.clear()

    updateProperties()
    onModelChanged(command)
  }

  /**
   * Closes the ongoing gesture: the next command starts a new undo entry even if it carries the same gesture key.
   *
   * Called when a drag ends.
   */
  fun finishGesture() {
    gestureOpen = false
  }

  /**
   * Applies every command in order, as one entry on the undo stack.
   *
   * Use this when one gesture produces several changes that a user thinks of as one action.
   */
  fun execute(commands: List<Command<M>>) {
    when (commands.size) {
      0 -> return
      1 -> execute(commands.first())
      else -> execute(CompositeCommand(commands))
    }
  }

  /**
   * Reverts the most recent command. Does nothing if there is none.
   */
  fun undo() {
    val command = executedCommands.removeLastOrNull() ?: return

    command.undo(model)
    undoneCommands.addLast(command)
    gestureOpen = false

    updateProperties()
    onModelChanged(command)
  }

  /**
   * Applies the most recently undone command again. Does nothing if there is none.
   */
  fun redo() {
    val command = undoneCommands.removeLastOrNull() ?: return

    command.execute(model)
    executedCommands.addLast(command)
    gestureOpen = false

    updateProperties()
    onModelChanged(command)
  }

  /**
   * Drops both stacks - for example after the model has been replaced or saved.
   */
  fun clearHistory() {
    executedCommands.clear()
    undoneCommands.clear()
    gestureOpen = false
    updateProperties()
  }

  private fun updateProperties() {
    canUndoProperty.value = executedCommands.isNotEmpty()
    canRedoProperty.value = undoneCommands.isNotEmpty()
    undoDescriptionProperty.value = executedCommands.lastOrNull()?.description
    redoDescriptionProperty.value = undoneCommands.lastOrNull()?.description
  }

  companion object {
    const val DefaultUndoLimit: Int = 100
  }
}
