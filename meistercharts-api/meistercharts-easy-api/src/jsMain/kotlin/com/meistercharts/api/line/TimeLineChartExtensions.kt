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
package com.meistercharts.api.line

import com.meistercharts.algorithms.layers.ValueAxisHudLayer
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.history.DecimalDataSeriesIndex

/**
 * Applies the default configuration for SICK
 */
fun TimeLineChartGestalt.applySickDefaults() {
  this.style.applyValueAxisTitleOnTop()
  thresholdsSupport.configuration.hudLayerConfiguration = { _: DecimalDataSeriesIndex, _: ValueAxisHudLayer ->
  }
}