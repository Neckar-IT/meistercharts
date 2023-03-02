package com.meistercharts.js

import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontStyle
import com.meistercharts.canvas.FontVariant
import com.meistercharts.canvas.FontWeight
import it.neckar.open.collections.cache

/**
 * Helper that converts from FontDescriptor to font strings
 */
object FontConversionCacheJS {
  /**
   * Reverse map for lookups from currently set font to font descriptor
   */
  internal val fromHtmlCache = cache<String, FontDescriptor>("HtmlFontConversionCache", 500)

  /**
   * Stores the pair in the cache
   */
  fun store(value: FontDescriptor, htmlFontString: String) {
    fromHtmlCache[htmlFontString] = value
  }

  /**
   * Returns the font descriptor for a given font.
   *
   * This methods just does a lookup!
   */
  fun reverse(htmlFontString: String): FontDescriptor {
    return fromHtmlCache[htmlFontString] ?: throw IllegalArgumentException("No entry available for <$htmlFontString>")
  }
}

/**
 * Returns a string that represents this font descriptor
 */
fun FontDescriptor.convertToHtmlFontString(): String {
  val htmlFontWeight = weight.toHtmlFontWeightString()
  val htmlFontStyle = style.toHtmlFontStyleString()
  val htmlFontVariant = variant.toHtmlFontVariantString()
  val htmlFontSize = "${size.size}px"
  val htmlFontFamily = family.family

  //https://developer.mozilla.org/en-US/docs/Web/CSS/font
  return "$htmlFontStyle $htmlFontVariant $htmlFontWeight $htmlFontSize $htmlFontFamily"
}

private fun FontWeight.toHtmlFontWeightString(): String {
  return weight.toString()
}

private fun FontStyle.toHtmlFontStyleString(): String {
  return when (this) {
    FontStyle.Normal  -> ""
    FontStyle.Italic  -> "italic"
    FontStyle.Oblique -> "oblique"
  }
}

private fun FontVariant.toHtmlFontVariantString(): String {
  return when (this) {
    FontVariant.Normal    -> ""
    FontVariant.SmallCaps -> "small-caps"
  }
}
