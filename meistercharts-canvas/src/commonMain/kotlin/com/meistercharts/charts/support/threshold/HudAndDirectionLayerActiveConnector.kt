package com.meistercharts.charts.support.threshold

import com.meistercharts.algorithms.layers.DirectionalLinesInteractionLayer
import com.meistercharts.algorithms.layers.DirectionalLinesLayer
import com.meistercharts.algorithms.layers.HudElementIndex
import com.meistercharts.algorithms.layers.ValueAxisHudInteractionLayer
import com.meistercharts.algorithms.layers.ValueAxisHudLayer
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.fastForEachIndexed
import it.neckar.open.unit.number.MayBeNegative

/**
 * Connects the active state for a [ValueAxisHudInteractionLayer] and a [DirectionalLinesInteractionLayer]
 */
class HudAndDirectionLayerActiveConnector(
  val directionalLinesInteractionLayer: DirectionalLinesInteractionLayer,
  val hudLayerInteractionLayer: ValueAxisHudInteractionLayer,
) {
  /**
   * The interaction layers this connector uses
   */
  val interactionLayers: Pair<DirectionalLinesInteractionLayer, ValueAxisHudInteractionLayer> = Pair(directionalLinesInteractionLayer, hudLayerInteractionLayer)

  /**
   * Is updated whenever the mouse is moved
   */
  private var mouseOverLineLayerIndex: Int = -1
  private var mouseOverLineIndex: @DirectionalLinesLayer.LineIndex Int = DirectionalLinesLayer.LineIndex.None

  /**
   * Is updated whenever the mouse is moved
   */
  private var mouseOverHudLayerIndex: Int = -1
  private var mouseOverHudElementIndex: @HudElementIndex Int = HudElementIndex.None

  /**
   * Connects the interaction layers to the provided directional lines and hud layers
   */
  fun connectTo(directionalLinesLayers: SizedProvider<DirectionalLinesLayer>, hudLayers: SizedProvider<ValueAxisHudLayer>): HudAndDirectionLayerActiveConnector {
    /**
     * Is called to update both layers
     */
    fun updateState(chartSupport: ChartSupport) {
      var activeLayerIndex = -1
      var activeElementIndex = -1

      if (mouseOverLineLayerIndex >= 0) {
        activeLayerIndex = mouseOverLineLayerIndex
        activeElementIndex = mouseOverLineIndex
      }

      if (mouseOverHudLayerIndex >= 0) {
        activeLayerIndex = mouseOverHudLayerIndex
        activeElementIndex = mouseOverHudElementIndex
      }

      directionalLinesLayers.fastForEachIndexed { layerIndex, directionalLinesLayer ->
        val hudLayer = hudLayers.valueAt(layerIndex)

        if (layerIndex == activeLayerIndex) {
          //This is the active layer!
          directionalLinesLayer.configuration.setActiveLineIndex(activeElementIndex) {
            chartSupport.markAsDirty(DirtyReason.ActiveElementUpdated)
          }
          hudLayer.configuration.setActiveHudElementIndex(activeElementIndex) {
            chartSupport.markAsDirty(DirtyReason.ActiveElementUpdated)
          }
          hudLayer.configuration.zOrderShowIndexOnTop(activeElementIndex)
        } else {
          //*not* active layer, clear if necessary
          directionalLinesLayer.configuration.setActiveLineIndex(-1) {
            chartSupport.markAsDirty(DirtyReason.ActiveElementUpdated)
          }
          hudLayer.configuration.setActiveHudElementIndex(-1) {
            chartSupport.markAsDirty(DirtyReason.ActiveElementUpdated)
          }
          hudLayer.configuration.resetZOrder()
        }
      }
    }

    //Update the actions
    directionalLinesInteractionLayer.configuration.applyActiveLineAction = {
        activeLayerIndex: @MayBeNegative Int,
        activeLineIndex: @DirectionalLinesLayer.LineIndex @MayBeNegative Int,
        chartSupport: ChartSupport,
      ->

      if (activeLayerIndex != mouseOverLineLayerIndex || activeLineIndex != mouseOverLineIndex) {
        mouseOverLineLayerIndex = activeLayerIndex
        mouseOverLineIndex = activeLineIndex

        updateState(chartSupport)
      }
    }

    hudLayerInteractionLayer.configuration.applyActiveElementAction = {
        activeLayerIndex: Int,
        activeElementIndex: @HudElementIndex @MayBeNegative Int,
        chartSupport: ChartSupport,
      ->

      if (activeLayerIndex != mouseOverHudLayerIndex || activeElementIndex != mouseOverHudElementIndex) {
        mouseOverHudLayerIndex = activeLayerIndex
        mouseOverHudElementIndex = activeElementIndex

        updateState(chartSupport)
      }
    }

    return this
  }


  /**
   * Connects the interaction layers to the provided layers
   */
  fun connectTo(directionalLinesLayer: DirectionalLinesLayer, hudLayer: ValueAxisHudLayer): HudAndDirectionLayerActiveConnector {
    return connectTo(SizedProvider.single(directionalLinesLayer), SizedProvider.single(hudLayer))
  }
}
