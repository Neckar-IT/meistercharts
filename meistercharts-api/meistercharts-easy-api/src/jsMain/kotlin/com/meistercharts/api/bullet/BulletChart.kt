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
package com.meistercharts.api.bullet

import com.meistercharts.api.MeisterChartsApi
import com.meistercharts.charts.bullet.BulletChartGestalt
import com.meistercharts.js.MeisterchartJS

/**
 * The api towards the browser that supports the creation and manipulation of a bullet chart
 */
@JsExport
class BulletChart internal constructor(
  /**
   * The gestalt that is configured
   */
  internal val gestalt: BulletChartGestalt,

  meisterChart: MeisterchartJS,
) : MeisterChartsApi<BulletChartConfiguration>(meisterChart) {

  init {
    gestalt.applyEasyApiDefaults()
  }

  override fun setConfiguration(jsConfiguration: BulletChartConfiguration) {
    gestalt.applyConfiguration(jsConfiguration)
    markAsDirty()
  }
}
