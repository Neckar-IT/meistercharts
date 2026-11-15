/*
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

import com.sun.javafx.tk.FontMetrics
import com.sun.javafx.tk.Toolkit
import it.neckar.open.collections.cache
import javafx.scene.text.Font

/**
 * Cache for the native JavaFX font metrics
 */
@Deprecated("Does not work in JavaFX 20+")
object NativeFontMetricsCache {
  private val cache = cache<Font, FontMetrics>("FontMetricsCache", 500)

  operator fun get(font: Font): FontMetrics {
    return cache.getOrStore(font) {
      loadFontMetrics(font)
    }
  }

  /**
   * Loads the font metrics for the provided font
   */
  fun loadFontMetrics(font: Font): FontMetrics {
    return Toolkit.getToolkit().fontLoader.getFontMetrics(font)
  }
}
