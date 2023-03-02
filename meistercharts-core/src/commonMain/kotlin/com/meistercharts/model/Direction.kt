package com.meistercharts.model

/**
 * Describes a direction
 *
 * Attention: The order of the direction should *not* matter.
 */
enum class Direction(
  val verticalAlignment: VerticalAlignment,
  val horizontalAlignment: HorizontalAlignment
) {
  Center(VerticalAlignment.Center, HorizontalAlignment.Center),
  CenterLeft(VerticalAlignment.Center, HorizontalAlignment.Left),
  CenterRight(VerticalAlignment.Center, HorizontalAlignment.Right),

  BaseLineCenter(VerticalAlignment.Baseline, HorizontalAlignment.Center),
  BaseLineLeft(VerticalAlignment.Baseline, HorizontalAlignment.Left),
  BaseLineRight(VerticalAlignment.Baseline, HorizontalAlignment.Right),

  TopLeft(VerticalAlignment.Top, HorizontalAlignment.Left),
  TopCenter(VerticalAlignment.Top, HorizontalAlignment.Center),
  TopRight(VerticalAlignment.Top, HorizontalAlignment.Right),

  BottomLeft(VerticalAlignment.Bottom, HorizontalAlignment.Left),
  BottomCenter(VerticalAlignment.Bottom, HorizontalAlignment.Center),
  BottomRight(VerticalAlignment.Bottom, HorizontalAlignment.Right);

  /**
   * Returns the direction with the given vertical alignment
   */
  fun with(verticalAlignment: VerticalAlignment): Direction {
    return get(verticalAlignment, this.horizontalAlignment)
  }

  /**
   * Returns the opposite anchor direction
   */
  fun opposite(): Direction {
    return when (this) {
      Center         -> Center
      CenterLeft     -> CenterRight
      CenterRight    -> CenterLeft
      BaseLineCenter -> BaseLineCenter
      BaseLineLeft   -> BaseLineRight
      BaseLineRight  -> BaseLineLeft
      TopLeft        -> BottomRight
      TopCenter      -> BottomCenter
      TopRight       -> BottomLeft
      BottomLeft     -> TopRight
      BottomCenter   -> TopCenter
      BottomRight    -> TopLeft
    }
  }

  /**
   * Returns the opposite if the given boolean is true
   */
  fun oppositeIf(useOpposite: Boolean): Direction {
    if (useOpposite) {
      return opposite()
    }

    return this
  }

  companion object {
    /**
     * All directions apart from the base-line directions
     */
    val allButBaseline: List<Direction> = listOf(
      Center,
      CenterLeft,
      CenterRight,

      TopLeft,
      TopCenter,
      TopRight,

      BottomLeft,
      BottomCenter,
      BottomRight
    )

    /**
     * Contains the 4 corners
     */
    val corners: List<Direction> = listOf(
      TopLeft,
      TopRight,
      BottomLeft,
      BottomRight
    )

    /**
     * The four sides
     */
    val sides: List<Direction> = listOf(
      CenterLeft,
      TopCenter,
      CenterRight,
      BottomCenter
    )

    /**
     * The four corners and four sides (no center and no base line
     */
    val cornersAndSides: List<Direction> = listOf(
      CenterLeft,
      CenterRight,

      TopLeft,
      TopCenter,
      TopRight,

      BottomLeft,
      BottomCenter,
      BottomRight
    )

    /**
     * Returns the anchor direction for the given vertical and horizontal alignment
     */
    fun get(verticalAlignment: VerticalAlignment, horizontalAlignment: HorizontalAlignment): Direction {
      //do *not* call values() - for performance reasons
      return when (verticalAlignment) {
        VerticalAlignment.Top ->
          when (horizontalAlignment) {
            HorizontalAlignment.Left -> TopLeft
            HorizontalAlignment.Right -> TopRight
            HorizontalAlignment.Center -> TopCenter
          }
        VerticalAlignment.Center ->
          when (horizontalAlignment) {
            HorizontalAlignment.Left -> CenterLeft
            HorizontalAlignment.Right -> CenterRight
            HorizontalAlignment.Center -> Center
          }
        VerticalAlignment.Baseline ->
          when (horizontalAlignment) {
            HorizontalAlignment.Left -> BaseLineLeft
            HorizontalAlignment.Right -> BaseLineRight
            HorizontalAlignment.Center -> BaseLineCenter
          }

        VerticalAlignment.Bottom   ->
          when (horizontalAlignment) {
            HorizontalAlignment.Left   -> BottomLeft
            HorizontalAlignment.Right  -> BottomRight
            HorizontalAlignment.Center -> BottomCenter
          }
      }
    }
  }
}
