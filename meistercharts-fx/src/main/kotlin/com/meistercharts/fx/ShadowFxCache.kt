package com.meistercharts.fx

import it.neckar.open.collections.Cache
import it.neckar.open.collections.cache
import com.meistercharts.fx.toJavaFx
import javafx.scene.effect.DropShadow

/**
 * Cache for JavaFX shadow object
 */
object ShadowFxCache {
  /**
   * The tiles cache
   */
  internal val cache: Cache<Int, DropShadow> = cache("ShadowFxCache", 200)


  /**
   * Returns a (cached) shadow object
   */
  fun shadow(blurRadius: Double, offsetX: Double, offsetY: Double, color: com.meistercharts.algorithms.painter.Color): DropShadow {
    val hashCode = (blurRadius.hashCode() + offsetX * 1000 + offsetY * 100000 + color.hashCode()).toInt()

    return cache.getOrStore(hashCode) {
      DropShadow(blurRadius, offsetX, offsetY, color.toJavaFx())
    }
  }
}
