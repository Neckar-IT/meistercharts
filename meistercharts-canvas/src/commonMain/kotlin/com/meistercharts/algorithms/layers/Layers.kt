package com.meistercharts.algorithms.layers

import com.meistercharts.canvas.LayerPaintDuration
import com.meistercharts.canvas.LayerPaintDurations
import com.meistercharts.canvas.PaintingStats
import com.meistercharts.canvas.saved
import com.meistercharts.charts.ChartId
import it.neckar.open.collections.fastForEach
import kotlin.time.DurationUnit
import kotlin.time.measureTime

/**
 * A collection of [Layer]s that paints its elements in a certain order.
 */
class Layers(val chartId: ChartId) {

  /**
   * Annotated values are relevant for painting.
   * Which might be different to the [LayoutOrder]
   */
  @Retention(AnnotationRetention.SOURCE)
  @Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE, AnnotationTarget.LOCAL_VARIABLE)
  annotation class PaintingOrder

  /**
   * Annotated values are relevant for layout.
   * Which might be different to the [PaintingOrder]
   */
  @Retention(AnnotationRetention.SOURCE)
  @Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE, AnnotationTarget.LOCAL_VARIABLE)
  annotation class LayoutOrder

  /**
   * The last element of this collection will be drawn last.
   *
   * First element in this list is painted first, last element is painted last.
   */
  @PaintingOrder
  private val layersListPainting = mutableListOf<Layer>()

  /**
   * Contains the *same* layers as [layersListPainting].
   * But the order might be different (if necessary).
   *
   * This list exists because sometimes the layout order is different from the painting oder.
   *
   * First element in this list is layouted first, last element is layouted last.
   */
  @LayoutOrder
  private val layersListLayout = mutableListOf<Layer>()

  /**
   * The number of [Layer]s found in this [Layers]
   */
  val size: Int
    get() = layersListPainting.size

  /**
   * @return whether this [Layers] is empty or not
   */
  fun isEmpty(): Boolean = layersListPainting.isEmpty()

  /**
   * @return the [Layer]s in painting order
   */
  val layers: @PaintingOrder List<Layer>
    get() = layersListPainting

  /**
   * Returns the layers in the order they process mouse/keyboard events
   */
  val layersOrderedForInteraction: List<Layer>
    get() = layers.reversed()

  /**
   * Returns the first layer of the given type or null
   * Takes delegating layers into consideration
   */
  inline fun <reified T : Layer> byType(): T? {
    layers.fastForEach {
      if (it is T) {
        return it
      }

      if (it is DelegatingLayer<*> && it.delegate is T) {
        return it.delegate
      }
    }

    return null
  }

  /**
   * Adds the given [Layer] to this collection.
   *
   * The layer is placed above the top most layer with the same [LayerType].
   */
  fun addLayer(layer: Layer): @PaintingOrder Int {
    @PaintingOrder val insertionIndex = layersListPainting.indexOfLast {
      it.type.sameOrBelow(layer.type)
    } + 1

    this.layersListPainting.add(insertionIndex, layer)
    this.layersListLayout.add(insertionIndex, layer)
    return insertionIndex
  }

  /**
   * Inserts [layer] at the specified [paintingIndex] and [layoutIndex] (if provided).
   */
  fun addLayerAt(layer: Layer, paintingIndex: @PaintingOrder Int, layoutIndex: @LayoutOrder Int = paintingIndex) {
    layersListPainting.add(paintingIndex, layer)
    layersListLayout.add(layoutIndex, layer)
  }

  /**
   * Adds the layer below the given anchor layer
   */
  fun addLayerBelow(layer: Layer, anchor: Layer) {
    val index = findLayerIndex(anchor)
    require(index >= 0) { "Anchor layer not found" }
    layersListPainting.add(index, layer)
    layersListLayout.add(index, layer)
  }

  fun addLayerAbove(layer: Layer, anchor: Layer) {
    val index = findLayerIndex(anchor)
    require(index >= 0) { "Anchor layer not found" }
    layersListPainting.add(index + 1, layer)
    layersListLayout.add(index + 1, layer)
  }

  /**
   * Returns the index for the given layer
   */
  fun findLayerIndex(anchor: Layer): @PaintingOrder Int {
    return layersListPainting.indexOfFirst {
      it == anchor || it.isDelegatingTo(anchor)
    }
  }

  /**
   * Removes the first occurrence of the given [Layer] from this collection.
   * @return the true if the layer has been removed, false otherwise
   */
  fun removeLayer(layer: Layer): Boolean {
    layersListLayout.remove(layer)
    return this.layersListPainting.remove(layer).also {
      if (it) {
        layer.removed()
      }
    }
  }

  /**
   * Removes all layers that match the given predicate
   */
  fun removeAll(predicate: (Layer) -> Boolean): @PaintingOrder List<Layer> {
    val layersToRemove = this.layersListPainting.filter(predicate)
    this.layersListPainting.removeAll(layersToRemove)
    this.layersListLayout.removeAll(layersToRemove)

    layersToRemove.fastForEach {
      it.removed()
    }

    return layersToRemove
  }

  /**
   * Moves the given [Layer] to the top of this collection
   * @return the new index of the layer
   */
  fun toTop(layer: Layer): @PaintingOrder Int {
    val removed = removeLayer(layer)
    require(removed) { "Layer <$layer> not found" }

    return addLayer(layer)
  }

  /**
   * Moves the given [Layer] to the bottom of this collection
   * (that [Layer] will be drawn first)
   */
  fun toBottom(layer: Layer): @PaintingOrder Int {
    val removed = removeLayer(layer)
    require(removed) { "Layer <$layer> not found" }

    this.layersListPainting.add(0, layer)
    this.layersListLayout.add(0, layer)
    return 0
  }

  /**
   * Adds [newLayer] to this [Layers] before the first layers that is not of type [LayerType.Background].
   */
  fun addAboveBackground(newLayer: Layer): @PaintingOrder Int {
    //Find the last background layer
    val insertionIndex = layers.indexOfLast { it.type == LayerType.Background } + 1
    addLayerAt(newLayer, insertionIndex)
    return insertionIndex
  }

  /**
   * Adds the new layer before the first layer that is not of type [LayerType.Background] and [LayerType.Content]
   */
  fun addAboveContent(newLayer: Layer): @PaintingOrder Int {
    //Find the last background/content layer
    val insertionIndex = layers.indexOfLast { it.type == LayerType.Background || it.type == LayerType.Content } + 1
    addLayerAt(newLayer, insertionIndex)
    return insertionIndex
  }

  /**
   * Paints all layers - records Repaint stats.
   */
  fun paintLayersWithStats(paintingContext: LayerPaintingContext): PaintingStats {
    //Layout first
    for (i in 0 until layersListPainting.size) {
      paintingContext.gc.saved {
        layersListPainting[i].layout(paintingContext)
      }
    }

    val layerRepaintDurations = layersListPainting.map { layer ->
      val measureTime = measureTime {
        paintingContext.gc.saved {
          layer.paint(paintingContext)
        }
      }

      //Calculate the repaint duration for the current layer
      LayerPaintDuration(layer, layer.description, measureTime.toDouble(DurationUnit.MILLISECONDS))
    }

    return PaintingStats(paintingContext.frameTimestamp, paintingContext.frameTimestampDelta, LayerPaintDurations(layerRepaintDurations))
  }

  /**
   * Paints all layers - does not record the paint stats
   */
  fun paintLayers(paintingContext: LayerPaintingContext) {
    //Layout first
    layersListLayout.fastForEach { layerToLayout ->
      paintingContext.gc.saved {
        layerToLayout.layout(paintingContext)

        /**
         * Test if super.layout() is called from all layers
         */
        if (layerToLayout is AbstractLayer) {
          require(layerToLayout.initialized) {
            "Layer has not been initialized. add super.layout(gc) to the layout method for ${layerToLayout::class}"
          }
        }
      }
    }

    //Now paint all layers
    layersListPainting.fastForEach { layerToPaint ->
      paintingContext.gc.saved {
        layerToPaint.paint(paintingContext)
      }
    }
  }
}
