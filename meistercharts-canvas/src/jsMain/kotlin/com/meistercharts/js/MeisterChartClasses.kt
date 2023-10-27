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
package com.meistercharts.js

import com.meistercharts.charts.ChartId

/**
 * Contains the CSS classes used in meistercharts
 */
object MeisterChartClasses {
  const val canvas: String = "meistercharts-canvas"
  const val nativeComponentsHolder: String = "meistercharts-native-components"

  /**
   * Is added if a chart is disposed
   */
  const val disposed: String = "meistercharts-disposed"

  /**
   * Class name for the holder of the chart
   */
  const val holder: String = "meistercharts"

  /**
   * Class name for the script tag that contains the configuration (JSON).
   */
  const val config: String = "meistercharts-config"

  /**
   * Used for image loader elements
   */
  const val imageLoader: String = "meistercharts-image-loader"

  fun chartId(chartId: ChartId): String {
    return "meistercharts-id-${chartId.id}"
  }
}
