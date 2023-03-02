package com.meistercharts.fx.font

import it.neckar.open.collections.cache
import com.sun.javafx.tk.FontMetrics
import com.sun.javafx.tk.Toolkit
import javafx.scene.text.Font

/**
 * Cache for the native JavaFX font metrics
 */
object NativeFontMetricsCache {
  private val cache = cache<Font, FontMetrics>("FontMetricsCache", 500)

  operator fun get(font: Font): FontMetrics {
    return cache.getOrStore(font) {
      Toolkit.getToolkit().fontLoader.getFontMetrics(font)
    }
  }
}
