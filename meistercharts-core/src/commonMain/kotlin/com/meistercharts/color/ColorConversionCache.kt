package com.meistercharts.color

import it.neckar.open.collections.Cache
import it.neckar.open.collections.cache

object ColorConversionCache {
  /**
   * Maps a hex or rgba string to a [RgbaColor]
   */
  val parseHexOrRgbaCache: Cache<String, RgbaColor> = cache("Color.parserCache", 256)
}
