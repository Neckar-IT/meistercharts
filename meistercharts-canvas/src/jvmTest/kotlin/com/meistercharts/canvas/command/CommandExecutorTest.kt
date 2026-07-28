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

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CommandExecutorTest {

  private lateinit var model: TestModel
  private lateinit var executor: CommandExecutor<TestModel>

  @BeforeEach
  fun setUp() {
    model = TestModel()
    executor = CommandExecutor(model)
  }

  @Test
  fun `a fresh executor has nothing to undo or redo`() {
    assertThat(executor.canUndo).isFalse()
    assertThat(executor.canRedo).isFalse()
    assertThat(executor.undoDescriptionProperty.value).isNull()
    assertThat(executor.redoDescriptionProperty.value).isNull()
  }

  @Test
  fun `execute applies the command and offers to undo it`() {
    executor.execute(Append("a"))

    assertThat(model.entries).containsExactly("a")
    assertThat(executor.canUndo).isTrue()
    assertThat(executor.undoDescriptionProperty.value).isEqualTo("Append a")
  }

  @Test
  fun `undo restores the state before the command`() {
    executor.execute(Append("a"))
    executor.execute(Append("b"))
    executor.undo()

    assertThat(model.entries).containsExactly("a")
    assertThat(executor.canRedo).isTrue()
    assertThat(executor.redoDescriptionProperty.value).isEqualTo("Append b")
  }

  @Test
  fun `redo applies the undone command again`() {
    executor.execute(Append("a"))
    executor.undo()
    executor.redo()

    assertThat(model.entries).containsExactly("a")
    assertThat(executor.canRedo).isFalse()
  }

  @Test
  fun `undoing everything empties the undo stack`() {
    executor.execute(Append("a"))
    executor.execute(Append("b"))
    executor.undo()
    executor.undo()

    assertThat(model.entries).isEmpty()
    assertThat(executor.canUndo).isFalse()
  }

  @Test
  fun `undo on an empty stack does nothing`() {
    executor.undo()

    assertThat(model.entries).isEmpty()
    assertThat(executor.canUndo).isFalse()
  }

  @Test
  fun `redo on an empty stack does nothing`() {
    executor.redo()

    assertThat(model.entries).isEmpty()
    assertThat(executor.canRedo).isFalse()
  }

  @Test
  fun `a new command discards the redo stack`() {
    executor.execute(Append("a"))
    executor.undo()
    executor.execute(Append("b"))

    assertThat(model.entries).containsExactly("b")
    assertThat(executor.canRedo).isFalse()
  }

  @Test
  fun `a list of commands is undone as one action`() {
    executor.execute(listOf(Append("a"), Append("b"), Append("c")))

    assertThat(model.entries).containsExactly("a", "b", "c")

    executor.undo()

    assertThat(model.entries).isEmpty()
    assertThat(executor.canUndo).isFalse()
  }

  @Test
  fun `a single element list is not wrapped`() {
    executor.execute(listOf(Append("a")))

    assertThat(executor.undoDescriptionProperty.value).isEqualTo("Append a")
  }

  @Test
  fun `an empty list changes nothing`() {
    executor.execute(emptyList())

    assertThat(model.entries).isEmpty()
    assertThat(executor.canUndo).isFalse()
  }

  @Test
  fun `a composite command is undone in reverse order`() {
    val order = mutableListOf<String>()
    executor.execute(CompositeCommand(listOf(Recording("first", order), Recording("second", order))))
    executor.undo()

    assertThat(order).containsExactly("execute first", "execute second", "undo second", "undo first")
  }

  @Test
  fun `the oldest command drops off once the undo limit is reached`() {
    val limitedExecutor = CommandExecutor(model, undoLimit = 2)
    limitedExecutor.execute(Append("a"))
    limitedExecutor.execute(Append("b"))
    limitedExecutor.execute(Append("c"))

    limitedExecutor.undo()
    limitedExecutor.undo()

    assertThat(model.entries).containsExactly("a")
    assertThat(limitedExecutor.canUndo).isFalse()
  }

  @Test
  fun `an undo limit below one is rejected`() {
    assertFailure { CommandExecutor(model, undoLimit = 0) }.isInstanceOf(IllegalArgumentException::class)
  }

  @Test
  fun `every model change notifies the listener`() {
    val notifications = mutableListOf<String>()
    val notifyingExecutor = CommandExecutor(model, onModelChanged = { notifications.add(it.description) })

    notifyingExecutor.execute(Append("a"))
    notifyingExecutor.undo()
    notifyingExecutor.redo()

    assertThat(notifications).containsExactly("Append a", "Append a", "Append a")
  }

  @Test
  fun `clearHistory drops both stacks but keeps the model`() {
    executor.execute(Append("a"))
    executor.undo()
    executor.clearHistory()

    assertThat(executor.canUndo).isFalse()
    assertThat(executor.canRedo).isFalse()
  }

  @Test
  fun `a composite command without commands is rejected`() {
    assertFailure { CompositeCommand<TestModel>(emptyList()) }.isInstanceOf(IllegalArgumentException::class)
  }

  private class TestModel {
    val entries: MutableList<String> = mutableListOf()
  }

  private class Append(val entry: String) : Command<TestModel> {
    override val description: String = "Append $entry"

    override fun execute(model: TestModel) {
      model.entries.add(entry)
    }

    override fun undo(model: TestModel) {
      model.entries.remove(entry)
    }
  }

  private class Recording(val name: String, val order: MutableList<String>) : Command<TestModel> {
    override val description: String = name

    override fun execute(model: TestModel) {
      order.add("execute $name")
    }

    override fun undo(model: TestModel) {
      order.add("undo $name")
    }
  }
}
