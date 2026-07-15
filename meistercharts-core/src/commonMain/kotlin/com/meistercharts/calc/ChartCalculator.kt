/*
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
@file:Suppress("NOTHING_TO_INLINE")

package com.meistercharts.calc

import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Zoom
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.ValueRange
import com.meistercharts.state.ChartState
import com.meistercharts.state.contentViewportHeight
import com.meistercharts.state.contentViewportWidth
import com.meistercharts.state.withAdditionalTranslation
import com.meistercharts.state.withAxisOrientation
import com.meistercharts.state.withContentAreaSize
import com.meistercharts.state.withTranslation
import com.meistercharts.state.withWindowSize
import com.meistercharts.state.withZoom
import com.meistercharts.tile.TileIndex
import com.meistercharts.time.TimeRange
import it.neckar.geometry.AxisOrientationX
import it.neckar.geometry.AxisOrientationY
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Distance
import it.neckar.geometry.Size
import it.neckar.open.annotations.Allocates
import it.neckar.open.annotations.AllocationCost
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import it.neckar.open.unit.quantity.Time
import it.neckar.open.unit.si.ms

/**
 * `ChartCalculator` is responsible for converting values between various coordinate systems
 * used in chart rendering. These systems include abstract "Domain" values, the pixel-based
 * "ContentArea" where the chart is drawn, zoom levels, and the visible "Window" area.
 *
 * It handles the necessary transformations for zoom, translation, and orientation, allowing
 * chart components to map between these different spaces.
 *
 * The following coordinate systems are used:
 *
 * 1. **Domain**: The abstract range of values being plotted (e.g., time, temperature).
 * 2. **DomainRelative**: The normalized position (0.0 to 1.0) within the Domain range.
 * 3. **ContentArea**: The pixel-based area where the chart content is drawn.
 * 4. **ContentAreaRelative**: The relative position (0.0 to 1.0) within the ContentArea.
 * 5. **Zoomed**: Values in the ContentArea that have been scaled by zoom factors.
 * 6. **Window**: The visible portion of the chart that is displayed on screen.
 *
 * # Main Responsibilities:
 *
 * - Convert between `Domain`, `ContentArea`, `Zoomed`, and `Window` values.
 * - Support for transformations involving zoom levels and translations (panning).
 * - Apply axis orientation to handle inverted axes.
 * - Handle viewport restrictions, ensuring values are constrained to the visible window area.
 *
 * # Example transformations:
 *
 * - `domain2windowX(domainX, valueRange)`: Convert a domain X value to a window X coordinate.
 * - `window2domainX(windowX, valueRange)`: Convert a window X coordinate back to the domain X value.
 * - `domainRelative2zoomedX(domainRelativeX)`: Convert a relative domain X value to a zoomed X coordinate.
 *
 * This class also provides convenience methods to convert areas and distances between coordinate systems.
 *
 * @noinspection ClassWithTooManyMethods
 */
open class ChartCalculator(val chartState: ChartState) {

  /**
   * Creates a new `ChartCalculator` instance with an overridden content area size.
   */
  fun withContentAreaSize(sizeOverride: @ContentArea Size): ChartCalculator {
    return withChartState(chartState.withContentAreaSize(sizeOverride))
  }

  /**
   * Creates a new `ChartCalculator` instance with an overridden window size.
   */
  fun withWindowSize(sizeOverride: @Zoomed Size): ChartCalculator {
    return withChartState(chartState.withWindowSize(sizeOverride))
  }

  /**
   * Creates a new `ChartCalculator` instance with an overridden translation.
   */
  fun withTranslation(translationOverride: @Zoomed Distance): ChartCalculator {
    return withChartState(chartState.withTranslation(translationOverride))
  }

  /**
   * Creates a new `ChartCalculator` instance with an overridden zoom factor.
   */
  fun withZoom(zoomOverride: Zoom): ChartCalculator {
    return withChartState(chartState.withZoom(zoomOverride))
  }

  /**
   * Creates a new `ChartCalculator` instance with additional translation applied on top of
   * the existing translation.
   */
  fun withAdditionalTranslation(additionalTranslation: @Zoomed Distance): ChartCalculator {
    return withChartState(chartState.withAdditionalTranslation(additionalTranslation))
  }

  /**
   * Creates a new `ChartCalculator` instance with overridden axis orientations.
   */
  fun withAxisOrientation(
    axisOrientationXOverride: AxisOrientationX?,
    axisOrientationYOverride: AxisOrientationY?,
  ): ChartCalculator {
    return withChartState(chartState.withAxisOrientation(axisOrientationXOverride, axisOrientationYOverride))
  }

  /**
   * Creates a new `ChartCalculator` instance with a new chart state.
   */
  fun withChartState(newChartState: ChartState): ChartCalculator {
    return ChartCalculator(newChartState)
  }

  // Conversion Methods

  /**
   * Converts a domain-relative X value to a content-area-relative X value.
   */
  fun domainRelative2contentAreaRelativeX(
    /**
     * the X value relative to the domain, where 0.0 is the start and 1.0 is the end.
     */
    @DomainRelative @pct x: Double
  ): @ContentAreaRelative Double {
    return InternalCalculations.domainRelative2contentAreaRelative(x, chartState.axisOrientationX)
  }

  /**
   * Converts a domain-relative Y value to a content-area-relative Y value.
   */
  fun domainRelative2contentAreaRelativeY(
    /**
     * the Y value relative to the domain, where 0.0 is the start and 1.0 is the end.
     */
    @DomainRelative @pct y: Double
  ): @ContentAreaRelative Double {
    return InternalCalculations.domainRelative2contentAreaRelative(y, chartState.axisOrientationY)
  }

  /**
   * Converts domain-relative coordinates to content-area-relative coordinates.
   */
  fun domainRelative2contentAreaRelative(
    /**
     * the coordinates relative to the domain, where 0.0 is the start and 1.0 is the end.
     */
    @DomainRelative @pct coordinates: Coordinates
  ): @ContentAreaRelative Coordinates {
    return Coordinates.of(
      domainRelative2contentAreaRelativeX(coordinates.x),
      domainRelative2contentAreaRelativeY(coordinates.y),
    )
  }

  @Allocates(AllocationCost.Constant)
  fun domainRelative2contentAreaRelative(@DomainRelative @pct size: Size): @ContentAreaRelative Size {
    return Size.of(
      domainRelative2contentAreaRelativeX(size.width),
      domainRelative2contentAreaRelativeY(size.height)
    )
  }

  /**
   * Converts a content-area-relative X value to a pixel value in the content area.
   */
  open fun contentAreaRelative2contentAreaX(
    /**
     * the X value relative to the content area, where 0.0 is the start and 1.0 is the end.
     */
    @ContentAreaRelative @pct x: Double
  ): @ContentArea Double {
    return InternalCalculations.contentAreaRelative2contentArea(x, chartState.contentAreaWidth)
  }

  /**
   * Converts a content-area-relative Y value to a pixel value in the content area.
   */
  open fun contentAreaRelative2contentAreaY(
    /**
     * the Y value relative to the content area, where 0.0 is the start and 1.0 is the end.
     */
    @ContentAreaRelative @pct y: Double
  ): @ContentArea Double {
    return InternalCalculations.contentAreaRelative2contentArea(y, chartState.contentAreaHeight)
  }

  /**
   * Converts content-area-relative coordinates to content-area pixel coordinates.
   */
  @Allocates(AllocationCost.Constant)
  fun contentAreaRelative2contentArea(@ContentAreaRelative @pct coordinates: Coordinates): @ContentArea Coordinates {
    return Coordinates.of(
      contentAreaRelative2contentAreaX(coordinates.x),
      contentAreaRelative2contentAreaY(coordinates.y)
    )
  }

  /**
   * Converts a content-area-relative size to content-area pixel size.
   */
  @Allocates(AllocationCost.Constant)
  fun contentAreaRelative2contentArea(@ContentAreaRelative @pct size: Size): @ContentArea Size {
    return Size.of(
      contentAreaRelative2contentAreaX(size.width),
      contentAreaRelative2contentAreaY(size.height)
    )
  }

  // ContentArea --> Zoomed

  /**
   * Converts a content-area X value to a zoomed X value, applying the current zoom factor.
   */
  fun contentArea2zoomedX(@ContentArea @px x: Double): @Zoomed Double {
    return InternalCalculations.contentArea2zoomed(x, chartState.zoomX)
  }

  /**
   * Converts a content-area Y value to a zoomed Y value, applying the current zoom factor.
   */
  fun contentArea2zoomedY(@ContentArea @px y: Double): @Zoomed Double {
    return InternalCalculations.contentArea2zoomed(y, chartState.zoomY)
  }

  /**
   * Converts a content-area size to a zoomed size, applying the current zoom factors.
   */
  @Allocates(AllocationCost.Constant)
  fun contentArea2zoomed(@ContentArea @px size: Size): @Zoomed Size {
    return Size.of(
      contentArea2zoomedX(size.width),
      contentArea2zoomedY(size.height)
    )
  }

  /**
   * Converts content-area coordinates to zoomed coordinates, applying the current zoom factors.
   */
  @Allocates(AllocationCost.Constant)
  fun contentArea2zoomed(@ContentArea @px coordinates: Coordinates): @Zoomed Coordinates {
    return Coordinates.of(
      contentArea2zoomedX(coordinates.x),
      contentArea2zoomedY(coordinates.y)
    )
  }

  // Zoomed --> Window

  /**
   * Converts a zoomed X value to a window X value.
   */
  fun zoomed2windowX(@Zoomed @px x: Double): @Window Double {
    return InternalCalculations.zoomed2window(x, chartState.windowTranslation.x)
  }

  /**
   * Converts a zoomed Y value to a window Y value.
   */
  fun zoomed2windowY(@Zoomed @px y: Double): @Window Double {
    return InternalCalculations.zoomed2window(y, chartState.windowTranslation.y)
  }

  /**
   * Converts zoomed coordinates to window coordinates.
   */
  @Allocates(AllocationCost.Constant)
  fun zoomed2window(@Zoomed @px coordinates: Coordinates): @Window Coordinates {
    return Coordinates.of(
      zoomed2windowX(coordinates.x),
      zoomed2windowY(coordinates.y)
    )
  }

  /**
   * Converts a zoomed size to window coordinates.
   */
  @Allocates(AllocationCost.Constant)
  fun zoomed2window(@Zoomed @px size: Size): @Window Coordinates {
    return Coordinates.of(
      zoomed2windowX(size.width),
      zoomed2windowY(size.height)
    )
  }

  //
  // Convenience calculations that directly convert some values
  //

  // DomainRelative -> (ContentAreaRelative --> ContentArea) --> Zoomed

  /**
   * Converts a domain-relative X value directly to a zoomed X value.
   */
  fun domainRelative2zoomedX(@DomainRelative @pct x: Double): @Zoomed Double {
    return contentAreaRelative2zoomedX(domainRelative2contentAreaRelativeX(x))
  }

  /**
   * Converts a domain-relative Y value directly to a zoomed Y value.
   */
  fun domainRelative2zoomedY(@DomainRelative @pct y: Double): @Zoomed Double {
    return contentAreaRelative2zoomedY(domainRelative2contentAreaRelativeY(y))
  }

  /**
   * Converts a domain-relative size directly to a zoomed size.
   */
  @Allocates(AllocationCost.Constant)
  fun domainRelative2zoomed(@DomainRelative @pct size: Size): @Zoomed Size {
    return Size.of(
      domainRelative2zoomedX(size.width),
      domainRelative2zoomedY(size.height)
    )
  }

  //
  // Convenience calculations that directly convert some values
  //

  // Domain -> (DomainRelative -> ContentAreaRelative --> ContentArea) --> Zoomed

  /**
   * Converts a domain X value directly to a zoomed X value.
   */
  fun domain2zoomedX(@Domain x: Double, valueRange: ValueRange): @Zoomed Double {
    @ContentArea val contentAreaX = domainRelative2contentAreaX(valueRange.toDomainRelative(x))
    return contentArea2zoomedX(contentAreaX)
  }

  /**
   * Converts a domain Y value directly to a zoomed Y value.
   */
  fun domain2zoomedY(@Domain y: Double, valueRange: ValueRange): @Zoomed Double {
    @ContentArea val contentAreaY = domainRelative2contentAreaY(valueRange.toDomainRelative(y))
    return contentArea2zoomedY(contentAreaY)
  }

  /**
   * Converts a domain X delta (distance) to a zoomed X delta.
   */
  fun domainDelta2zoomedX(@Domain deltaX: Double, valueRange: LinearValueRange): @Zoomed Double {
    @DomainRelative val delta = valueRange.deltaToDomainRelative(deltaX)

    return domainRelativeDelta2ZoomedX(delta)
  }

  /**
   * Converts a domain-relative X delta to a zoomed X delta.
   */
  fun domainRelativeDelta2ZoomedX(delta: @DomainRelative Double): @Zoomed Double {
    @Zoomed val base = contentArea2zoomedX(domainRelative2contentAreaX(0.0))
    @Zoomed val deltaZoomed = contentArea2zoomedX(domainRelative2contentAreaX(delta))

    return deltaZoomed - base
  }

  /**
   * Converts a domain Y delta (distance) to a zoomed Y delta.
   */
  fun domainDelta2zoomedY(@Domain deltaY: Double, valueRange: LinearValueRange): @Zoomed Double {
    @DomainRelative val delta = valueRange.deltaToDomainRelative(deltaY)

    return domainRelativeDelta2ZoomedY(delta)
  }

  /**
   * Converts a domain-relative Y delta to a zoomed Y delta.
   */
  fun domainRelativeDelta2ZoomedY(delta: @DomainRelative Double): @Zoomed Double {
    @Zoomed val base = contentArea2zoomedY(domainRelative2contentAreaY(0.0))
    @Zoomed val deltaZoomed = contentArea2zoomedY(domainRelative2contentAreaY(delta))

    return deltaZoomed - base
  }

  /**
   * Converts a domain size to a zoomed size.
   */
  @Allocates(AllocationCost.Constant)
  fun domain2zoomed(@Domain size: Size, valueRangeX: ValueRange, valueRangeY: ValueRange): @Zoomed Size {
    return Size.of(
      domain2zoomedX(size.width, valueRangeX),
      domain2zoomedY(size.height, valueRangeY)
    )
  }


  // DomainRelative -> (ContentAreaRelative) --> ContentArea

  fun domainRelative2contentAreaX(@DomainRelative @pct x: Double): @ContentArea Double {
    return contentAreaRelative2contentAreaX(domainRelative2contentAreaRelativeX(x))
  }

  fun domainRelative2contentAreaY(@DomainRelative @pct y: Double): @ContentArea Double {
    return contentAreaRelative2contentAreaY(domainRelative2contentAreaRelativeY(y))
  }

  @Allocates(AllocationCost.Constant)
  fun domainRelative2contentArea(@DomainRelative @pct coordinates: Coordinates): @ContentArea Coordinates {
    return Coordinates.of(
      domainRelative2contentAreaX(coordinates.x),
      domainRelative2contentAreaY(coordinates.y)
    )
  }

  // ContentAreaRelative --> (ContentArea) --> Zoomed

  fun contentAreaRelative2zoomedX(@ContentAreaRelative @pct x: Double): @Zoomed Double {
    return contentArea2zoomedX(contentAreaRelative2contentAreaX(x))
  }

  fun contentAreaRelative2zoomedY(@ContentAreaRelative @pct y: Double): @Zoomed Double {
    return contentArea2zoomedY(contentAreaRelative2contentAreaY(y))
  }

  @Allocates(AllocationCost.Constant)
  fun contentAreaRelative2zoomed(@ContentAreaRelative @pct width: Double, @ContentAreaRelative @pct height: Double): @Zoomed Size {
    return Size.of(
      contentAreaRelative2zoomedX(width),
      contentAreaRelative2zoomedY(height)
    )
  }

  // ContentArea --> (Zoomed) --> Window

  fun contentArea2windowX(@ContentArea x: Double): @Window Double {
    return zoomed2windowX(contentArea2zoomedX(x))
  }

  fun contentArea2windowY(@ContentArea y: Double): @Window Double {
    return zoomed2windowY(contentArea2zoomedY(y))
  }

  @Allocates(AllocationCost.Constant)
  fun contentArea2window(@ContentArea coordinates: Coordinates): @Window Coordinates {
    return Coordinates.of(
      contentArea2windowX(coordinates.x),
      contentArea2windowY(coordinates.y)
    )
  }

  /**
   * Converts a delta in content area X coordinates to window X coordinates.
   */
  fun contentAreaDelta2windowX(@ContentArea deltaX: Double): @Window Double {
    val zoomedDeltaX = contentArea2zoomedX(deltaX)
    return zoomed2windowX(zoomedDeltaX)
  }

  /**
   * Converts a delta in window X coordinates to content area X coordinates.
   */
  fun windowDelta2contentAreaX(@Window deltaX: Double): @ContentArea Double {
    val zoomedDeltaX = window2zoomedX(deltaX)
    return zoomed2contentAreaX(zoomedDeltaX)
  }

  // ContentAreaRelative --> (ContentArea --> Zoomed) --> Window

  fun contentAreaRelative2windowX(@ContentAreaRelative @pct x: Double): @Window @px Double {
    @ContentArea val zoomedX = contentAreaRelative2zoomedX(x)
    return zoomed2windowX(zoomedX)
  }

  /**
   * Returns the window value for the given content area *within* the content viewport
   */
  fun contentAreaRelative2windowXInViewport(@ContentAreaRelative @pct x: Double): @Window @px Double {
    return contentAreaRelative2windowX(x)
      .coerceIn(contentViewportMinX(), contentViewportMaxX())
  }

  fun contentAreaRelative2windowY(@ContentAreaRelative @pct y: Double): @Window @px Double {
    @ContentArea val zoomedY = contentAreaRelative2zoomedY(y)
    return zoomed2windowY(zoomedY)
  }

  /**
   * Returns the window value for the given content area *within* the content viewport
   */
  fun contentAreaRelative2windowYInViewport(@ContentAreaRelative @pct y: Double): @Window @px Double {
    return contentAreaRelative2windowY(y)
      .coerceIn(contentViewportMinY(), contentViewportMaxY())
  }

  @Allocates(AllocationCost.Constant)
  fun contentAreaRelative2window(@ContentAreaRelative @pct x: Double, @ContentAreaRelative @pct y: Double): @Window @px Coordinates {
    return Coordinates.of(
      contentAreaRelative2windowX(x),
      contentAreaRelative2windowY(y)
    )
  }

  // DomainRelative --> (ContentAreaRelative --> ContentArea --> Zoomed) --> Window

  /**
   * Converts a domain-relative X value directly to a window X value.
   */
  fun domainRelative2windowX(
    /**
     * the X value relative to the domain, where 0.0 is the start and 1.0 is the end.
     */
    @DomainRelative @pct x: Double
  ): @Window @px Double {
    @ContentAreaRelative val contentAreaRelativeX = domainRelative2contentAreaRelativeX(x)
    return contentAreaRelative2windowX(contentAreaRelativeX)
  }

  fun domainRelative2windowY(@DomainRelative @pct y: Double): @Window @px Double {
    @ContentAreaRelative val contentAreaRelativeY = domainRelative2contentAreaRelativeY(y)
    return contentAreaRelative2windowY(contentAreaRelativeY)
  }

  @Allocates(AllocationCost.Constant)
  fun domainRelative2window(@DomainRelative @pct coordinates: Coordinates): @Window @px Coordinates {
    return Coordinates.of(
      domainRelative2windowX(coordinates.x),
      domainRelative2windowY(coordinates.y)
    )
  }

  fun domain2contentAreaX(@Domain x: Double, valueRange: ValueRange): @ContentArea Double {
    return domainRelative2contentAreaX(valueRange.toDomainRelative(x))
  }

  fun domain2contentAreaY(@Domain y: Double, valueRange: ValueRange): @ContentArea Double {
    return domainRelative2contentAreaY(valueRange.toDomainRelative(y))
  }

  @Allocates(AllocationCost.Constant)
  fun domain2contentArea(@Domain coordinates: Coordinates, valueRangeX: ValueRange, valueRangeY: ValueRange): @ContentArea Coordinates {
    return Coordinates.of(
      domain2contentAreaX(coordinates.x, valueRangeX),
      domain2contentAreaY(coordinates.y, valueRangeY)
    )
  }


  fun domain2windowX(@Domain x: Double, valueRange: ValueRange): @Window @px Double {
    @ContentArea val contentAreaX = domainRelative2contentAreaX(valueRange.toDomainRelative(x))
    return contentArea2windowX(contentAreaX)
  }

  fun domain2windowY(@Domain y: Double, valueRange: ValueRange): @Window @px Double {
    @ContentArea val contentAreaY = domainRelative2contentAreaY(valueRange.toDomainRelative(y))
    return contentArea2windowY(contentAreaY)
  }

  fun domain2windowYInViewport(@Domain y: Double, valueRange: ValueRange): @Window @px Double {
    return coerceInViewportY(domain2windowY(y, valueRange))
  }

  @Allocates(AllocationCost.Constant)
  fun domain2window(@Domain coordinates: Coordinates, valueRangeX: ValueRange, valueRangeY: ValueRange): @Window @px Coordinates {
    return Coordinates.of(
      domain2windowX(coordinates.x, valueRangeX),
      domain2windowY(coordinates.y, valueRangeY)
    )
  }


  //
  //
  // Backward conversion: Window to Domain
  //
  // Window --> Zoomed --> ContentArea --> ContentAreaRelative --> DomainRelative
  //
  //

  // Window --> Zoomed

  fun window2zoomedX(@Window @px x: Double): @Zoomed Double {
    @Zoomed val translateX = chartState.windowTranslation.x

    return InternalCalculations.window2zoomed(x, translateX)
  }

  fun window2zoomedY(@Window @px y: Double): @Zoomed Double {
    @Zoomed val translateY = chartState.windowTranslation.y

    return InternalCalculations.window2zoomed(y, translateY)
  }

  @Allocates(AllocationCost.Constant)
  fun window2zoomed(@Window @px coordinates: Coordinates): @Zoomed Coordinates {
    return Coordinates.of(
      window2zoomedX(coordinates.x),
      window2zoomedY(coordinates.y)
    )
  }

  @Allocates(AllocationCost.Constant)
  fun window2zoomed(@Window @px size: Size): @Zoomed Size {
    return Size.of(
      window2zoomedX(size.width),
      window2zoomedY(size.height)
    )
  }

  // Zoomed --> ContentArea

  fun zoomed2contentAreaX(@Zoomed @px x: Double): @ContentArea Double {
    @px val zoomFactorX = chartState.zoomX
    return InternalCalculations.zoomed2contentArea(x, zoomFactorX)
  }

  fun zoomed2contentAreaY(@Zoomed @px y: Double): @ContentArea Double {
    @px val zoomFactorY = chartState.zoomY
    return InternalCalculations.zoomed2contentArea(y, zoomFactorY)
  }

  @Allocates(AllocationCost.Constant)
  fun zoomed2contentArea(@Zoomed @px coordinates: Coordinates): @ContentArea Coordinates {
    return Coordinates.of(
      zoomed2contentAreaX(coordinates.x),
      zoomed2contentAreaY(coordinates.y)
    )
  }

  @Allocates(AllocationCost.Constant)
  fun zoomed2contentArea(@Zoomed @px size: Size): @ContentArea Size {
    return Size.of(
      zoomed2contentAreaX(size.width),
      zoomed2contentAreaY(size.height)
    )
  }


  // ContentArea --> ContentAreaRelative
  open fun contentArea2contentAreaRelativeX(@ContentArea @px x: Double): @ContentAreaRelative Double {
    return InternalCalculations.contentArea2contentAreaRelative(x, chartState.contentAreaWidth)
  }

  open fun contentArea2contentAreaRelativeY(@ContentArea @px y: Double): @ContentAreaRelative Double {
    return InternalCalculations.contentArea2contentAreaRelative(y, chartState.contentAreaHeight)
  }

  @Allocates(AllocationCost.Constant)
  fun contentArea2contentAreaRelative(@ContentArea @px coordinates: Coordinates): @ContentAreaRelative Coordinates {
    return Coordinates.of(
      contentArea2contentAreaRelativeX(coordinates.x),
      contentArea2contentAreaRelativeY(coordinates.y)
    )
  }

  @Allocates(AllocationCost.Constant)
  fun contentArea2contentAreaRelative(@ContentArea @px size: Size): @ContentAreaRelative Size {
    return Size.of(
      contentArea2contentAreaRelativeX(size.width),
      contentArea2contentAreaRelativeY(size.height)
    )
  }

  // ContentAreaRelative --> DomainRelative

  fun contentAreaRelative2domainRelativeX(@ContentAreaRelative @px x: Double): @DomainRelative Double {
    return InternalCalculations.contentAreaRelative2domainRelative(x, chartState.axisOrientationX)
  }

  fun contentAreaRelative2domainRelativeY(@ContentAreaRelative @px y: Double): @DomainRelative Double {
    return InternalCalculations.contentAreaRelative2domainRelative(y, chartState.axisOrientationY)
  }

  @Allocates(AllocationCost.Constant)
  fun contentAreaRelative2domainRelative(@ContentAreaRelative @px coordinates: Coordinates): @DomainRelative Coordinates {
    return Coordinates.of(
      contentAreaRelative2domainRelativeX(coordinates.x),
      contentAreaRelative2domainRelativeY(coordinates.y)
    )
  }

  @Allocates(AllocationCost.Constant)
  fun contentAreaRelative2domainRelative(@ContentAreaRelative @px size: Size): @DomainRelative Size {
    return Size.of(
      contentAreaRelative2domainRelativeX(size.width),
      contentAreaRelative2domainRelativeY(size.height)
    )
  }

  //
  // Convenience calculations that directly convert some values
  //

  // Window-relative -> Window

  /**
   * Converts a relative horizontal position to an absolute horizontal position
   * @see windowRelative2WindowY
   */
  fun windowRelative2WindowX(@WindowRelative @pct relativePosition: Double): @Window Double {
    return chartState.windowWidth * relativePosition
  }

  /**
   * Converts a relative vertical position to an absolute vertical position
   * @see windowRelative2WindowX
   */
  fun windowRelative2WindowY(@WindowRelative @pct relativePosition: Double): @Window Double {
    return chartState.windowHeight * relativePosition
  }

  // Window --> (Zoomed) --> ContentArea

  fun window2contentAreaX(@Window @px x: Double): @ContentArea Double {
    @ContentArea val zoomedX = window2zoomedX(x)
    return zoomed2contentAreaX(zoomedX)
  }

  fun window2contentAreaY(@Window @px y: Double): @ContentArea Double {
    @ContentArea val zoomedY = window2zoomedY(y)
    return zoomed2contentAreaY(zoomedY)
  }

  @Allocates(AllocationCost.Constant)
  fun window2contentArea(@Window @px coordinates: Coordinates): @ContentArea Coordinates {
    return Coordinates.of(
      window2contentAreaX(coordinates.x),
      window2contentAreaY(coordinates.y)
    )
  }

  @Allocates(AllocationCost.Constant)
  fun window2contentArea(@Window @px x: Double, @Window @px y: Double): @ContentArea Coordinates {
    return Coordinates.of(
      window2contentAreaX(x),
      window2contentAreaY(y)
    )
  }


  // Window --> (Zoomed --> ContentArea) --> ContentAreaRelative

  fun window2contentAreaRelativeX(@Window @px x: Double): @ContentAreaRelative Double {
    return contentArea2contentAreaRelativeX(window2contentAreaX(x))
  }

  fun window2contentAreaRelativeY(@Window @px y: Double): @ContentAreaRelative Double {
    return contentArea2contentAreaRelativeY(window2contentAreaY(y))
  }

  @Allocates(AllocationCost.Constant)
  fun window2contentAreaRelative(@Window @px x: Double, @Window @px y: Double): @ContentAreaRelative Coordinates {
    return Coordinates.of(
      window2contentAreaRelativeX(x),
      window2contentAreaRelativeY(y)
    )
  }

  @Allocates(AllocationCost.Constant)
  fun window2contentAreaRelative(@Window coordinates: Coordinates): @ContentAreaRelative Coordinates {
    return Coordinates.of(
      window2contentAreaRelativeX(coordinates.x),
      window2contentAreaRelativeY(coordinates.y)
    )
  }

  // Window --> (Zoomed --> ContentArea --> ContentAreaRelative ) --> DomainRelative

  fun window2domainRelativeX(@Window @px x: Double): @DomainRelative Double {
    return contentAreaRelative2domainRelativeX(window2contentAreaRelativeX(x))
  }

  fun window2domainRelativeY(@Window @px y: Double): @DomainRelative Double {
    return contentAreaRelative2domainRelativeY(window2contentAreaRelativeY(y))
  }

  @Allocates(AllocationCost.Constant)
  fun window2domainRelative(@Window @px x: Double, @Window @px y: Double): @DomainRelative Coordinates {
    return Coordinates.of(
      window2domainRelativeX(x),
      window2domainRelativeY(y)
    )
  }

  @Allocates(AllocationCost.Constant)
  fun window2domainRelative(@Window @px coordinates: Coordinates): @DomainRelative Coordinates {
    return Coordinates.of(
      window2domainRelativeX(coordinates.x),
      window2domainRelativeY(coordinates.y)
    )
  }

  fun window2domainX(@Window @px x: Double, valueRange: ValueRange): @Domain Double {
    return valueRange.toDomain(window2domainRelativeX(x))
  }

  fun window2domainY(@Window @px y: Double, valueRange: ValueRange): @Domain Double {
    return valueRange.toDomain(window2domainRelativeY(y))
  }

  @Allocates(AllocationCost.Constant)
  fun window2domain(@Window @px x: Double, @Window @px y: Double, valueRange: ValueRange): @Domain Coordinates {
    return Coordinates.of(
      window2domainX(x, valueRange),
      window2domainY(y, valueRange)
    )
  }

  @Allocates(AllocationCost.Constant)
  fun window2domain(@Window @px coordinates: Coordinates, valueRangeX: ValueRange, valueRangeY: ValueRange): @Domain Coordinates {
    return Coordinates.of(
      window2domainX(coordinates.x, valueRangeX),
      window2domainY(coordinates.y, valueRangeY)
    )
  }

  @Allocates(AllocationCost.Constant)
  fun zoomed2domainDelta(@Zoomed @px coordinates: Coordinates, valueRangeX: LinearValueRange, valueRangeY: LinearValueRange): @Domain Coordinates {
    return Coordinates.of(
      zoomed2domainDeltaX(coordinates.x, valueRangeX),
      zoomed2domainDeltaY(coordinates.y, valueRangeY)
    )
  }

  @Allocates(AllocationCost.Constant)
  fun zoomed2domainDelta(@Zoomed @px distance: Distance, valueRangeX: LinearValueRange, valueRangeY: LinearValueRange): @Domain Coordinates {
    return Coordinates.of(
      zoomed2domainDeltaX(distance.x, valueRangeX),
      zoomed2domainDeltaY(distance.y, valueRangeY)
    )
  }

  /**
   * Zoomed 2 domain delta
   */
  fun zoomed2domainDeltaX(@Zoomed @px x: Double, contentAreaDomainRange: LinearValueRange): @Domain Double {
    @ContentAreaRelative val contentAreaRelative = zoomed2contentAreaRelativeX(x)
    return contentAreaDomainRange.deltaToDomain(contentAreaRelative)
  }

  /**
   * Zoomed 2 domain delta
   */
  fun zoomed2domainDeltaY(@Zoomed @px y: Double, contentAreaDomainRange: LinearValueRange): @Domain Double {
    @ContentAreaRelative val contentAreaRelative = zoomed2contentAreaRelativeY(y)
    return contentAreaDomainRange.deltaToDomain(contentAreaRelative)
  }

  /**
   * Returns the currently visible value range - on the x axis
   */
  fun visibleDomainRangeXinWindow(contentAreaValueRange: LinearValueRange): LinearValueRange {
    return TimeRange.fromUnsorted(
      window2domainX(0.0, contentAreaValueRange),
      window2domainX(chartState.windowWidth, contentAreaValueRange)
    )
  }

  /**
   * Returns the currently visible value range - on the y axis
   */
  fun visibleDomainRangeYinWindow(contentAreaValueRange: LinearValueRange): LinearValueRange {
    return TimeRange.fromUnsorted(
      window2domainY(0.0, contentAreaValueRange),
      window2domainY(chartState.windowHeight, contentAreaValueRange)
    )
  }



  //  Zoomed --> (ContentArea) --> ContentAreaRelative

  fun zoomed2contentAreaRelativeX(@Zoomed @px x: Double): @ContentAreaRelative Double {
    return contentArea2contentAreaRelativeX(zoomed2contentAreaX(x))
  }

  fun zoomed2contentAreaRelativeY(@Zoomed @px y: Double): @ContentAreaRelative Double {
    return contentArea2contentAreaRelativeY(zoomed2contentAreaY(y))
  }

  @Allocates(AllocationCost.Constant)
  fun zoomed2contentAreaRelative(@Zoomed @px size: Size): @ContentAreaRelative Size {
    return Size.of(
      zoomed2contentAreaRelativeX(size.width),
      zoomed2contentAreaRelativeY(size.height)
    )
  }

  //  Zoomed --> (ContentArea --> ContentAreaRelative) --> DomainRelative

  fun zoomed2domainRelativeX(@Zoomed @px x: Double): @DomainRelative Double {
    return contentAreaRelative2domainRelativeX(zoomed2contentAreaRelativeX(x))
  }

  fun zoomed2domainRelativeY(@Zoomed @px y: Double): @DomainRelative Double {
    return contentAreaRelative2domainRelativeY(zoomed2contentAreaRelativeY(y))
  }

  @Allocates(AllocationCost.Constant)
  fun zoomed2domainRelative(@Zoomed @px coordinates: Coordinates): @DomainRelative Coordinates {
    return Coordinates.of(
      zoomed2domainRelativeX(coordinates.x),
      zoomed2domainRelativeY(coordinates.y)
    )
  }

  fun zoomedDelta2domainRelativeX(@Zoomed @px x: Double): @DomainRelative Double {
    val contentAreaRelativeX = contentAreaRelative2domainRelativeX(zoomed2contentAreaRelativeX(x))
    val contentAreaRelativeBase = contentAreaRelative2domainRelativeX(zoomed2contentAreaRelativeX(0.0))

    return contentAreaRelativeX - contentAreaRelativeBase
  }

  fun zoomedDelta2domainRelativeY(@Zoomed @px y: Double): @DomainRelative Double {
    val contentAreaRelativeY = contentAreaRelative2domainRelativeY(zoomed2contentAreaRelativeY(y))
    val contentAreaRelativeBase = contentAreaRelative2domainRelativeY(zoomed2contentAreaRelativeY(0.0))

    return contentAreaRelativeY - contentAreaRelativeBase
  }


  //  ContentArea --> (ContentAreaRelative) --> DomainRelative

  fun contentArea2domainRelativeX(@ContentArea @px x: Double): @DomainRelative Double {
    return contentAreaRelative2domainRelativeX(contentArea2contentAreaRelativeX(x))
  }

  fun contentArea2domainRelativeY(@ContentArea @px y: Double): @DomainRelative Double {
    return contentAreaRelative2domainRelativeY(contentArea2contentAreaRelativeY(y))
  }

  @Allocates(AllocationCost.Constant)
  fun contentArea2domainRelative(@ContentArea @px coordinates: Coordinates): @DomainRelative Coordinates {
    return Coordinates.of(
      contentArea2domainRelativeX(coordinates.x),
      contentArea2domainRelativeY(coordinates.y)
    )
  }

  /**
   * Returns the tile index for window coordinates
   */
  fun window2tileIndex(window: @Window Coordinates, tileSize: @Zoomed Size): TileIndex {
    return InternalCalculations.calculateTileIndex(window2contentArea(window), zoomed2contentArea(tileSize))
  }

  /**
   * Converts the tile index to window coordinates
   */
  @Allocates(AllocationCost.Constant)
  fun tileIndex2window(tileIndex: TileIndex, tileSize: @Zoomed Size): @Window Coordinates {
    @ContentArea val tileOrigin = tileIndex2contentArea(tileIndex, tileSize)
    return contentArea2window(tileOrigin)
  }

  /**
   * Returns the tile index for a content area coordinates
   */
  fun contentArea2tileIndex(contentArea: @ContentArea Coordinates, tileSize: @Zoomed Size): TileIndex {
    return InternalCalculations.calculateTileIndex(contentArea, zoomed2contentArea(tileSize))
  }

  fun contentArea2tileIndex(contentAreaX: @ContentArea Double, contentAreaY: @ContentArea Double, tileSize: @Zoomed Size): TileIndex {
    return InternalCalculations.calculateTileIndex(contentAreaX, contentAreaY, zoomed2contentArea(tileSize))
  }

  /**
   * Returns the content area values for a given tile index
   */
  @Allocates(AllocationCost.Constant)
  fun tileIndex2contentArea(tileIndex: TileIndex, tileSize: @Zoomed Size): @ContentArea Coordinates {
    val tileSizeContentArea = zoomed2contentArea(tileSize)
    return InternalCalculations.calculateTileOrigin(tileIndex, tileSizeContentArea)
  }


  //
  //
  // Time related methods
  //
  //
  fun time2windowX(@Time @ms time: Double, contentAreaTimeRange: TimeRange): @px @Window Double {
    return timeRelative2windowX(contentAreaTimeRange.time2relative(time))
  }

  fun timeDuration2zoomedX(@Time @ms duration: Double, contentAreaTimeRange: TimeRange): @px @Window Double {
    return timeRelative2zoomedX(contentAreaTimeRange.time2relativeDelta(duration))
  }

  fun timeDuration2zoomedY(@Time @ms duration: Double, contentAreaTimeRange: TimeRange): @px @Window Double {
    return timeRelative2zoomedY(contentAreaTimeRange.time2relativeDelta(duration))
  }

  fun time2windowY(@Time @ms time: Double, contentAreaTimeRange: TimeRange): @px @Window Double {
    return timeRelative2windowY(contentAreaTimeRange.time2relative(time))
  }

  fun window2timeX(@px @Window value: Double, contentAreaTimeRange: TimeRange): @Time @ms Double {
    @TimeRelative @pct val relativeTime = window2timeRelativeX(value)
    return contentAreaTimeRange.relative2time(relativeTime)
  }

  fun window2timeY(@px @Window value: Double, contentAreaTimeRange: TimeRange): @Time @ms Double {
    @TimeRelative @pct val relativeTime = window2timeRelativeY(value)
    return contentAreaTimeRange.relative2time(relativeTime)
  }

  fun contentAreaRelative2timeX(value: @ContentAreaRelative Double, contentAreaTimeRange: TimeRange): @Time @ms Double {
    @TimeRelative @pct val relativeTime = contentAreaRelative2timeRelativeX(value)
    return contentAreaTimeRange.relative2time(relativeTime)
  }

  fun contentAreaRelative2timeRelativeX(value: @ContentAreaRelative Double): @TimeRelative @pct Double {
    return contentAreaRelative2domainRelativeX(value)
  }

  fun contentAreaRelative2timeRelativeY(value: @ContentAreaRelative Double): @TimeRelative @pct Double {
    return contentAreaRelative2domainRelativeY(value)
  }

  /**
   * Zoomed 2 time delta
   */
  fun zoomed2timeDeltaX(@Zoomed @px x: Double, contentAreaTimeRange: TimeRange): @Time Double {
    @ContentAreaRelative val contentAreaRelative = zoomed2contentAreaRelativeX(x)
    return contentAreaTimeRange.relative2timeDelta(contentAreaRelative)
  }

  /**
   * Zoomed 2 time delta
   */
  fun zoomed2timeDeltaY(@Zoomed @px y: Double, contentAreaTimeRange: TimeRange): @Time Double {
    @ContentAreaRelative val contentAreaRelative = zoomed2contentAreaRelativeY(y)
    return contentAreaTimeRange.relative2timeDelta(contentAreaRelative)
  }

  fun timeRelative2windowX(@TimeRelative @pct value: Double): @Window @px Double {
    return domainRelative2windowX(value)
  }

  fun timeRelative2zoomedX(@TimeRelative @pct value: Double): @Zoomed @px Double {
    return domainRelative2zoomedX(value)
  }

  fun timeRelative2zoomedY(@TimeRelative @pct value: Double): @Zoomed @px Double {
    return domainRelative2zoomedY(value)
  }

  fun timeRelative2windowY(@TimeRelative @pct value: Double): @Window @px Double {
    return domainRelative2windowY(value)
  }

  fun window2timeRelativeX(@Window @px value: Double): @TimeRelative @pct Double {
    return window2domainRelativeX(value)
  }

  fun window2timeRelativeY(@Window @px value: Double): @TimeRelative @pct Double {
    return window2domainRelativeY(value)
  }

  /**
   * Returns the currently visible time range - on the x axis
   */
  fun visibleTimeRangeXinWindow(contentAreaTimeRange: TimeRange): TimeRange {
    return TimeRange.fromUnsorted(
      window2timeX(0.0, contentAreaTimeRange),
      window2timeX(chartState.windowWidth, contentAreaTimeRange)
    )
  }

  /**
   * Returns the currently visible time range - on the y axis
   */
  fun visibleTimeRangeYinWindow(contentAreaTimeRange: TimeRange): TimeRange {
    return TimeRange.fromUnsorted(
      window2timeY(0.0, contentAreaTimeRange),
      window2timeY(chartState.windowHeight, contentAreaTimeRange)
    )
  }

  inline fun isInWindowY(y: @Window Double): Boolean {
    return chartState.isInWindowY(y)
  }

  val contentViewportWidth: @Window Double by chartState::contentViewportWidth
  val contentViewportHeight: @Window Double by chartState::contentViewportHeight

  /**
   * Returns the min x value for the content viewport
   */
  fun contentViewportMinX(): @Window Double {
    return chartState.contentViewportMarginLeft
  }

  /**
   * Returns the min y value for the content viewport
   */
  fun contentViewportMinY(): @Window Double {
    return chartState.contentViewportMarginTop
  }

  /**
   * Returns the min x value for the content viewport
   */
  fun contentViewportMaxX(): @Window Double {
    return chartState.windowWidth - chartState.contentViewportMarginRight
  }

  /**
   * Returns the min y value for the content viewport
   */
  fun contentViewportMaxY(): @Window Double {
    return chartState.windowHeight - chartState.contentViewportMarginBottom
  }

  /**
   * Returns true if the given x coordinates is in the viewport, false otherwise
   */
  fun isInViewportX(x: @Window Double): Boolean {
    return contentViewportMinX() <= x && x <= contentViewportMaxX()
  }

  fun isInViewportY(y: @Window Double): Boolean {
    return contentViewportMinY() <= y && y <= contentViewportMaxY()
  }

  fun isAboveViewportY(y: @Window Double): Boolean {
    return y < contentViewportMinY()
  }

  fun isBelowViewportY(y: @Window Double): Boolean {
    return y > contentViewportMaxY()
  }

  fun isLeftOfViewportX(x: @Window Double): Boolean {
    return x < contentViewportMinX()
  }

  fun isRightOfViewportX(x: @Window Double): Boolean {
    return x > contentViewportMaxX()
  }

  fun coerceInViewportX(window: @Window @px Double): @Window @px Double {
    return window.coerceIn(contentViewportMinX(), contentViewportMaxX())
  }

  fun coerceInViewportY(window: @Window @px Double): @Window @px Double {
    return window.coerceIn(contentViewportMinY(), contentViewportMaxY())
  }

  /**
   * Returns the given x value - if it is in the viewport - or the fallback value if x is *not* in the content viewport
   */
  fun inViewportOrX(x: @Window @px Double, fallback: Double = Double.NaN): @Window Double {
    if (isInViewportX(x)) {
      return x
    }
    return fallback
  }

  fun inViewportOrY(y: @Window @px Double, fallback: Double = Double.NaN): @Window Double {
    if (isInViewportY(y)) {
      return y
    }
    return fallback
  }


  //Additional methods. Untested!


  /**
   * Converts a window-relative X position (0.0 to 1.0) to a content-area-relative X position.
   */
  fun windowRelative2contentAreaRelativeX(
    /**
     * the relative X position in the window, where 0.0 is the left edge and 1.0 is the right edge.
     */
    @WindowRelative @pct relativeX: Double
  ): @ContentAreaRelative Double {
    val windowX = windowRelative2WindowX(relativeX)
    return window2contentAreaRelativeX(windowX)
  }

  /**
   * Converts a window-relative Y position (0.0 to 1.0) to a content-area-relative Y position.
   */
  fun windowRelative2contentAreaRelativeY(
    /**
     * the relative Y position in the window, where 0.0 is the top edge and 1.0 is the bottom edge.
     */
    @WindowRelative @pct relativeY: Double
  ): @ContentAreaRelative Double {
    val windowY = windowRelative2WindowY(relativeY)
    return window2contentAreaRelativeY(windowY)
  }


  /**
   * Checks if a domain-relative X value is currently visible within the viewport.
   */
  fun isInViewportDomainRelativeX(
    /**
     * the relative X value in the domain, where 0.0 is the start and 1.0 is the end.
     */
    @DomainRelative @pct x: Double,
    valueRange: ValueRange
  ): Boolean {
    val windowX = domainRelative2windowX(x)
    return isInViewportX(windowX)
  }

  /**
   * Checks if a domain-relative Y value is currently visible within the viewport.
   */
  fun isInViewportDomainRelativeY(
    /**
     * the relative Y value in the domain, where 0.0 is the start and 1.0 is the end.
     */
    @DomainRelative @pct y: Double,
    valueRange: ValueRange
  ): Boolean {
    val windowY = domainRelative2windowY(y)
    return isInViewportY(windowY)
  }


  companion object {
    val percentageRange: ClosedRange<Double> = 0.0..1.0

    /**
     * Returns true if the given value is within the content area
     */
    fun inContentArea(relative: @ContentAreaRelative @DomainRelative Double): Boolean {
      return relative in percentageRange
    }
  }
}

fun @DomainRelative Double.domainRelative2WindowX(chartCalculator: ChartCalculator): Double {
  return chartCalculator.domainRelative2windowX(this)
}

fun @DomainRelative Double.domainRelative2WindowY(chartCalculator: ChartCalculator): Double {
  return chartCalculator.domainRelative2windowY(this)
}

fun @ContentAreaRelative Double.contentAreaRelative2WindowX(chartCalculator: ChartCalculator): Double {
  return chartCalculator.contentAreaRelative2windowX(this)
}

fun @ContentAreaRelative Double.contentAreaRelative2WindowY(chartCalculator: ChartCalculator): Double {
  return chartCalculator.contentAreaRelative2windowY(this)
}
