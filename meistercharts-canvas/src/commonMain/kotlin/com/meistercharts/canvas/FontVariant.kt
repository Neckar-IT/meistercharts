package com.meistercharts.canvas

/**
 * Selects a normal, or small-caps face from a font family
 *
 * [CSS font-variant](https://developer.mozilla.org/de/docs/Web/CSS/font-variant)
 *
 * Attention! small-caps may lead to unforeseen problems! Use with care.
 */
enum class FontVariant {
  /**
   * A normal font variant
   */
  Normal,
  /**
   * Attention! This may lead to unforeseen problems. Use with care
   */
  SmallCaps
}
