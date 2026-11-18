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
package com.meistercharts.demo.descriptors.zeitstrahl

/**
 * Which nodes of the tree are open. Zooming in opens a node once its span fills [OpenShare] of the window width and closes it below
 * [CloseShare]; in between the node keeps its state. A click decides until the node leaves the window.
 *
 * One paint calls [update] for every node it shows and [endPass] at its end; a node without an update left the window or sits
 * in a closed node, and loses its state and its click.
 */
class OpenDecisions {
  private val open: MutableSet<EntryId> = mutableSetOf()
  private val clicked: MutableMap<EntryId, Boolean> = mutableMapOf()
  private val updated: MutableSet<EntryId> = mutableSetOf()

  /** [share] is the part of the window width the span of [id] covers, 0 to 1; returns whether [id] is open. */
  fun update(id: EntryId, share: Double): Boolean {
    updated.add(id)
    val isOpen = clicked[id] ?: when {
      share >= OpenShare -> true
      share < CloseShare -> false
      else -> id in open
    }
    if (isOpen) open.add(id) else open.remove(id)
    return isOpen
  }

  fun isOpen(id: EntryId): Boolean = id in open

  /** A click on the node in the tree: opens a closed node and closes an open one. */
  fun toggle(id: EntryId) {
    val isOpen = id !in open
    clicked[id] = isOpen
    if (isOpen) open.add(id) else open.remove(id)
  }

  fun endPass() {
    open.retainAll(updated)
    clicked.keys.retainAll(updated)
    updated.clear()
  }

  companion object {
    const val OpenShare: Double = 0.6
    const val CloseShare: Double = 0.4
  }
}
