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
package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import it.neckar.open.kotlin.lang.random
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms

/**
 * Delays the execution for some time
 */
class SlowLayer(
  @ms val targetPaintTime: Double,
  /**
   * This value is added or subtracted from the target time to get random values
   */
  @ms val plusMinus: Double = 0.0
) : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val startTime = nowMillis()
    val target = startTime + targetPaintTime + (random.nextDouble() * 2.0 * plusMinus - plusMinus / 2.0)

    @Suppress("ControlFlowWithEmptyBody", "EmptyWhileBlock")
    while (nowMillis() < target) {
    }
  }
}
