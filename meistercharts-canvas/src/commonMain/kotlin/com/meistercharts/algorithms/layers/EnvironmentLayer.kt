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
package com.meistercharts.algorithms.layers

import com.meistercharts.environment
import com.meistercharts.algorithms.layers.text.LinesProvider
import com.meistercharts.algorithms.layers.text.addText
import it.neckar.geometry.Direction
import com.meistercharts.geometry.DirectionBasedBasePointProvider

/**
 * Adds an [Layer] that displays information about the environment
 *
 */
fun Layers.addEnvironment() {
  val textsProvider: LinesProvider = { _, _ ->
    val lines = mutableListOf<String>()
    lines.add("multiTouchSupported: ${environment.multiTouchSupported}")
    lines.add("devicePixelRatio: ${environment.devicePixelRatio}")
    lines
  }

  addText(textsProvider) {
    anchorDirection = Direction.CenterLeft
    anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
  }
}
