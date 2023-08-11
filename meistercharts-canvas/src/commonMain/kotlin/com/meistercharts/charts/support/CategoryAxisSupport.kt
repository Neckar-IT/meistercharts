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
package com.meistercharts.charts.support

import com.meistercharts.algorithms.layers.AxisTitleLocation
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.layers.MultiValueAxisLayer
import com.meistercharts.algorithms.layers.MultipleLayersDelegatingLayer
import com.meistercharts.algorithms.layers.barchart.CategoryAxisLayer
import com.meistercharts.algorithms.layers.barchart.CategoryLayer
import com.meistercharts.algorithms.layout.EquisizedBoxLayout
import com.meistercharts.model.category.createCategoryLabelsProvider
import com.meistercharts.canvas.layer.LayerSupport
import com.meistercharts.provider.SizedLabelsProvider

/**
 * This class supports multiple category axis.
 * The axes can be accessed using a key
 */
class CategoryAxisSupport<Key>(
  /**
   * Provides the labels - for each category
   */
  labelsProvider: (Key) -> SizedLabelsProvider,

  /**
   * Returns the layout of the segment
   */
  layoutProvider: (Key) -> EquisizedBoxLayout?,

  /**
   * Additional configuration for this support class
   */
  additionalConfiguration: CategoryAxisSupport<Key>.Configuration.() -> Unit = {},
) : AbstractAxisSupport<Key, CategoryAxisLayer>() {

  override fun createAxisLayer(key: Key): CategoryAxisLayer {
    return CategoryAxisLayer(
      labelsProvider = configuration.labelsProvider.invoke(key),
      layoutProvider = {
        configuration.layoutProvider(key)
      }
    ).also { layer ->
      configuration.axisConfiguration(layer.axisConfiguration, key, layer, preferredAxisTitleLocation)
    }
  }

  override fun createTopTitleLayer(key: Key): AxisTopTopTitleLayer {
    return AxisTopTopTitleLayer.forAxis(getAxisLayer(key))
  }

  /**
   * The configuration for the multi layer
   */
  val configuration: Configuration = Configuration(labelsProvider = labelsProvider, layoutProvider = layoutProvider)
    .also(additionalConfiguration)

  inner class Configuration(
    /**
     * Provides the labels - for each category
     */
    var labelsProvider: (Key) -> SizedLabelsProvider,

    /**
     * Returns the layout of the segment
     */
    var layoutProvider: (Key) -> EquisizedBoxLayout?,

    ) {

    /**
     * Configures the axis.
     * Is also called for all (already) existing value axis
     */
    var axisConfiguration: CategoryAxisConfiguration<Key> = { _, _, _ -> }
      set(value) {
        field = value
        //Apply the new configuration to existing
        axisLayersCache.forEach { key, layer ->
          value.invoke(layer.axisConfiguration, key, layer, preferredAxisTitleLocation)
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
    val axisLayerIndex: @Layers.PaintingOrder Int,
    val multiValueAxisLayer: MultiValueAxisLayer,
    val topTitleLayerIndex: @Layers.PaintingOrder Int,
    val multiTopTitleLayer: MultipleLayersDelegatingLayer<AxisTopTopTitleLayer>,
  )

  companion object {
    /**
     * Creates a value axis support for a single value axis
     */
    fun single(
      /**
       * Provides the labels - for each category
       */
      labelsProvider: SizedLabelsProvider,

      /**
       * Returns the layout of the segment
       */
      layoutProvider: () -> EquisizedBoxLayout?,

      /**
       * Additional configuration for this support class
       */
      additionalConfiguration: CategoryAxisSupport<Unit>.Configuration.() -> Unit = {},
    ): CategoryAxisSupport<Unit> {
      return CategoryAxisSupport(
        labelsProvider = { labelsProvider },
        layoutProvider = { layoutProvider() },
        additionalConfiguration = additionalConfiguration,
      )
    }
  }
}

typealias CategoryAxisConfiguration<Key> = CategoryAxisLayer.Configuration.(Key, axis: CategoryAxisLayer, axisTitleLocation: AxisTitleLocation) -> Unit


inline fun CategoryAxisSupport<Unit>.getTopTitleLayer(): AxisTopTopTitleLayer {
  return this.getTopTitleLayer(Unit)
}

inline fun CategoryAxisSupport<Unit>.getAxisLayer(): CategoryAxisLayer {
  return this.getAxisLayer(Unit)
}

inline fun CategoryAxisSupport<Unit>.addLayers(layerSupport: LayerSupport, noinline visibleCondition: (() -> Boolean)? = null) {
  this.addLayers(layerSupport, Unit, visibleCondition)
}

/**
 * Creates a category axis support for this (single) category layer
 */
fun CategoryLayer<*>.createCategoryAxisSupport(
  labelsProvider: SizedLabelsProvider = configuration.modelProvider().createCategoryLabelsProvider(),
  additionalConfiguration: CategoryAxisSupport<Unit>.Configuration.() -> Unit = {},
): CategoryAxisSupport<Unit> {
  return CategoryAxisSupport.single(
    labelsProvider = labelsProvider,
    layoutProvider = { paintingVariables().layout },
    additionalConfiguration = additionalConfiguration,
  )
}


