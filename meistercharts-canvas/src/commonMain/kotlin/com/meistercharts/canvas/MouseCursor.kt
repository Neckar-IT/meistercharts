package com.meistercharts.canvas

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



