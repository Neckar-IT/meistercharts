package com.meistercharts.algorithms.impl

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug

/**
 * Prints the calls to the zoom and pan modifier to the logger
 */
class DebugOutputZoomAndTranslationModifier(
  private val delegate: ZoomAndTranslationModifier
) : ZoomAndTranslationModifier by delegate {
  override
  fun modifyTranslation(translation: Distance, calculator: ChartCalculator): Distance {
    return delegate.modifyTranslation(translation, calculator).also {
      logger.debug {
        if (translation == it) {
          "panning not modified ($translation)"
        } else {
          "modifyPanning($translation) -> $it"
        }
      }
    }
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator).also {
      logger.debug {
        if (zoom == it) {
          "zoom not modified ($zoom)"
        } else {
          "modifyZoom($zoom) -> $it"
        }
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.algorithms.impl.DebugOutputZoomAndTranslationModifier")
  }
}


/**
 * Adds a [DebugOutputZoomAndTranslationModifier] wrapper
 */
fun ZoomAndTranslationModifiersBuilder.debugEnabled(): ZoomAndTranslationModifiersBuilder {
  current = DebugOutputZoomAndTranslationModifier(current)
  return this
}
