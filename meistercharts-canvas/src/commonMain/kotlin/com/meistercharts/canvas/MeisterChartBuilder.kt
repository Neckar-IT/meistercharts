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

import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.ZoomLevelCalculator
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.impl.ZoomAndTranslationModifiersBuilder
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.debug.ContentViewportDebugLayer
import com.meistercharts.algorithms.layers.debug.ToggleDebuggingModeLayer
import com.meistercharts.algorithms.layers.gesture.ZoomAndTranslationConfiguration
import com.meistercharts.algorithms.layers.gesture.ZoomAndTranslationLayer
import com.meistercharts.algorithms.layers.gesture.addZoomAndTranslation
import com.meistercharts.algorithms.layers.visibleIf
import com.meistercharts.charts.ChartGestaltConfiguration
import com.meistercharts.charts.ChartId
import com.meistercharts.model.Orientation
import it.neckar.open.annotations.JavaFriendly
import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.OnDispose
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A builder that builds [MeisterChart] instances.
 */
@MeisterChartsBuilderDsl
abstract class MeisterChartBuilder(
  val description: String,
  /**
   * The chart ID for the newly generated chart
   */
  val chartId: ChartId = ChartId.next(),
  /**
   * The factory that is used to create platform dependent objects
   */
  val meisterChartsFactory: MeisterChartsFactory = meisterChartsFactory(),
) : OnDispose {

  /**
   * Indicates whether [build] has been called
   */
  private var hasBeenBuilt = false

  /**
   * The zoom and translation modifier that is used
   */
  var zoomAndTranslationModifier: ZoomAndTranslationModifier = ZoomAndTranslationModifier.none

  /**
   * The zoom and translation defaults
   */
  var zoomAndTranslationDefaults: ZoomAndTranslationDefaults = ZoomAndTranslationDefaults.noTranslation

  /**
   * The binding strategy for the window size
   */
  var windowSizeBindingStrategy: WindowSizeBindingStrategy = ImmediateWindowSizeBindingStrategy

  /**
   * Configures the canvas for a time chart that uses tiles and whose time axis is the x-axis.
   * Sets the [windowSizeBindingStrategy] to a delayed strategy - for the y-axis.
   */
  fun configureAsTiledTimeChart() {
    windowSizeBindingStrategy = DelayedWindowSizeBindingStrategy(axisSelection = AxisSelection.Y)
  }

  /**
   * The sizing strategy of the content area.
   */
  var contentAreaSizingStrategy: ContentAreaSizingStrategy = BindContentAreaSize2ContentViewport()

  /**
   * This method configures properties that are useful for time charts
   */
  fun configureAsTimeChart(timeAxisOrientation: Orientation = Orientation.Horizontal) {
    contentAreaSizingStrategy = when (timeAxisOrientation) {
      Orientation.Horizontal -> FixedContentAreaWidth(1000.0)
      else -> throw UnsupportedOperationException("No supported for <$timeAxisOrientation> yet")
    }
  }

  fun zoomAndTranslationModifier(config: ZoomAndTranslationModifiersBuilder.() -> Unit) {
    zoomAndTranslationModifier = ZoomAndTranslationModifiersBuilder().also(config).build()
  }

  /**
   * Sets the zoom and translation defaults.
   */
  fun zoomAndTranslationDefaults(provider: () -> ZoomAndTranslationDefaults) {
    contract {
      callsInPlace(provider, InvocationKind.EXACTLY_ONCE)
    }

    zoomAndTranslationDefaults(provider())
  }

  /**
   * Sets the zoom and translation defaults.
   */
  fun zoomAndTranslationDefaults(zoomAndTranslationDefaults: ZoomAndTranslationDefaults) {
    this.zoomAndTranslationDefaults = zoomAndTranslationDefaults
  }

  /**
   * How the zoom is changed between two consecutive zoom levels
   */
  var zoomChangeFactor: Double = ZoomLevelCalculator.SQRT_2

  /**
   * Whether the [ZoomAndTranslationLayer] should be registered that connects the user interactions
   * with the [com.meistercharts.algorithms.ZoomAndTranslationSupport]
   *
   * If [enableZoomAndTranslation] is set to false, in nearly all cases it is necessary/useful to
   * set the `resizeBehavior` to `ResetToDefaultsOnResize`
   *
   * @see zoomAndTranslationConfiguration
   *
   */
  var enableZoomAndTranslation: Boolean = true

  @Suppress("NOTHING_TO_INLINE")
  inline fun disableZoomAndTranslation() {
    enableZoomAndTranslation = false
  }

  /**
   * Configure how the [ZoomAndTranslationLayer] should handle zoom-related events
   * @see enableZoomAndTranslation to use the default configuration instead
   */
  var zoomAndTranslationConfiguration: ZoomAndTranslationLayer.() -> Unit = ZoomAndTranslationConfiguration()::configure

  /**
   * Configures the zoom and translation configuration.
   *
   * For complete control over the zoom and translation configuration set the [zoomAndTranslationConfiguration] directly
   */
  fun zoomAndTranslationConfiguration(config: ZoomAndTranslationConfiguration.() -> Unit) {
    contract {
      callsInPlace(config, InvocationKind.EXACTLY_ONCE)
    }

    zoomAndTranslationConfiguration = ZoomAndTranslationConfiguration().also(config)::configure
  }

  /**
   * If set to true the [ToggleDebuggingModeLayer] is automatically added
   */
  var addToggleDebuggingModeLayer: Boolean = true

  /**
   * Creates the chart support
   */
  fun createChartSupport(): ChartSupport {
    return createChartSupport(meisterChartsFactory.canvasFactory.createCanvas(CanvasType.Main))
  }

  /**
   * Creates the chart canvas support for the given canvas
   */
  fun createChartSupport(canvas: Canvas): ChartSupport {
    val chartSupport = ChartSupport(canvas, zoomAndTranslationModifier, zoomAndTranslationDefaults, zoomChangeFactor, windowSizeBindingStrategy, chartId)
    val layerSupport = chartSupport.layerSupport

    if (addToggleDebuggingModeLayer) {
      layerSupport.layers.addLayer(ToggleDebuggingModeLayer())
      layerSupport.layers.addLayer(ContentAreaDebugLayer().visibleIf { chartSupport.debug[DebugFeature.ShowContentAreaDebug] })
      layerSupport.layers.addLayer(ContentViewportDebugLayer().visibleIf { chartSupport.debug[DebugFeature.ShowContentViewportDebug] })
    }

    //Register the zoom and translation layer first
    if (enableZoomAndTranslation) {
      layerSupport.layers.addZoomAndTranslation(chartSupport.zoomAndTranslationSupport, zoomAndTranslationConfiguration)
    }

    //register the dispose actions
    disposeActions.fastForEach {
      chartSupport.onDispose(it)
    }

    //Now add all other configurations
    layerSupportConfigurations.forEach {
      layerSupport.it()
    }

    // Bind the resize behavior after(!) iterating through layerSupportConfigurations to ensure that we use the correct resizeBehavior
    contentAreaSizingStrategy.bindResize(chartSupport)
    onDispose(contentAreaSizingStrategy)

    return chartSupport
  }

  /**
   * Configure
   */
  @ChartGestaltConfiguration
  fun configure(configuration: LayerSupport.() -> Unit) {
    check(!hasBeenBuilt) { "do not call configure() after calling build()" }
    layerSupportConfigurations += configuration
  }

  @JavaFriendly
  fun interface ChartCanvasConfigurer {
    fun configure(layerSupport: LayerSupport)
  }

  @JavaFriendly
  fun configure(configurer: ChartCanvasConfigurer) {
    configure(configurer::configure)
  }

  private val layerSupportConfigurations = mutableListOf<LayerSupport.() -> Unit>()

  /**
   * The dispose actions that have been registered at the builder.
   * These will be moved to the build MeisterCharts
   */
  private val disposeActions = mutableListOf<() -> Unit>()

  /**
   * Registers a dispose action that will be called when the build [MeisterChart] object has been be disposed.
   * Is *not* called, when the builder is finished
   */
  override fun onDispose(action: () -> Unit) {
    disposeActions += action
  }

  /**
   * Builds the chart
   */
  open fun build(): MeisterChart {
    //Set first to avoid call to configure during build
    hasBeenBuilt = true

    val chartSupport = createChartSupport()
    return meisterChartsFactory.createChart(chartSupport, description)
  }
}

/**
 * The(!) MeisterChart interface
 */
interface MeisterChart : Disposable, OnDispose {
  val chartSupport: ChartSupport

  /**
   * Contains a description that helps to understand why this MeisterCharts instance has been created and where it is used.
   * Only for debugging purposes.
   */
  val description: String

  val layerSupport: LayerSupport
    get() {
      return chartSupport.layerSupport
    }

  override fun dispose() {
    chartSupport.dispose()
  }

  override fun onDispose(action: () -> Unit) {
    chartSupport.onDispose(action)
  }

  /**
   * Marks the canvas as dirty
   */
  fun markAsDirty() {
    chartSupport.markAsDirty()
  }
}
