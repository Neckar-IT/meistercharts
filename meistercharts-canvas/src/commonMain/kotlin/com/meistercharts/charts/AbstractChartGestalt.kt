package com.meistercharts.charts

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.LayerSupport
import com.meistercharts.canvas.MeisterChartBuilder
import it.neckar.open.collections.fastForEach

/**
 * Abstract base class for chart gestalts.
 *
 * There are two ways to register configurations:
 * *[configureBuilder] - can be used to configure the [MeisterChartBuilder] itself. These actions are applied first
 * *[configure] - can be used to configure that [LayerSupport]. This method is often used in many gestalts.
 */
abstract class AbstractChartGestalt : ChartGestalt {
  private var configured: Boolean = false

  /**
   * Contains configuration actions
   */
  private val builderConfigurationActions: MutableList<(meisterChartBuilder: MeisterChartBuilder) -> Unit> = mutableListOf()

  /**
   * Contains configurations that are applied
   */
  private val layerSupportConfigurationActions: MutableList<LayerSupport.() -> Unit> = mutableListOf()

  /**
   * Registers a configuration lambda that will later be applied when [configure] is called.
   *
   * In many cases [configure] can be used instead.
   */
  fun configureBuilder(configurationAction: (meisterChartBuilder: MeisterChartBuilder) -> Unit) {
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

  final override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    ensureNotConfigured()

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
