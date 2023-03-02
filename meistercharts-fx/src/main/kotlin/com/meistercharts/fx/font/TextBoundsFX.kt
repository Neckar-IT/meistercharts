package com.meistercharts.fx.font

import it.neckar.open.collections.cache
import it.neckar.open.unit.other.px
import javafx.geometry.Bounds
import javafx.scene.text.Font
import javafx.scene.text.Text

/**
 * Helper that calculates text bounds
 *
 */
object TextBoundsFX {
  private val TEXT = Text()

  /**
   * Holds cached values for the width
   */
  private val widthCache = cache<Int, Double>("FxTextBounds-width", 500)

  /**
   * Calculates the width of the string [text] with the given font [font] when painted in a [Text]
   */
  @px
  @JvmStatic
  fun calculateWidth(text: String, font: Font): Double {
    val key = 31 * text.hashCode() + 17 * font.hashCode()

    return widthCache.getOrStore(key) {
      TEXT.text = text
      TEXT.font = font
      TEXT.layoutBounds.width
    }
  }

  @px
  @JvmStatic
  fun calculateBounds(text: String, font: Font): Bounds {
    TEXT.text = text
    TEXT.font = font
    return TEXT.layoutBounds
  }
}
