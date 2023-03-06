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
package com.meistercharts.api

import com.meistercharts.js.MeisterChartJS

/**
 * The main API for MeisterCharts.
 * Call dispose when the component is no longer used.
 */
@JsExport
abstract class MeisterChartsApiLegacy<DataType, StyleType>
internal constructor(
  /**
   * The meister charts object. Can be used to call markAsDirty and dispose
   */
  meisterChart: MeisterChartJS,

  ) : MeisterChartsApi<Nothing>(meisterChart) {

  /**
   * Sets the data that is displayed
   */
  @JsName("setData")
  abstract fun setData(jsData: DataType)

  /**
   * Sets the style for the component
   */
  @JsName("setStyle")
  abstract fun setStyle(jsStyle: StyleType)

  @Deprecated("Not supported by MeisterChartsApiLegacy. Extend MeisterChartsApi directly", level = DeprecationLevel.ERROR)
  override fun setConfiguration(jsConfiguration: Nothing) {
    throw UnsupportedOperationException("")
  }
}
