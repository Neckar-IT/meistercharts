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
