package com.meistercharts.canvas

import it.neckar.open.observable.ObservableObject

/**
 * Supports mouse cursor management for the canvas
 */
class MouseCursorSupport(
  private val mouseCursor: ObservableObject<MouseCursor>
) {
  /**
   * Contains requested cursor properties.
   *
   * Different layers can request an cursor in different properties.
   * The first property that contains a non null mouse cursor is used.
   */
  private val requestedCursors: MutableMap<Any, ObservableObject<MouseCursor?>> = mutableMapOf()

  /**
   * Returns the cursor property for the given key (which usually represents a layer)
   */
  fun cursorProperty(key: Any): ObservableObject<MouseCursor?> {
    return requestedCursors.getOrPut(key) {
      val observableObject: ObservableObject<MouseCursor?> = ObservableObject(null)
      observableObject.consumeImmediately {
        updateRequestedCursors()
      }

      observableObject
    }
  }

  /**
   * Clears the property for the given key
   */
  fun clearProperty(key: Any): ObservableObject<MouseCursor?>? {
    return requestedCursors.remove(key)
  }

  private fun updateRequestedCursors() {
    mouseCursor.value = calculateRequestedCursors() ?: MouseCursor.Default
  }

  /**
   * Recalculates the requested cursors
   */
  private fun calculateRequestedCursors(): MouseCursor? {
    return requestedCursors
      .values
      .asSequence()
      .map {
        it.value
      }.filterNotNull()
      .filter { it != MouseCursor.Default } //Skip default mouse cursors
      .firstOrNull()
  }
}

/**
 * The supported mouse cursors
 */
enum class MouseCursor {
  Default,
  /**
   * A pointing hand. indicates that something can be clicked - for example a hyperlink
   */
  Hand,
  /**
   * An open hand
   */
  OpenHand,
  /**
   * A closed hand. Can be used when something has been "grapped" (e.g. when panning)
   */
  ClosedHand,
  CrossHair,
  /**
   * Used for text input
   */
  Text,
  /**
   * Indicates that the UI is busy at the moment
   */
  Busy,
  /**
   * Indicates a movement
   */
  Move,
  /**
   * No cursor is shown (if supported).
   * If a platform does not support this, [Default] cursor will be shown.
   */
  None,

  ResizeNorth,
  ResizeNorthEast,
  ResizeEast,
  ResizeSouthEast,
  ResizeSouth,
  ResizeSouthWest,
  ResizeWest,
  ResizeNorthWest,
  ResizeEastWest,
  ResizeNorthSouth,
}
