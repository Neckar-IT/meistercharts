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

/**
 * Contains platform dependent methods / factories.
 *
 * It is suggested to get the current factory using [MeisterChartsFactoryAccess.factory].
 *
 * It is necessary to
 * call `MeisterChartsPlatform.init()` first
 *
 */
interface MeisterChartsFactory {
  /**
   * Creates a new chart instance
   * @param description used for debugging purposes
   */
  fun createChart(chartSupport: ChartSupport, description: String): MeisterChart

  /**
   * The platform dependent canvas factory
   */
  val canvasFactory: CanvasFactory
}

/**
 * Offers a way to receive the chart factory
 */
object MeisterChartsFactoryAccess {
  /**
   * The current factory. Should be set once at startup - specific for each platform
   */
  var factory: MeisterChartsFactory? = null
}

/**
 * Returns an instance for the platform dependent chart factory.
 *
 * It is necessary to
 * call `MeisterChartsPlatform.init()` first
 */
fun meisterChartsFactory(): MeisterChartsFactory {
  return MeisterChartsFactoryAccess.factory ?: throw IllegalStateException("No meisterChartsFactory set - please call MeisterChartsPlatform.init()")
}
