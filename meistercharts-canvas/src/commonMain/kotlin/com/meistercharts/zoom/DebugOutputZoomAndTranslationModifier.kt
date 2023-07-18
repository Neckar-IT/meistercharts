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
package com.meistercharts.zoom

import com.meistercharts.calc.ChartCalculator
import com.meistercharts.annotations.Zoomed
import com.meistercharts.geometry.Distance
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
  fun modifyTranslation(translation: @Zoomed Distance, calculator: ChartCalculator): Distance {
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
