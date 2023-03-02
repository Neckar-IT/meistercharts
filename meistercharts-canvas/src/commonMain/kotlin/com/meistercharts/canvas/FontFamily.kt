package com.meistercharts.canvas

/**
 * Represents a font family
 */
data class FontFamily(val family: String) {
  companion object {
    val SansSerif: FontFamily = FontFamily("sans-serif")
    val Serif: FontFamily = FontFamily("serif")
    val Monospace: FontFamily = FontFamily("monospace")
    val FontAwesome: FontFamily = FontFamily("FontAwesome")
  }

  override fun toString(): String {
    return family
  }
}
