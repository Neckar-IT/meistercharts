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
