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
package com.meistercharts.canvas

import it.neckar.open.unit.si.ms

/**
 * Contains information about the repaint
 */
data class PaintingStats(
  /**
   * When the repaint happened
   */
  @ms val frameTimestamp: Double,
  /**
   * The time delta to the last frame
   */
  @ms val frameTimestampDelta: Double,
  /**
   * The durations of the layer repaints
   */
  val layerPaintDurations: LayerPaintDurations
) {
}

/**
 * Contains information about the repaint durations for all layers
 */
data class LayerPaintDurations(
  val paintDurations: List<LayerPaintDuration>
) {

  /**
   * Returns the slowest repaint duration
   */
  val slowest: LayerPaintDuration?
    get() {
      return paintDurations.maxByOrNull { it: LayerPaintDuration -> it.duration }
    }

  /**
   * Returns the total time
   */
  @ms
  val total: Double
    get() {
      return paintDurations
        .sumByDouble { it.duration }
    }
}

/**
 * Contains information about one repaint of one layer
 */
data class LayerPaintDuration(
  /**
   * The key of the layer
   */
  val key: Any,
  /**
   * The description of the layer
   */
  val layerDescription: String,

  /**
   * The repaint duration
   */
  @ms
  val duration: Double
)
