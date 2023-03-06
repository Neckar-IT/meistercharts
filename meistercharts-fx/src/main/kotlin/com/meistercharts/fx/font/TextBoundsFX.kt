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
