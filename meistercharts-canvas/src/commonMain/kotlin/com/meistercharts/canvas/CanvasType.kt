package com.meistercharts.canvas

/**
 * Describes the type of the canvas.
 */
enum class CanvasType {
  /**
   * A "main" canvas that is directly shown to the user.
   * Has listeners registered
   */
  Main,

  /**
   * A canvas that is only used for calculate off screen images.
   * No interaction happens on this canvas - therefore no listeners are registered
   */
  OffScreen
}
