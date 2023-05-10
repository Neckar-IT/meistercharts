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
package com.meistercharts.algorithms.layers

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.events.EventConsumption
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.fastForEachIndexed
import it.neckar.open.unit.number.MayBeNegative

/**
 * Handles mouse events for a single [DirectionalLinesLayer]
 */
class DirectionalLinesInteractionLayer(
  /**
   * The directional layers
   */
  initialDirectionalLinesLayers: SizedProvider<DirectionalLinesLayer>,
  /**
   * The additional configuration that is applied to the configuration object
   */
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  constructor(layer: DirectionalLinesLayer) : this(SizedProvider.single(layer))

  val configuration: Configuration = Configuration(initialDirectionalLinesLayers).also(additionalConfiguration)

  override val type: LayerType = LayerType.Content

  override fun initialize(paintingContext: LayerPaintingContext) {
    super.initialize(paintingContext)

    val layerSupport = paintingContext.layerSupport

    layerSupport.mouseEvents.onMove { mouseMoveEvent ->
      val mousePosition = mouseMoveEvent.coordinates

      var activeLayerIndex = -1
      var activeLineIndex = -1
      var bestDistance: @Zoomed Double = 10.0 //min distance

      //First find the active line - one for all layers
      if (mousePosition != null) {
        //Iterate over all lines layers
        configuration.directionalLinesLayers.fastForEachIndexed { layerIndex, layer ->
          layer.paintingVariables().fastForEach { lineIndex: @DirectionalLinesLayer.LineIndex Int, startX, startY, endX, endY ->
            @Zoomed val distance = mousePosition.distanceToLine(startX, startY, endX, endY)
            if (distance < bestDistance) {
              activeLayerIndex = layerIndex
              activeLineIndex = lineIndex
              bestDistance = distance
            }
          }
        }
      }

      //Apply the active line to *all* layers
      configuration.applyActiveLineAction(activeLayerIndex, activeLineIndex, paintingContext)

      EventConsumption.Ignored
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    //noop
  }

  class Configuration(var directionalLinesLayers: SizedProvider<DirectionalLinesLayer>) {
    /**
     * Is called whenever the active line might have updated.
     * ATTENTION: This method is called for *every* mouse event.
     */
    var applyActiveLineAction: (activeLayerIndex: @MayBeNegative Int, activeLineIndex: @DirectionalLinesLayer.LineIndex @MayBeNegative Int, paintingContext: LayerPaintingContext) -> Unit = { activeLayerIndex: Int, activeLineIndex: Int, paintingContext: LayerPaintingContext ->
      /**
       * Default implementation that updates all directional layers
       */
      directionalLinesLayers.fastForEachIndexed { layerIndex, layer ->
        @DirectionalLinesLayer.LineIndex val activeLineIndexForLayer = if (layerIndex == activeLayerIndex) {
          activeLineIndex
        } else {
          -1
        }

        layer.configuration.setActiveLineIndex(activeLineIndexForLayer) {
          paintingContext.chartSupport.markAsDirty(DirtyReason.UiStateChanged)
        }
      }
    }
  }
}

/**
 * Creates a new interactions handler for this [DirectionalLinesLayer]
 */
fun DirectionalLinesLayer.mouseOverInteractions(): DirectionalLinesInteractionLayer {
  return DirectionalLinesInteractionLayer(this)
}

/**
 * Creates a new interactions handler for the provided [DirectionalLinesLayer]
 */
fun MultipleLayersDelegatingLayer<DirectionalLinesLayer>.mouseOverInteractions(): DirectionalLinesInteractionLayer {
  return DirectionalLinesInteractionLayer(delegates)
}
