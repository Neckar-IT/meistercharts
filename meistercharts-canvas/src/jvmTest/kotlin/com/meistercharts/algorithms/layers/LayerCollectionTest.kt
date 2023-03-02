package com.meistercharts.algorithms.layers

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.charts.ChartId
import org.junit.jupiter.api.Test


internal class LayerCollectionTest {

  private fun createLayer(): Layer {
    return object : AbstractLayer() {
      override val type: LayerType
        get() = LayerType.Content

      override fun paint(paintingContext: LayerPaintingContext) {
        // do nothing
      }
    }
  }

  @Test
  internal fun testAddLayerWithType() {
    val layerCollection = Layers(ChartId(17))
    assertThat(layerCollection.layers.isEmpty()).isTrue()

    assertThat(layerCollection.addLayer(MyLayer(LayerType.Background))).isEqualTo(0)
    assertThat(layerCollection.addLayer(MyLayer(LayerType.Content))).isEqualTo(1)
    assertThat(layerCollection.addLayer(MyLayer(LayerType.Notification))).isEqualTo(2)

    assertThat(layerCollection.layers).hasSize(3)
    assertThat(layerCollection.addLayer(MyLayer(LayerType.Notification))).isEqualTo(3)
    assertThat(layerCollection.addLayer(MyLayer(LayerType.Background))).isEqualTo(1)
    assertThat(layerCollection.layers).hasSize(5)
    assertThat(layerCollection.addLayer(MyLayer(LayerType.Content))).isEqualTo(3)
  }

  private class MyLayer(
    override val type: LayerType
  ) : AbstractLayer() {
    override fun paint(paintingContext: LayerPaintingContext) {
    }
  }

  @Test
  internal fun testAddAboveBg() {
    assertThat(ClearBackgroundLayer().type).isEqualTo(LayerType.Background)

    val layerCollection = Layers(ChartId(17))
    assertThat(layerCollection.layers.isEmpty()).isTrue()
    assertThat(layerCollection.addAboveBackground(ClearBackgroundLayer())).isEqualTo(0)
    assertThat(layerCollection.addAboveBackground(ClearBackgroundLayer())).isEqualTo(1)
    assertThat(layerCollection.addAboveBackground(ClearBackgroundLayer())).isEqualTo(2)

    layerCollection.addTextUnresolved("asdf").also {
      assertThat(layerCollection.layers).hasSize(4)
      assertThat(layerCollection.layers.indexOf(it)).isEqualTo(3)
    }
  }

  @Test
  fun testAddWithoutKey() {
    val layerCollection = Layers(ChartId(17))
    assertThat(layerCollection.isEmpty()).isTrue()
    assertThat(layerCollection.size).isEqualTo(0)

    val layer1 = createLayer()
    layerCollection.addLayer(layer1)
    assertThat(layerCollection.size).isEqualTo(1)

    val layer2 = createLayer()
    layerCollection.addLayer(layer2)
    assertThat(layerCollection.size).isEqualTo(2)

    layerCollection.addLayer(layer2)
    assertThat(layerCollection.size).isEqualTo(3)
  }

  @Test
  fun testAddWithKey() {
    val layerCollection = Layers(ChartId(17))
    assertThat(layerCollection.isEmpty()).isTrue()
    assertThat(layerCollection.size).isEqualTo(0)

    val layer1 = createLayer()
    layerCollection.addLayer(layer1)
    assertThat(layerCollection.isEmpty()).isFalse()
    assertThat(layerCollection.size).isEqualTo(1)

    val layer2 = createLayer()
    layerCollection.addLayer(layer2)
    assertThat(layerCollection.size).isEqualTo(2)
  }

  @Test
  fun testRemoveWithoutKey() {
    val layerCollection = Layers(ChartId(17))
    assertThat(layerCollection.isEmpty()).isTrue()
    assertThat(layerCollection.size).isEqualTo(0)

    val layer1 = createLayer()
    val layer2 = createLayer()
    layerCollection.addLayer(layer1)
    assertThat(layerCollection.removeLayer(layer1)).isTrue()
    assertThat(layerCollection.removeLayer(layer2)).isFalse()
  }

  @Test
  fun testGetLayers() {
    val layerCollection = Layers(ChartId(17))
    val layer1 = createLayer()
    val layer2 = createLayer()
    val layer3 = createLayer()
    layerCollection.addLayer(layer1)
    layerCollection.addLayer(layer2)
    layerCollection.addLayer(layer3)
    assertThat(layerCollection.size).isEqualTo(3)

    val layers = layerCollection.layers
    assertThat(layers).hasSize(3)
    assertThat(layers).containsExactly(layer1, layer2, layer3)
  }

  @Test
  fun testToTopWithoutKey() {
    val layerCollection = Layers(ChartId(17))
    val layer1 = createLayer()
    val layer2 = createLayer()
    val layer3 = createLayer()
    layerCollection.addLayer(layer1)
    layerCollection.addLayer(layer2)
    layerCollection.addLayer(layer3)
    layerCollection.toTop(layer2)
    assertThat(layerCollection.layers).containsExactly(layer1, layer3, layer2)
  }

  @Test
  fun testToTopWithKey() {
    val layerCollection = Layers(ChartId(17))
    val layer1 = createLayer()
    val layer2 = createLayer()
    val layer3 = createLayer()
    layerCollection.addLayer(layer1)
    layerCollection.addLayer(layer2)
    layerCollection.addLayer(layer3)

    assertThat(layerCollection.layers).containsExactly(layer1, layer2, layer3)
    layerCollection.toTop(layer2)
    assertThat(layerCollection.layers).containsExactly(layer1, layer3, layer2)
  }

  @Test
  fun testToBottomWithoutKey() {
    val layerCollection = Layers(ChartId(17))
    val layer1 = createLayer()
    val layer2 = createLayer()
    val layer3 = createLayer()
    layerCollection.addLayer(layer1)
    layerCollection.addLayer(layer2)
    layerCollection.addLayer(layer3)
    layerCollection.toBottom(layer2)
    assertThat(layerCollection.layers).containsExactly(layer2, layer1, layer3)
  }

  @Test
  fun testToBottomWithKey() {
    val layerCollection = Layers(ChartId(17))
    val layer1 = createLayer()
    val layer2 = createLayer()
    val layer3 = createLayer()
    layerCollection.addLayer(layer1)
    layerCollection.addLayer(layer2)
    layerCollection.addLayer(layer3)
    layerCollection.toBottom(layer2)
    assertThat(layerCollection.layers).containsExactly(layer2, layer1, layer3)
  }

}
