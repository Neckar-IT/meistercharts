package com.meistercharts.algorithms.impl

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Insets

/**
 * Builder for a zoom and pan modifiers
 *
 */
class ZoomAndTranslationModifiersBuilder {
  /**
   * The current modifier that can be wrapped
   */
  var current: ZoomAndTranslationModifier = ZoomAndTranslationModifier.none

  fun disableZoomingAndTranslation(): ZoomAndTranslationModifiersBuilder {
    current = DisableZoomAndTranslationModifier(current)
    return this
  }

  fun disableTranslationX(): ZoomAndTranslationModifiersBuilder {
    current = DisableTranslateXModifier(current)
    return this
  }

  fun disableTranslationY(): ZoomAndTranslationModifiersBuilder {
    current = DisableTranslateYModifier(current)
    return this
  }

  fun disableZoomX(): ZoomAndTranslationModifiersBuilder {
    current = DisableZoomXModifier(current)
    return this
  }

  fun disableZoomY(): ZoomAndTranslationModifiersBuilder {
    current = DisableZoomYModifier(current)
    return this
  }

  fun disableZoom(): ZoomAndTranslationModifiersBuilder {
    current = DisableZoomModifier(current)
    return this
  }

  fun contentAlwaysBarelyVisible(): ZoomAndTranslationModifiersBuilder {
    current = ContentAreaAlwaysBarelyVisibleTranslationModifier(current)
    return this
  }

  fun contentAlwaysCompletelyVisible(
    axisSelection: AxisSelection = AxisSelection.Both,
    boundsProvider: @ContentAreaRelative BoundsProvider = DefaultBoundsProvider,
    /**
     * The margin around the content. Usually the content viewport margin.
     * Should only be overwritten under special circumstances
     */
    marginProvider: (calculator: ChartCalculator) -> @Zoomed Insets = { it.chartState.contentViewportMargin },
  ): ZoomAndTranslationModifiersBuilder {
    current = ContentAreaAlwaysCompletelyVisibleTranslationModifier(axisSelection, marginProvider, boundsProvider, delegate = current)
    return this
  }

  fun onlyPositiveTranslation(): ZoomAndTranslationModifiersBuilder {
    current = OnlyPositiveTranslationModifier(current)
    return this
  }

  fun maxZoom(maxZoomFactorX: Double, maxZoomFactorY: Double): ZoomAndTranslationModifiersBuilder {
    current = MaxZoomModifier(maxZoomFactorX, maxZoomFactorY, current)
    return this
  }

  fun minZoom(minZoomFactorX: Double, minZoomFactorY: Double): ZoomAndTranslationModifiersBuilder {
    current = MinZoomModifier(minZoomFactorX, minZoomFactorY, current)
    return this
  }

  fun build(): ZoomAndTranslationModifier {
    return current
  }
}
