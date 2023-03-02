package com.meistercharts.canvas.animation

import assertk.*
import assertk.assertions.*
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DefaultLayerSupport
import com.meistercharts.canvas.LayerSupport
import com.meistercharts.canvas.MockCanvas
import it.neckar.open.observable.ObservableDouble
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ChartAnimationTest {
  lateinit var canvas: MockCanvas
  lateinit var chartSupport: ChartSupport
  lateinit var layerSupport: LayerSupport

  @BeforeEach
  internal fun setUp() {
    canvas = MockCanvas()
    chartSupport = ChartSupport(canvas)
    layerSupport = DefaultLayerSupport(chartSupport)
  }

  @Test
  fun testFinished() {
    val chartAnimation = ChartAnimation { frameTimestamp ->
      AnimationState.finishedIf(frameTimestamp > 10_000.0)
    }

    assertThat(chartSupport.refreshListeners).containsNone(chartAnimation)
    chartSupport.onRefresh(chartAnimation)
    assertThat(chartSupport.refreshListeners).contains(chartAnimation)

    //Simulate an event
    chartSupport.refresh(10_000.0)
    assertThat(chartSupport.refreshListeners).contains(chartAnimation)

    chartSupport.refresh(20_000.0)
    assertThat(chartSupport.refreshListenersToRemove).contains(chartAnimation)
    assertThat(chartSupport.refreshListeners).contains(chartAnimation)

    chartSupport.refresh(30_000.0)
    assertThat(chartSupport.refreshListeners).containsNone(chartAnimation)
  }

  @Test
  fun testDispose() {
    val chartAnimation = ChartAnimation { frameTimestamp ->
      AnimationState.finishedIf(frameTimestamp > 10_000.0)
    }

    assertThat(chartSupport.refreshListeners).containsNone(chartAnimation)
    assertThat(chartSupport.refreshListeners).hasSize(1)
    chartSupport.onRefresh(chartAnimation)
    assertThat(chartSupport.refreshListeners).hasSize(2)
    assertThat(chartSupport.refreshListeners).contains(chartAnimation)

    chartSupport.refresh(10_000.0)
    assertThat(chartSupport.refreshListeners).hasSize(2)
    assertThat(chartSupport.refreshListeners).contains(chartAnimation)

    chartAnimation.dispose()

    assertThat(chartSupport.refreshListeners).hasSize(2)
    assertThat(chartSupport.refreshListeners).contains(chartAnimation)

    chartSupport.refresh(20_000.0)

    assertThat(chartSupport.refreshListenersToRemove).contains(chartAnimation)
    chartSupport.refresh(30_000.0)

    assertThat(chartSupport.refreshListeners).hasSize(1)
    assertThat(chartSupport.refreshListeners).containsNone(chartAnimation)
  }

  @Test
  fun testUsageWithProperty() {
    val positionY = ObservableDouble(0.0)

    val tweenDefinition = TweenDefinition(1000.0)
    val tween = tweenDefinition.realize(40_000.0)

    val propertyTween = tween.animate(positionY::value, 7.0)

    propertyTween.update(40_000.0)
    assertThat(positionY.value).isEqualTo(0.0)

    propertyTween.update(41_000.0)
    assertThat(positionY.value).isEqualTo(7.0)

    propertyTween.update(40_500.0)
    assertThat(positionY.value).isEqualTo(3.5)
  }
}
