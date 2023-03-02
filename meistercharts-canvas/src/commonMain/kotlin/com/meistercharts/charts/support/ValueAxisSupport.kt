package com.meistercharts.charts.support

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AxisTitleLocation
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.layers.Layers.PaintingOrder
import com.meistercharts.algorithms.layers.MultiValueAxisLayer
import com.meistercharts.algorithms.layers.MultipleLayersDelegatingLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.canvas.LayerSupport
import it.neckar.open.provider.SizedProvider

/**
 * This class supports multiple value axis.
 * The axes can be accessed using a key
 */
class ValueAxisSupport<Key>(
  /**
   * Provides the value ranges for each axis
   */
  valueRangeProvider: (Key) -> ValueRange,
  /**
   * Additional configuration for this support class
   */
  additionalConfiguration: ValueAxisSupport<Key>.Configuration.() -> Unit = {},
) : AbstractAxisSupport<Key, ValueAxisLayer>(), ValueAxisForKeyProvider<Key> {

  override fun createAxisLayer(key: Key): ValueAxisLayer {
    return ValueAxisLayer(ValueAxisLayer.Data { configuration.valueRangeProvider(key) }).also { layer ->
      configuration.valueAxisConfiguration(layer.style, key, layer, preferredAxisTitleLocation)
    }
  }

  override fun createTopTitleLayer(key: Key): AxisTopTopTitleLayer {
    return AxisTopTopTitleLayer.forAxis(getAxisLayer(key))
  }

  /**
   * Adds multiple layers
   */
  fun addMultipleLayers(layers: Layers, keys: Iterable<Key>): AddLayersResult {
    val multiAxisLayer = createMultiValueAxisLayer(keys)

    val multiTopTitleLayer = createMultiTopTitleLayer(keys)

    @PaintingOrder val axisLayerIndex = layers.addLayer(multiAxisLayer)
    @PaintingOrder val topTitleLayerIndex = layers.addLayer(multiTopTitleLayer)

    return AddLayersResult(axisLayerIndex, multiAxisLayer, topTitleLayerIndex, multiTopTitleLayer)
  }

  /**
   * Creates a multi value axis layer
   */
  fun createMultiValueAxisLayer(keys: Iterable<Key>): MultiValueAxisLayer {
    val multiValueAxisLayer = MultiValueAxisLayer(
      valueAxesProvider = SizedProvider.forList(keys.map {
        getAxisLayer(it)
      }),
    )
    return multiValueAxisLayer
  }

  /**
   * The configuration for the multi layer
   */
  val configuration: Configuration = Configuration(valueRangeProvider).also(additionalConfiguration)

  inner class Configuration(
    /**
     * Provides the value ranges for each axis - will only be used when creating a new value axis
     */
    var valueRangeProvider: (Key) -> ValueRange,
  ) {

    /**
     * Configures the axis.
     * Is also called for all (already) existing value axis
     *
     * Attention: The title visibility property is overridden by the support *after* the [valueAxisConfiguration] has been applied.
     * Therefore, changing the title visibility within this configuration has no effect!
     */
    var valueAxisConfiguration: ValueAxisConfiguration<Key> = { _, _, _ -> }
      set(value) {
        field = value
        //Apply the new configuration to existing
        axisLayersCache.forEach { key, layer ->
          value.invoke(layer.style, key, layer, preferredAxisTitleLocation)
        }
      }

    /**
     * Configures the top title layer
     * Is also called for all (already) existing value axis
     */
    var topTitleLayerConfiguration: ValueAxisTopTopTitleLayerConfiguration<Key> = { _, _ -> }
      set(value) {
        field = value
        //Apply the new configuration to existing
        topTitleLayersCache.forEach { key, layer ->
          value.invoke(layer.configuration, key, layer)
        }
      }
  }

  data class AddLayersResult(
    val axisLayerIndex: @PaintingOrder Int,
    val multiValueAxisLayer: MultiValueAxisLayer,
    val topTitleLayerIndex: @PaintingOrder Int,
    val multiTopTitleLayer: MultipleLayersDelegatingLayer<AxisTopTopTitleLayer>,
  )

  companion object {
    /**
     * Creates a value axis support for a single value axis
     */
    fun single(
      valueRangeProvider: () -> ValueRange,
      /**
       * Additional configuration for this support class
       */
      additionalConfiguration: ValueAxisSupport<Unit>.Configuration.() -> Unit = {},
    ): ValueAxisSupport<Unit> {
      return ValueAxisSupport({ valueRangeProvider() }, additionalConfiguration)
    }
  }
}

typealias ValueAxisConfiguration<Key> = ValueAxisLayer.Style.(Key, axis: ValueAxisLayer, axisTitleLocation: AxisTitleLocation) -> Unit


inline fun ValueAxisSupport<Unit>.getTopTitleLayer(): AxisTopTopTitleLayer {
  return this.getTopTitleLayer(Unit)
}

inline fun ValueAxisSupport<Unit>.getValueAxisLayer(): ValueAxisLayer {
  return this.getAxisLayer(Unit)
}

inline fun ValueAxisSupport<Unit>.addLayers(layerSupport: LayerSupport, noinline visibleCondition: (() -> Boolean)? = null) {
  this.addLayers(layerSupport, Unit, visibleCondition)
}
