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
package com.meistercharts.js

import com.meistercharts.canvas.FontDescriptor
import it.neckar.open.collections.cache
import com.meistercharts.events.FontLoadedEventBroker
import it.neckar.open.unit.other.px

/**
 * Caches the results of a html canvas font width/bounds results
 */
object FontBoundsCacheJS {
  /**
   * Holds cached values for the width
   */
  private val widthCache = cache<Int, Double>("HtmlFontBoundsCache-width", 500)

  init {
    //Clear the cache whenever a font has been loaded.
    FontLoadedEventBroker.onLoaded {
      widthCache.clear()
    }
  }

  /**
   * Calculates the width of the string [text] with the given font [font] when painted in a [Text]
   */
  @px
  fun calculateWidth(text: String, font: FontDescriptor, calculate: () -> @px Double): @px Double {
    val key = 31 * text.hashCode() + 17 * font.hashCode()
    return widthCache.getOrStore(key, calculate)
  }
}
