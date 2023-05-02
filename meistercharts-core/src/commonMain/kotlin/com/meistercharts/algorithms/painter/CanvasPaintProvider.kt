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
package com.meistercharts.algorithms.painter

import it.neckar.open.unit.other.px

/**
 * Can be used in styles - represents flat colors or gradients.
 *
 * Can be a [Color] or a [LinearGradient]
 */
fun interface CanvasPaintProvider {
  /**
   * Converts this [CanvasPaintProvider] to a [CanvasPaint].
   *
   * The given values are relevant when creating a gradient. They are ignored for flat colors
   */
  fun toCanvasPaint(x0: @px Double, y0: @px Double, x1: @px Double, y1: @px Double): CanvasPaint

}

