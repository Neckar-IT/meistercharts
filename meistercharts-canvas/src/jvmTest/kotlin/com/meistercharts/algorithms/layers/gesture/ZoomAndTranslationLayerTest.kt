package com.meistercharts.algorithms.layers.gesture

import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.ZoomAndTranslationSupport
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.impl.DefaultChartState
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.ModifierCombination
import org.junit.jupiter.api.Test

/**
 * Tests to use the API
 */
class ZoomAndTranslationLayerTest {
  @Test
  fun testApiHighLevel() {
    val chartState = DefaultChartState()
    val zoomAndTranslationSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    val layer = ZoomAndTranslationLayer(zoomAndTranslationSupport)

    layer.resetToDefaultsOnDoubleClick()
    layer.resetToDefaultsOnDoubleTap()
    layer.translateOnMouseDrag(AxisSelection.Both)
    layer.zoomOnMouseWheel()
  }

  @Test
  fun testApi() {
    val chartState = DefaultChartState()
    val zoomAndTranslationSupport = ZoomAndTranslationSupport(chartState, ZoomAndTranslationModifier.none, ZoomAndTranslationDefaults.noTranslation)

    val layer = ZoomAndTranslationLayer(zoomAndTranslationSupport)

    layer.onDoubleClick {
      zoomAndTranslationSupport.resetToDefaults()
      Consumed
    }


    /**
     * Which axis can be zoomed
     */
    val zoomAxisSelection: AxisSelection = AxisSelection.Both

    /**
     * Which modifier must be set to zoom along both axes
     */
    val zoomXandYModifier: ModifierCombination = ModifierCombination.CtrlShift

    /**
     * Which modifier must be set to zoom along the x-axis
     */
    val zoomXModifier: ModifierCombination = ModifierCombination.Control

    /**
     * Which modifier must be set to zoom along the y-axis
     */
    val zoomYModifier: ModifierCombination = ModifierCombination.Shift


    layer.onMouseWheel { event ->
      val doZoomX = (event.modifierCombination == zoomXModifier || event.modifierCombination == zoomXandYModifier) && (zoomAxisSelection == AxisSelection.X || zoomAxisSelection == AxisSelection.Both)
      val doZoomY = (event.modifierCombination == zoomYModifier || event.modifierCombination == zoomXandYModifier) && (zoomAxisSelection == AxisSelection.Y || zoomAxisSelection == AxisSelection.Both)

      val zoomedAxis = AxisSelection.get(doZoomX, doZoomY)

      if (zoomedAxis == AxisSelection.None) {
        return@onMouseWheel EventConsumption.Ignored
      }
      if (event.delta + 0.0 == 0.0) {
        return@onMouseWheel EventConsumption.Ignored
      }

      zoomAndTranslationSupport.modifyZoom(event.delta < 0, zoomedAxis, event.coordinates)
      Consumed
    }

    layer.onMouseDrag { distance ->
      zoomAndTranslationSupport.translateWindow(AxisSelection.X, distance.x, distance.y)
      Consumed
    }
  }
}
