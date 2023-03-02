package com.meistercharts.fx.font

import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontStyle
import com.meistercharts.canvas.FontWeight
import it.neckar.open.collections.cache
import javafx.scene.text.Font
import javafx.scene.text.FontPosture

/**
 * Converts FontDescriptors to JavaFX fonts
 */
object FontConversionCacheFX {
  /**
   * Caches the results when converting a font descriptor to a JavaFX font
   */
  private val toFxCache = cache<FontDescriptor, Font>("FxFontConversionCache-toFx", 500)

  /**
   * Reverse map for lookups from currently set font to font descriptor
   */
  private val fromFxCache = cache<Font, FontDescriptor>("FxFontConversionCache-fromFx", 500)

  /**
   * Converts a font descriptor to a JavaFX font
   */
  fun convert(fontDescriptor: FontDescriptor): Font {
    return toFxCache.getOrStore(fontDescriptor) {
      fontDescriptor.convert().also {
        fromFxCache.store(it, fontDescriptor)
      }
    }
  }

  /**
   * Returns the font descriptor for a given font.
   *
   * This methods just does a lookup!
   */
  fun reverse(font: Font): FontDescriptor {
    return fromFxCache[font] ?: throw IllegalArgumentException("No entry available for <$font>")
  }
}

/**
 * Returns the JavaFX font for this font descriptor. Uses a cache
 */
fun FontDescriptor.toFont(): Font {
  return FontConversionCacheFX.convert(this)
}

/**
 * Returns the font descriptor for a given font (Uses a cache)
 */
fun Font.toFontDescriptor(): FontDescriptor {
  return FontConversionCacheFX.reverse(this)
}

/**
 * Converts the font
 */
private fun FontDescriptor.convert(): Font {
  return Font.font(family.family, weight.toFontWeight(), style.toFontPosture(), size.size)
}

private fun FontWeight.toFontWeight(): javafx.scene.text.FontWeight {
  return javafx.scene.text.FontWeight.findByWeight(weight)
}

private fun FontStyle.toFontPosture(): FontPosture {
  return when (this) {
    FontStyle.Normal  -> FontPosture.REGULAR
    FontStyle.Italic  -> FontPosture.ITALIC
    FontStyle.Oblique -> FontPosture.ITALIC
  }
}
