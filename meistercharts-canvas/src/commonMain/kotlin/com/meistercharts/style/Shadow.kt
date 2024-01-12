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
package com.meistercharts.style

import com.meistercharts.color.Color
import it.neckar.open.unit.other.px

/**
 * Contains the configuration for a shadow
 */
data class Shadow(
  val color: Color = Color.black,
  val blurRadius: @px Double = 10.0,
  val offsetX: @px Double = 0.0,
  val offsetY: @px Double = 0.0,
) {

  companion object {
    /**
     * Default shadow - without offset
     */
    val Default: Shadow = Shadow()

    val Light: Shadow = Shadow(color = Color.black, blurRadius = 5.0)
    val LightDrop: Shadow = Shadow(color = Color.black, blurRadius = 5.0, offsetX = 1.0, offsetY = 1.0)

    val DropHigh: Shadow = Shadow(color = Color.black, blurRadius = 7.0, offsetX = 2.0, offsetY = 2.0)
    val Drop: Shadow = Shadow(color = Color.black, blurRadius = 5.0, offsetX = 1.0, offsetY = 1.0)
    val DropSmall: Shadow = Shadow(color = Color.black, blurRadius = 3.0, offsetX = 1.0, offsetY = 1.0)
  }
}
