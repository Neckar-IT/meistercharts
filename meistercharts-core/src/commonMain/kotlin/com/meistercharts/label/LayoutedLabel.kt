package com.meistercharts.label

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Rectangle
import it.neckar.open.unit.other.px

/**
 * Contains all information related to the layout of a label.
 *
 * ATTENTION: This object is *mutable*. During the layout process some of the values are updated
 *
 * ATTENTION: Currently there is no implementation for the label layouter on the X axis. Therefore this class
 * uses a lot of references to "y" in its names.
 * When the label layouter supports flipped axis, this has to updated
 *
 */
@Deprecated("replaced with new label painter")
class LayoutedLabel(
  /**
   * The domain relative label that is the base for the layouted label
   */
  val domainRelativeLabel: DomainRelativeLabel,
  /**
   * The location within the window - based upon the domain relative value.
   *
   * Only contains one value (x or y) depending on the orientation of the chart.
   *
   * In most cases the Y axis is the value axis. And this location describes
   * an y value
   */
  @Window val locationInWindow: Double
) {

  val labelData: LabelData
    get() = domainRelativeLabel.labelData

  /*
   *
   *
   * All values below are updated during the layout phase of the labels
   * Theses value are then used to paint the label
   *
   *
   */

  /**
   * The bounds where this label is painted.
   *
   * This are the final bounds that are calculated on paint
   */
  @Window
  var bounds: Rectangle = Rectangle.zero


  /**
   * The natural/preferred location of the label.
   * This is the best location where the label would be placed if there were no other labels
   */
  @Window
  var preferredCenterY: Double = 0.0
    set(value) {
      field = value
      //Apply the new preferred center also to the actual center
      actualCenterY = value
    }

  /**
   * The height of the label in pixels
   */
  @px
  @Zoomed
  var height: Double = 0.0

  /**
   * The absolute min value for the center.
   * *Might* be larger then [absoluteCenterYMax] if there is not enough space for all labels
   */
  @Window
  var absoluteCenterYMin: Double = -Double.MAX_VALUE

  /**
   * The absolute max value for the center
   * *Might* be smaller then [absoluteCenterYMin] if there is not enough space for all labels
   */
  @Window
  var absoluteCenterYMax: Double = Double.MAX_VALUE

  /**
   * The actual position where the label is painted.
   * Is calculated and depends on the amount and location of other labels
   */
  @Window
  var actualCenterY: Double = preferredCenterY
    set(value) {
      field = if (absoluteCenterYMin >= absoluteCenterYMax) {
        value
      } else {
        /**
         * Ensures the value is between the absolute min and max
         */
        value.coerceIn(absoluteCenterYMin, absoluteCenterYMax)
      }
    }

  /**
   * Half of the height
   */
  @Zoomed
  val halfHeight: Double
    get() = height / 2.0

  /**
   * The minimum y value
   */
  @Window
  val actualMinY: Double
    get() = actualCenterY - height / 2.0


  /**
   * The maximum y value
   */
  @Window
  val actualMaxY: Double
    get() = actualCenterY + height / 2.0

  /**
   * Returns whether the given y value is within the actual range of this label info
   */
  fun containsActual(@px @Window y: Double): Boolean {
    return y in actualMinY..actualMaxY
  }

  /**
   * Returns true if the actual y is different than the natural y
   */
  fun hasModifiedActualY(): Boolean {
    return preferredCenterY != actualCenterY
  }

  /**
   * Returns true if the actual y values overlaps with the actual bounds of the other label
   */
  fun overlapsActualY(other: LayoutedLabel): Boolean {
    //this is above other
    if (actualMinY > other.actualMaxY) {
      return false
    }

    //this is below other
    return actualMaxY >= other.actualMinY
  }

  /**
   * Moves the bounds
   */
  fun moveBounds(@px deltaX: Double, @px deltaY: Double) {
    bounds = bounds.move(deltaX, deltaY)
  }
}
