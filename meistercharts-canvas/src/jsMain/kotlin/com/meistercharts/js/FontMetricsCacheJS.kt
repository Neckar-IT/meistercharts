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

import com.meistercharts.font.FontDescriptor
import com.meistercharts.font.FontMetrics
import com.meistercharts.font.FontMetricsCache
import it.neckar.open.collections.cache
import com.meistercharts.events.FontLoadedEventBroker

/**
 * Provides exact font metrics (cached) for the HTML canvas.
 *
 * Uses a canvas to render the font and measures the pixels.
 */
object FontMetricsCacheJS : FontMetricsCache {
  /**
   * Returns the current cache size (for debugging purposes)
   */
  val cacheSize: Int
    get() {
      return fontMetricsCache.size
    }

  private val fontMetricsCache = cache<FontDescriptor, FontMetrics>("HtmlCanvasFontMetricsCache", 500)

  /**
   * Is used to calculate the font metrics
   */
  val fontMetricsCalculator: CanvasFontMetricsCalculatorJS = CanvasFontMetricsCalculatorJS()

  init {
    //Clear the cache whenever a font has been loaded.
    FontLoadedEventBroker.onLoaded {
      fontMetricsCache.clear()
    }
  }

  /**
   * Returns the height for the given font family and size
   */
  override fun get(font: FontDescriptor): FontMetrics {
    return fontMetricsCache.getOrStore(font) {
      fontMetricsCalculator.calculateFontMetrics(font)
    }
  }
}
