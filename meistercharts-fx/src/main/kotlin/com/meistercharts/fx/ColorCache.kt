package com.meistercharts.fx

import it.neckar.open.annotations.UiThread
import com.meistercharts.algorithms.painter.CanvasPaint
import it.neckar.open.collections.Cache
import it.neckar.open.collections.cache
import javafx.scene.paint.Paint

/**
 * A cache for JavaFX colors
 */
@UiThread
object ColorCache {
  private val content: Cache<CanvasPaint, Paint> = cache("FxColorCache", 100)

  /**
   * Returns the color for the given descriptor
   */
  @UiThread
  fun getPaint(canvasPaint: CanvasPaint): Paint {
    //Do *NOT* use #getOrStore to avoid unnecessary instantiations of the store lambda
    val found = content[canvasPaint]
    if (found != null) {
      return found
    }

    return canvasPaint.toJavaFx().also {
      content.store(canvasPaint, it)
    }
  }
}
