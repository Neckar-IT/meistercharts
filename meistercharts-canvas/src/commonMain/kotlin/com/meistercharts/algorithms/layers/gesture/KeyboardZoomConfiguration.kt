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
package com.meistercharts.algorithms.layers.gesture

import it.neckar.events.KeyCode
import it.neckar.events.KeyStroke

/**
 * The configuration for keyboard zooming
 */
data class KeyboardZoomConfiguration(
  /**
   * The key stroke to zoom in
   */
  val zoomInKeys: List<KeyStroke> = listOf(KeyStroke(KeyCode.Plus), KeyStroke(KeyCode.PlusNumPad)),
  /**
   * The key stroke to zoom out
   */
  val zoomOutKeys: List<KeyStroke> = listOf(KeyStroke(KeyCode.Minus), KeyStroke(KeyCode.MinusNumPad)),

  /**
   * The key stroke that resets the zoom to the default
   */
  val resetZoomStrokes: List<KeyStroke> = listOf(KeyStroke.ctrl(KeyCode('0'))),
)
