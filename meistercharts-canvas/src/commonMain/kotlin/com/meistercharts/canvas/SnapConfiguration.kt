package com.meistercharts.canvas

import com.meistercharts.model.Insets
import it.neckar.open.unit.other.px

/**
 * Whether to snap to full values
 */
enum class SnapConfiguration(
  /**
   * Snap on the x axis
   */
  val snapX: Boolean,
  /**
   * Snap on the y axis
   */
  val snapY: Boolean,
) {
  /**
   * Does not snap
   */
  None(false, false),
  OnlyX(true, false),
  OnlyY(false, true),
  Both(true, true);

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapXValue(@px value: Double): Double {
    return PaintingUtils.snapPosition(value, snapX)
  }

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapXSize(@px size: Double): Double {
    return PaintingUtils.snapSize(size, snapX)
  }

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapYValue(@px value: Double): Double {
    return PaintingUtils.snapPosition(value, snapY)
  }

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapYSize(@px size: Double): Double {
    return PaintingUtils.snapSize(size, snapY)
  }

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapInsets(@px insets: Insets): Insets {
    return Insets(
      snapYValue(insets.top),
      snapXValue(insets.right),
      snapYValue(insets.bottom),
      snapXValue(insets.left)
    )
  }
}

/**
 * Snaps to the physical pixels - depending on the snap configuration
 */
fun CanvasRenderingContext.snapPhysicalTranslation(snapConfiguration: SnapConfiguration) {
  this.snapPhysicalTranslation(snapX = snapConfiguration.snapX, snapY = snapConfiguration.snapY)
}
