/**
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

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.trace
import it.neckar.open.annotations.Boxed
import kotlin.jvm.JvmInline

/**
 * Supports the dirty state
 */
class DirtySupport {
  /**
   * Whether we are dirty or not.
   */
  var dirty: Boolean = false
    private set

  /**
   * Contains the dirty reasons bit set
   */
  var dirtyReasonsBits: DirtyReasonBitSet = DirtyReasonBitSet.empty
    private set

  /**
   * Sets the dirty-state to `true`
   * @see [clearIsDirty]
   */
  fun markAsDirty(reason: DirtyReason) {
    dirty = true

    logger.trace { "Marking as dirty because of ${reason.format()}" }

    dirtyReasonsBits = dirtyReasonsBits or reason
  }

  /**
   * Sets the dirty-state to `false`
   * @see [markAsDirty]
   */
  fun clearIsDirty() {
    dirty = false
    dirtyReasonsBits = DirtyReasonBitSet.empty
  }

  /**
   * Calls the given function if dirty state is `true`
   */
  inline fun ifDirty(function: (dirtyReasons: DirtyReasonBitSet) -> Unit) {
    if (!dirty) {
      return
    }
    val dirtyReasonBitSet = dirtyReasonsBits
    clearIsDirty()
    function(dirtyReasonBitSet)
  }

  /**
   * Checks if the dirty flag was set due to a specific DirtyReason.
   *
   * @param reason The DirtyReason to check
   * @return `true` if the dirty flag was set due to the specified DirtyReason, otherwise `false`
   */
  fun isDirtyBecause(reason: DirtyReason): Boolean {
    return dirtyReasonsBits.isDirtyBecause(reason)
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.canvas.DirtySupport")
  }
}

/**
 * Contains the dirty reasons bitset
 */
@JvmInline
value class DirtyReasonBitSet(val value: Int) {
  infix fun or(dirtyReason: DirtyReason): DirtyReasonBitSet {
    return DirtyReasonBitSet(this.value.or(dirtyReason.value))
  }

  fun isDirtyBecause(reason: DirtyReason): Boolean {
    return (value and reason.value) == reason.value
  }

  override fun toString(): String {
    return "DirtyReasonBitSet(value=0b_${value.toString(2)})"
  }

  companion object {
    val empty: DirtyReasonBitSet = DirtyReasonBitSet(0)

    /**
     * Only
     */
    val Unknown: DirtyReasonBitSet = empty or DirtyReason.Unknown
  }
}

/**
 * Represents a dirty reason.
 * Wraps an integer for performance reasons
 */
@JvmInline
value class DirtyReason(val value: Int) {

  override fun toString(): String {
    return "DirtyReason(value=${value.toString(2)})"
  }

  /**
   * Formats the dirty reason as a (human-readable) string
   */
  fun format(): String {
    return when (this) {
      UserInteraction -> "UserInteraction"
      UiStateChanged -> "UiStateChanged"
      ConfigurationChanged -> "ConfigurationChanged"
      ChartStateChanged -> "ChartStateChanged"
      Tooltip -> "Tooltip"
      ResourcesLoaded -> "ResourcesLoaded"
      DataUpdated -> "DataUpdated"
      ActiveElementUpdated -> "ActiveElementUpdated"
      Animation -> "Animation"
      Visibility -> "Visibility"
      Initial -> "Initial"
      Unknown -> "Unknown"
      else -> "$value"
    }
  }

  companion object {
    /**
     * Represents a user interaction (e.g. mouse interaction)
     */
    val UserInteraction: DirtyReason = DirtyReason(0b0000000_00000000_00000000_00000010)

    /**
     * Generic UI state changed reason. E.g. resize handles became visible
     */
    val UiStateChanged: DirtyReason = DirtyReason(0b0000000_00000000_00000000_00000100)

    val ConfigurationChanged: DirtyReason = DirtyReason(0b0000000_00000000_00000000_10000000)

    val ChartStateChanged: DirtyReason = DirtyReason(0b0000000_00000000_00000000_00001000)

    val Tooltip: DirtyReason = DirtyReason(0b0000000_00000000_00000100_00000000)

    val ResourcesLoaded: DirtyReason = DirtyReason(0b0000000_00100000_00000000_00000000)

    /**
     * Some data has been updated
     */
    val DataUpdated: DirtyReason = DirtyReason(0b0000000_00000000_00000000_00010000)

    /**
     * Is called when the active elements have been updated (usually by a mouse move)
     */
    val ActiveElementUpdated: DirtyReason = DirtyReason(0b0000000_00000000_00000001_00000000)

    /**
     * Repaint necessary because of an animation
     */
    val Animation: DirtyReason = DirtyReason(0b0000000_00000000_00000010_00000000)

    val Visibility: DirtyReason = DirtyReason(0b0000000_00000000_00010000_00000000)

    /**
     * Used for the initial repaint - or if painting has been enabled again
     */
    val Initial: DirtyReason = DirtyReason(0b0100000_00000000_00000000_00000000)

    /**
     * Represents an unknown reason. Should only be used for backwards compatibility.
     */
    val Unknown: DirtyReason = DirtyReason(0b1000000_00000000_00000000_00000000)


    /**
     * Contains all entries.
     *
     * ATTENTION: Do *not* use in production code, since the elements are boxed!
     *
     * These values are verified by a unit test.
     * If the test fails the updated code will be generated on the console.
     */
    @Boxed
    val entries: List<DirtyReason> = listOf(
      ActiveElementUpdated, Animation, ChartStateChanged, ConfigurationChanged, DataUpdated, Initial, ResourcesLoaded, Tooltip, UiStateChanged, Unknown, UserInteraction, Visibility
    )

    /**
     * Contains the labels
     *
     * These values are verified by a unit test.
     * If the test fails the updated code will be generated on the console.
     */
    @Boxed
    val entryLabels: Map<DirtyReason, String> = mapOf(
      ActiveElementUpdated to "ActiveElementUpdated",
      Animation to "Animation",
      ChartStateChanged to "ChartStateChanged",
      ConfigurationChanged to "ConfigurationChanged",
      DataUpdated to "DataUpdated",
      Initial to "Initial",
      ResourcesLoaded to "ResourcesLoaded",
      Tooltip to "Tooltip",
      UiStateChanged to "UiStateChanged",
      Unknown to "Unknown",
      UserInteraction to "UserInteraction",
      Visibility to "Visibility"
    )
  }
}
