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
package com.meistercharts.charts.config

import com.meistercharts.color.Color
import com.meistercharts.charts.BarChartStackedGestalt

/**
 * Configuration for a horizontal bar
 */
class HorizontalBarConfig {
  fun apply(gestalt: BarChartStackedGestalt) {
    gestalt.style.applyHorizontalConfiguration()
    gestalt.stackedBarsPainter.stackedBarPaintable.style.backgroundColor = Color.gray
  }

}
