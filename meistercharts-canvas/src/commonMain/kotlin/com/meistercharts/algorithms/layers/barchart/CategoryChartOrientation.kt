package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.model.Orientation
import com.meistercharts.algorithms.layout.LayoutDirection

/**
 * Defines how the category chart (e.g. bar chart) is oriented
 *
 */
enum class CategoryChartOrientation(
  /**
   * The orientation of the categories (e.g. bars) itself
   */
  val categoryOrientation: Orientation,
  /**
   * The layout direction of the categories (e.g. bars).
   * The categories are placed in that direction.
   */
  val layoutDirection: LayoutDirection
) {
  /**
   * ```
   * ━━━
   * ━━
   * ━━━━━━━
   * ━━━━
   * <empty space>
   *```
   */
  HorizontalTop(Orientation.Horizontal, LayoutDirection.TopToBottom),

  /**
   * ```
   * <empty space / 2.0>
   * ━━━
   * ━━
   * ━━━━━━━
   * ━━━━
   * <empty space / 2.0>
   *```
   */
  HorizontalCenter(Orientation.Horizontal, LayoutDirection.CenterVertical),

  /**
   * ```
   * <empty space>
   * ━━━
   * ━━
   * ━━━━━━━
   * ━━━━
   *```
   */
  HorizontalBottom(Orientation.Horizontal, LayoutDirection.BottomToTop),

  /**
   * ```
   *  ┃     ┃
   *  ┃   ┃ ┃
   *  ┃ ┃ ┃ ┃ <empty space>
   *```
   */
  VerticalLeft(Orientation.Vertical, LayoutDirection.LeftToRight),

  /**
   * ```
   *                     ┃     ┃
   *                     ┃   ┃ ┃
   * <empty space / 2.0> ┃ ┃ ┃ ┃ <empty space / 2.0>
   *```
   */
  VerticalCenter(Orientation.Vertical, LayoutDirection.CenterHorizontal),

  /**
   * ```
   *               ┃     ┃
   *               ┃   ┃ ┃
   * <empty space> ┃ ┃ ┃ ┃
   *```
   */
  VerticalRight(Orientation.Vertical, LayoutDirection.RightToLeft);

  init {
    require(layoutDirection.orientation != categoryOrientation) {
      "Invalid combination of layout direction <$layoutDirection> and orientation <$categoryOrientation>"
    }
  }
}
