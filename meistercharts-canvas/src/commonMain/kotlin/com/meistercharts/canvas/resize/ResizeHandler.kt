package com.meistercharts.canvas.resize

import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import it.neckar.open.unit.other.px

/**
 * Handler that receives resize events.
 */
interface ResizeHandler {
  /**
   * Is notified when the mouse is above the handle
   */
  fun armed(handleDirection: Direction) {
  }

  /**
   * Is
   */
  fun disarmed() {
  }

  fun beginResizing(handleDirection: Direction) {
  }

  /**
   * Is called for each resize event
   */
  fun resizing(
    /**
     * The mouse movement distance taken from the original event
     */
    rawDistance: @px Distance,
    /**
     * The handle direction
     */
    handleDirection: Direction,
    /**
     * The horizontal distance (0.0 for center handles)
     */
    deltaX: @px @Zoomed Double,
    /**
     * The vertical distance (0.0 for center handles)
     */
    deltaY: @px @Zoomed Double,
  )

  fun resizingFinished() {
  }
}
