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
package com.meistercharts.charts

import com.meistercharts.canvas.MeisterchartBuilder

/**
 * Each implementation contains a set of options/configurations that can be applied to a [com.meistercharts.canvas.MeisterchartBuilder].
 *
 * Each Gestalt combines one or more features (e.g. layers) and offers the API to the end user.
 *
 */
interface ChartGestalt {

  /**
   * Applies this configuration to the given builder
   */
  @ChartGestaltConfiguration
  fun configure(meisterChartBuilder: MeisterchartBuilder)
}

@DslMarker
annotation class ChartGestaltConfiguration
