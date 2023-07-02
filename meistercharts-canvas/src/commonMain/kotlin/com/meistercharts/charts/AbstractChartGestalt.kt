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

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.layer.LayerSupport
import com.meistercharts.canvas.MeisterchartBuilder
import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.dispose.OnDispose

/**
 * Abstract base class for chart gestalts.
 *
 * There are two ways to register configurations:
 * *[configureBuilder] - can be used to configure the [MeisterchartBuilder] itself. These actions are applied first
 * *[configure] - can be used to configure that [LayerSupport]. This method is often used in many gestalts.
 */
abstract class AbstractChartGestalt : ChartGestalt, Disposable, OnDispose {
  private var disposeSupport: DisposeSupport = DisposeSupport()

  override fun dispose() {
    disposeSupport.dispose()
  }

  override fun onDispose(action: () -> Unit) {
    disposeSupport.onDispose(action)
  }

  private var configured: Boolean = false

  /**
   * Contains configuration actions
   */
  private val builderConfigurationActions: MutableList<(meisterChartBuilder: MeisterchartBuilder) -> Unit> = mutableListOf()

  /**
   * Contains configurations that are applied
   */
  private val layerSupportConfigurationActions: MutableList<LayerSupport.() -> Unit> = mutableListOf()

  /**
   * Registers a configuration lambda that will later be applied when [configure] is called.
   *
   * In many cases [configure] can be used instead.
   */
  fun configureBuilder(configurationAction: (meisterChartBuilder: MeisterchartBuilder) -> Unit) {
    ensureNotConfigured()
    this.builderConfigurationActions.add(configurationAction)
  }

  /**
   * Registers a configuration lambda for the layer support.
   */
  fun configure(configuration: LayerSupport.() -> Unit) {
    ensureNotConfigured()
    this.layerSupportConfigurationActions.add(configuration)
  }

  /**
   * The chart support that is set when configuring
   */
  private var configuredChartSupport: ChartSupport? = null

  /**
   * Returns the chart support that has been used to configure this gestalt.
   * Attention!
   */
  fun chartSupport(): ChartSupport {
    return configuredChartSupport ?: throw IllegalStateException("ChartSupport not available - gestalt has not yet been configured")
  }

  /**
   * Returns the chart support that has been used to configure this gestalt.
   * Returns null if there is no chart support available.
   */
  fun chartSupportOrNull(): ChartSupport? {
    return configuredChartSupport
  }

  final override fun configure(meisterChartBuilder: MeisterchartBuilder) {
    ensureNotConfigured()

    meisterChartBuilder.onDispose(this)

    //Apply the builder actions first!
    builderConfigurationActions.fastForEach { it(meisterChartBuilder) }

    meisterChartBuilder.configure {
      //Save the chart support
      configuredChartSupport = this.chartSupport

      //Apply the layer support configuration actions
      layerSupportConfigurationActions.fastForEach {
        it.invoke(this)
      }
    }

    configured = true
  }

  /**
   * Throws an exception if this gestalt has already been configured
   */
  private fun ensureNotConfigured() {
    if (configured) {
      throw IllegalStateException("configure has already been called!")
    }
  }
}
