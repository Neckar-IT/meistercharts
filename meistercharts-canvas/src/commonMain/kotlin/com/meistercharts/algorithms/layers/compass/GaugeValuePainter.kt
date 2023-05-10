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
package com.meistercharts.algorithms.layers.compass

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Domain
import it.neckar.open.unit.number.MayBeNegative
import com.meistercharts.annotations.Zoomed
import it.neckar.open.unit.si.rad

/**
 * Paints the current value for a gauge
 *
 */
fun interface GaugeValuePainter {
  /**
   * Paints the current value
   */
  fun paintCurrentValue(
    gaugePaintable: GaugePaintable,
    paintingContext: LayerPaintingContext,
    radius: @Zoomed Double,
    startAngle: @rad Double,
    extendWithRotationDirection: @rad @MayBeNegative Double,
    valueRange: ValueRange,
    /**
     * The current value
     */
    value: @Domain Double
  )

}
