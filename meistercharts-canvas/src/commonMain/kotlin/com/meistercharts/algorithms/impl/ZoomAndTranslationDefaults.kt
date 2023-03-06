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
package com.meistercharts.algorithms.impl

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Distance
import com.meistercharts.model.Insets
import com.meistercharts.model.Zoom
import com.meistercharts.model.asDistance
import it.neckar.open.unit.other.pct
import kotlin.reflect.KProperty0

/**
 * Provides the default values for zoom and translation when resetting the view (e.g. pressing the "home" button)
 *
 */
interface ZoomAndTranslationDefaults {
  /**
   * Returns the default zoom for the current chart state
   */
  fun defaultZoom(chartCalculator: ChartCalculator): Zoom

  /**
   * Returns the default window translation (for [defaultZoom] zoom)
   */
  fun defaultTranslation(chartCalculator: ChartCalculator): @Zoomed Distance


  companion object {
    /**
     * No translation. Origin is at the top left, zoom is set to 1.0/1.0
     */
    val noTranslation: NoTranslation = NoTranslation

    /**
     * 10% overscan on both axis. ContentArea is centered in the window
     */
    val tenPercentMargin: ZoomAndTranslationDefaults = FittingWithMarginPercentage(0.1, 0.1)

    /**
     * 10% overscan *only* on the Y axis, ContentArea is centered in the window
     */
    val tenPercentYMargin: ZoomAndTranslationDefaults = FittingWithMarginPercentage(0.0, 0.1)
  }
}

/**
 * No translation. Origin of content area is moved to top left of the window
 */
object NoTranslation : ZoomAndTranslationDefaults {
  override fun defaultZoom(chartCalculator: ChartCalculator): Zoom {
    return Zoom.default
  }

  override fun defaultTranslation(chartCalculator: ChartCalculator): @Zoomed Distance {
    return Distance.zero
  }
}

/**
 * Shows the content area with a padding in percent of the content area size
 */
open class FittingWithMarginPercentage(
  /**
   * The padding percentage for the x axis (applied on left and right)
   */
  val marginPercentageX: @pct Double,
  /**
   * The padding percentage for the x axis (applied on top and bottom)
   */
  val marginPercentageY: @pct Double
) : ZoomAndTranslationDefaults {
  init {
    require(marginPercentageX < 1.0) { "marginPercentageX must be smaller than 1.0 but was <${marginPercentageX}" }
    require(marginPercentageY < 1.0) { "marginPercentageY must be smaller than 1.0 but was <${marginPercentageY}" }
  }

  override fun defaultZoom(chartCalculator: ChartCalculator): Zoom {
    return Zoom.of(1.0 - marginPercentageX, 1.0 - marginPercentageY)
  }

  override fun defaultTranslation(chartCalculator: ChartCalculator): @Zoomed Distance {
    return Distance(
      chartCalculator.contentArea2zoomedX(chartCalculator.chartState.contentAreaWidth * marginPercentageX / 2.0),
      chartCalculator.contentArea2zoomedY(chartCalculator.chartState.contentAreaHeight * marginPercentageY / 2.0)
    )
  }
}

/**
 * Returns the smaller factor for both x and y axis
 */
class FittingWithMarginPercentageAspectRatio(
  marginPercentageX: @pct Double,
  marginPercentageY: @pct Double,
) : FittingWithMarginPercentage(marginPercentageX, marginPercentageY) {

  override fun defaultZoom(chartCalculator: ChartCalculator): Zoom {
    return super.defaultZoom(chartCalculator).smallerValueForBoth()
  }
}

/**
 * Shows the content area within the content viewport
 */
object FittingInContentViewport : FittingWithMargin(
  {
    it.chartState.contentViewportMargin
  }
)

/**
 * Shows the content area with a margin.
 *
 * ATTENTION: Use [FittingInContentViewport] instead in most cases!
 */
open class FittingWithMargin constructor(
  /**
   * The margin
   */
  var marginProvider: (chartCalculator: ChartCalculator) -> @Zoomed Insets,
) : ZoomAndTranslationDefaults {

  constructor(margin: @Zoomed Insets = Insets.empty) : this({ margin })

  override fun defaultZoom(chartCalculator: ChartCalculator): Zoom {
    val chartState = chartCalculator.chartState

    if (chartState.hasZeroSize) {
      return Zoom.default
    }

    val windowSize = chartState.windowSize
    val margin = marginProvider(chartCalculator)

    val windowNetWidth = windowSize.width - margin.offsetWidth
    val windowNetHeight = windowSize.height - margin.offsetHeight

    if (windowNetHeight <= 0.0 || windowNetHeight <= 0.0) {
      return Zoom.default
    }

    val contentAreaSize = chartState.contentAreaSize

    return Zoom.of(
      1.0 / contentAreaSize.width * windowNetWidth,
      1.0 / contentAreaSize.height * windowNetHeight
    )
  }

  override fun defaultTranslation(chartCalculator: ChartCalculator): @Zoomed Distance {
    marginProvider(chartCalculator).let { margin ->
      return Distance(
        margin.left,
        margin.top
      )
    }
  }
}

/**
 * Fits with margin - keeps both zoom factors the same
 */
open class FittingWithMarginAspectRatio(
  /**
   * The margin provider
   */
  marginProvider: (chartCalculator: ChartCalculator) -> @Zoomed Insets = { Insets.empty },
) : FittingWithMargin(marginProvider) {
  override fun defaultZoom(chartCalculator: ChartCalculator): Zoom {
    return super.defaultZoom(chartCalculator).smallerValueForBoth()
  }
}

/**
 * Resets the zoom to defaults - moves the
 */
open class Offset(val provider: (chartCalculator: ChartCalculator) -> Distance) : ZoomAndTranslationDefaults {
  override fun defaultZoom(chartCalculator: ChartCalculator): Zoom {
    return Zoom.default
  }

  override fun defaultTranslation(chartCalculator: ChartCalculator): Distance {
    return provider(chartCalculator)
  }
}

/**
 * Moves the top left corner to the content viewport.
 * Does *not* modify the zoom
 */
object OriginToContentViewport : Offset({ chartCalculator ->
  chartCalculator.chartState.contentViewportMargin.topLeft.asDistance()
})

/**
 * Ensures that a domain value is moved to a specific window location.
 * This is useful e.g. to ensure in context with a cross wire
 */
class MoveDomainValueToLocation(
  /**
   * The zoom to be used as default zoom
   */
  val defaultZoomProvider: (chartCalculator: ChartCalculator) -> Zoom = { Zoom.default },
  /**
   * Returns the domain value that is moved to the location returned by [targetLocationProvider]
   */
  val domainRelativeValueProvider: DomainRelativeValueProvider,
  /**
   * Returns the target location the value provided by [domainRelativeValueProvider] is moved to.
   */
  val targetLocationProvider: TargetLocationProvider,
) : ZoomAndTranslationDefaults {
  override fun defaultZoom(chartCalculator: ChartCalculator): Zoom {
    return defaultZoomProvider(chartCalculator)
  }

  override fun defaultTranslation(chartCalculator: ChartCalculator): @Zoomed Distance {
    val chartState = chartCalculator.chartState
    if (chartState.hasZeroSize) {
      return Distance.zero
    }

    //The value that will be moved to the target location
    @DomainRelative val domainRelativeValue = domainRelativeValueProvider(chartCalculator)
    @Zoomed val distanceX = chartCalculator.domainRelative2zoomedX(domainRelativeValue)

    @Window val targetLocation = targetLocationProvider(chartCalculator)

    //Center the y axis
    @Zoomed val translationY = chartCalculator.contentArea2zoomedY(chartCalculator.chartState.contentAreaHeight * (1.0 - chartCalculator.chartState.zoomY) / 2.0)
    return Distance(-distanceX + targetLocation, translationY)
  }
}

/**
 * Provides the target location in window coordinates used in [MoveDomainValueToLocation]
 */
typealias TargetLocationProvider = (chartCalculator: ChartCalculator) -> @Window Double

/**
 * Provides the domain value that is used in [MoveDomainValueToLocation]
 */
typealias DomainRelativeValueProvider = (chartCalculator: ChartCalculator) -> @DomainRelative Double


/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun KProperty0<ZoomAndTranslationDefaults>.delegate(): ZoomAndTranslationDefaults {
  return object : ZoomAndTranslationDefaults {
    override fun defaultZoom(chartCalculator: ChartCalculator): Zoom {
      return get().defaultZoom(chartCalculator)
    }

    override fun defaultTranslation(chartCalculator: ChartCalculator): Distance {
      return get().defaultTranslation(chartCalculator)
    }
  }
}
