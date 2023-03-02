package com.meistercharts.algorithms.paintable

/**
 * Specifies how the content should be resized to fit its container.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/object-fit">object-fit</a>
 */
enum class ObjectFit(
  /**
   * True, if the aspect ratio is maintained
   */
  val aspectRatioMaintained: Boolean,
  /**
   * True, if the object can be scaled down
   */
  val mayBeScaledDown: Boolean,
  /**
   * True, if the object can be scaled up
   */
  val mayBeScaledUp: Boolean,
) {
  /**
   * The content is not resized.
   * The original size is retained.
   */
  None(true, false, false),

  /**
   * The content is sized to fill the container's box.
   * The content will completely fill the box.
   * If the content's aspect ratio does not match the aspect ratio of its box, then the content will be stretched to fit.
   */
  Fill(false, true, true),

  /**
   * The content is scaled (up *or* down) to maintain its aspect ratio while fitting within the container’s box.
   * The content is made to fill the box, while preserving its aspect ratio, so the content will be "letterboxed" if its aspect ratio does not match the aspect ratio of the box.
   */
  Contain(true, true, true),

  /**
   * The content is scaled (down) to maintain its aspect ratio while fitting within the container’s box.
   * The content is made to fill the box, while preserving its aspect ratio, so the content will be "letterboxed" if its aspect ratio does not match the aspect ratio of the box.
   *
   * If the content is smaller than the bounding box, the content will be centered.
   */
  ContainNoGrow(true, true, false),
}
