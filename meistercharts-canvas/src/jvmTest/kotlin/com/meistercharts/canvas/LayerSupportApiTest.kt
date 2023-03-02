package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.layers.Layer
import it.neckar.open.time.nowMillis
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

/**
 */
class LayerSupportApiTest {
  @Test
  internal fun name() {
    val mockCanvas = MockCanvas()

    val layerSupport = ChartSupport(mockCanvas)

    layerSupport.markAsDirty()
    layerSupport.disabled = true
  }

  @Test
  fun testEnsurePaintNotCalled() {
    val mockLayer = mockk<Layer> {
      every {
        paint(any())
      } throws AssertionError("must not be called")
    }

    val chartSupport = ChartSupport(MockCanvas())
    val layerSupport = chartSupport.layerSupport
    layerSupport.layers.addLayer(mockLayer)

    assertThat(chartSupport.dirtySupport.dirty).isTrue() //initially set to true
    //reset dirty marker
    chartSupport.dirtySupport.clearIsDirty()

    //Not marked as dirty
    assertThat(chartSupport.dirtySupport.dirty).isFalse()
    chartSupport.refresh(nowMillis())

    chartSupport.markAsDirty()
    chartSupport.disabled = true

    //Repaint is disabled
    chartSupport.refresh(nowMillis())
  }
}
