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
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.events.EventConsumption
import it.neckar.events.MouseMoveEvent
import it.neckar.open.kotlin.lang.abs
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.fastForEachIndexed
import it.neckar.open.unit.number.MayBeNegative

/**
 * Handles interactions for a [ValueAxisHudLayer]
 */
class ValueAxisHudInteractionLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  constructor(
    hudLayers: SizedProvider<ValueAxisHudLayer>,
    additionalConfiguration: Configuration.() -> Unit = {},
  ): this(Configuration(hudLayers), additionalConfiguration)

  constructor(hudLayer: ValueAxisHudLayer) : this(SizedProvider.single(hudLayer))

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    //Noop - just interaction
  }

  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      val mouseLocation = event.coordinates

      var activeLayerIndex = -1
      var activeHudElementIndex: @HudElementIndex Int = HudElementIndex.None
      var bestDistance: @Zoomed Double = Double.MAX_VALUE


      if (mouseLocation != null) {
        configuration.hudLayers.fastForEachIndexed { layerIndex, layer ->
          val paintingVariables = layer.paintingVariables()

          //Iterate over *all* hud elements
          paintingVariables.zOrder.fastForEachIndicesTopToBottom { hudElementIndex ->

            //First check if the mouse is within the hud element
            if (paintingVariables.boundingBoxes.contains(hudElementIndex, mouseLocation)) {
              @Zoomed val deltaX = (paintingVariables.coordinatesCache.x(hudElementIndex) - mouseLocation.x).abs()
              @Zoomed val deltaY = (paintingVariables.coordinatesCache.y(hudElementIndex) - mouseLocation.y).abs()

              //either deltaX or deltaY are *always* the same. Therefore, we can just add these values
              val deltaAdded = deltaX + deltaY

              if (deltaAdded < bestDistance) {
                activeLayerIndex = layerIndex
                activeHudElementIndex = hudElementIndex
                bestDistance = deltaAdded
              }
            }
          }
        }
      }

      configuration.applyActiveElementAction(activeLayerIndex, activeHudElementIndex, chartSupport)

      return super.onMove(event, chartSupport)
    }
  }

  @ConfigurationDsl
  class Configuration(var hudLayers: SizedProvider<ValueAxisHudLayer>) {
    /**
     * Is called whenever the active element might have updated.
     * ATTENTION: This method is called for *every* mouse event.
     */
    var applyActiveElementAction: (
      activeLayerIndex: @MayBeNegative Int,
      activeElementIndex: @HudElementIndex @MayBeNegative Int,
      chartSupport: ChartSupport,
    ) -> Unit = { activeLayerIndex: Int, activeLineIndex: Int, chartSupport: ChartSupport ->
      /*
       * Default implementation that updates all layers
       */

      hudLayers.fastForEachIndexed { layerIndex, layer ->
        @HudElementIndex val activeHudElementIndexForLayer = if (layerIndex == activeLayerIndex) {
          activeLineIndex
        } else {
          -1
        }

        layer.configuration.setActiveHudElementIndex(activeHudElementIndexForLayer) {
          if (activeHudElementIndexForLayer >= 0) {
            layer.configuration.zOrderShowIndexOnTop(activeHudElementIndexForLayer)
          } else {
            layer.configuration.resetZOrder()
          }

          chartSupport.markAsDirty(DirtyReason.UiStateChanged)
        }
      }
    }
  }
}

/**
 * Creates a new interactions handler for this [ValueAxisHudLayer]
 */
fun ValueAxisHudLayer.mouseOverInteractions(): ValueAxisHudInteractionLayer {
  return ValueAxisHudInteractionLayer(this)
}

/**
 * Creates a new interactions handler for the provided [ValueAxisHudLayer]s
 */
fun MultipleLayersDelegatingLayer<ValueAxisHudLayer>.mouseOverInteractions(): ValueAxisHudInteractionLayer {
  return delegates.mouseOverInteractions()
}

fun SizedProvider<ValueAxisHudLayer>.mouseOverInteractions(): ValueAxisHudInteractionLayer {
  return ValueAxisHudInteractionLayer(this)
}
