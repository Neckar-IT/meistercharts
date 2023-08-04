/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.canvas

import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontFamily
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontStyle
import com.meistercharts.font.FontVariant
import com.meistercharts.font.FontWeight
import com.meistercharts.js.CanvasFontMetricsCalculatorJS.Companion.logger
import it.neckar.geometry.Size
import it.neckar.open.unit.other.px
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.Image


/**
 * Creates an [Image] that wraps an [HTMLImageElement] whose src-attribute is set to [src]
 * @see [createImageElement]
 */
fun createImage(src: String): com.meistercharts.canvas.Image {
  throw UnsupportedOperationException("not implemented yet")
  return Image(createImageElement(src), Size.zero)
}

/**
 * Creates an [HTMLImageElement] whose src-attribute is set to [src]
 * @see [createImage]
 */
fun createImageElement(src: String): HTMLImageElement {
  throw UnsupportedOperationException("not implemented yet")
  return (document.createElement("IMG") as HTMLImageElement).also { it.src = src }
}


/**
 * Makes this [HTMLElement] unselectable by setting the css user-select property to `none`
 */
fun HTMLElement.unselectable() {
  this.style.setProperty("user-select", "none")
  this.style.setProperty("-moz-user-select", "-moz-none")
  this.style.setProperty("-khtml-user-select", "none")
  this.style.setProperty("-webkit-user-select", "none")
  this.style.setProperty("-ms -user-select", "none")
}


/**
 * Disable the border normally painted by the browser if this [HTMLElement] has the focus
 */
fun HTMLElement.noFocusBorder() {
  this.style.setProperty("outline", "none")
}

/**
 * Retrieves the computed font of this element (see [getComputedStyle](https://developer.mozilla.org/en-US/docs/Web/API/Window/getComputedStyle))
 */
fun HTMLElement.font(): FontDescriptorFragment {
  val computedStyle = window.getComputedStyle(this)
  val cssFontSize = computedStyle.fontSize
  val cssFontFamily = computedStyle.fontFamily
  val cssFontWeight = computedStyle.fontWeight
  val cssFontStyle = computedStyle.fontStyle
  val cssFontVariant = computedStyle.fontVariant

  return FontDescriptorFragment(
    size = parseCssFontSize(cssFontSize),
    family = parseCssFontFamily(cssFontFamily),
    weight = parseCssFontWeight(cssFontWeight),
    style = parseCssFontStyle(cssFontStyle),
    variant = parseCssFontVariant(cssFontVariant)
  )
}


/**
 * Parses this string and returns a [FontSize].
 *
 * The string is supposed to be the result of a call like `window.getComputedStyle(element).getPropertyValue("font-size")`
 *
 * @return The font size or `null` if parsing failed
 */
@px
fun parseCssFontSize(fontSize: String): FontSize? {
  try {
    if (fontSize.isBlank()) {
      return null
    }
    // Note: empirically all browsers return a pixel-based font size
    val indexOfPx = fontSize.indexOf("px")
    if (indexOfPx == -1) {
      return null
    }

    return fontSize.substring(0, indexOfPx).trim().toDoubleOrNull()?.let { FontSize(it) }
  } catch (e: Exception) {
    logger.warn("failed to parse font size from <$fontSize>: $e")
  }
  return null
}

fun parseCssFontFamily(fontFamily: String): FontFamily? {
  try {
    val trimmedFontFamily = fontFamily.trim()
    if (trimmedFontFamily.isNullOrBlank()) {
      return null
    }
    return FontFamily(trimmedFontFamily)
  } catch (e: Exception) {
    logger.warn("failed to parse font family from <$fontFamily>: $e")
  }
  return null
}

/**
 * Parses the given CSS font-weight and returns a [FontWeight]
 */
fun parseCssFontWeight(fontWeight: String): FontWeight? {
  try {
    return when (fontWeight) {
      "normal" -> FontWeight.Normal
      "bold" -> FontWeight.Bold
      else -> {
        fontWeight.toIntOrNull()?.let { FontWeight(it) }
      }
    }
  } catch (e: Exception) {
    logger.warn("failed to parse font weight from <$fontWeight>: $e")
  }
  return null
}

/**
 * Parses the given CSS font-style and returns a [FontStyle]
 */
fun parseCssFontStyle(fontStyle: String): FontStyle? {
  try {
    return when (fontStyle) {
      "normal" -> FontStyle.Normal
      "italic" -> FontStyle.Italic
      "oblique" -> FontStyle.Oblique
      else -> null
    }
  } catch (e: Exception) {
    logger.warn("failed to parse font style from <$fontStyle>: $e")
  }
  return null
}

/**
 * Parses the given CSS font-variant and returns a [FontVariant]
 */
fun parseCssFontVariant(fontVariant: String): FontVariant? {
  try {
    return when (fontVariant) {
      "normal" -> FontVariant.Normal
      "small-caps" -> FontVariant.SmallCaps
      else -> null
    }
  } catch (e: Exception) {
    logger.warn("failed to parse font variant from <$fontVariant>: $e")
  }
  return null
}

/**
 * Returns the size of the html canvas element
 */
val HTMLCanvasElement.size: @px Size
  get() {
    return Size(width, height)
  }
