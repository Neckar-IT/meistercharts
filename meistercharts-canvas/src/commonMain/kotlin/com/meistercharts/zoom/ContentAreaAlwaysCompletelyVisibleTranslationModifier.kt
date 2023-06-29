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
package com.meistercharts.zoom

import com.meistercharts.calc.ChartCalculator
import com.meistercharts.axis.AxisSelection
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.geometry.Distance
import com.meistercharts.model.Insets
import com.meistercharts.model.Zoom
import it.neckar.open.unit.other.px

/**
 * Default implementations that limits the panning depending on the zoom level.
 * This modifier ensures the content area is always fully visible when panned.
 *
 * See "[internal/closed/charting/meistercharts-canvas/doc/translation/ContentAlwaysFullyVisible.svg] for a visualization how this class works.
 *
 * On zoom factor > 1.0
 * - The top left edge of the content is always visible
 * -
 *
 */
class ContentAreaAlwaysCompletelyVisibleTranslationModifier(
  val axisSelection: AxisSelection = AxisSelection.Both,
  /**
   * Provides the margin that will also be kept visible
   */
  val marginProvider: (calculator: ChartCalculator) -> @Zoomed Insets = { it.chartState.contentViewportMargin },
  /**
   * Provides the bounds that shall be kept visible.
   * Usually the complete content area is kept visible
   */
  val boundsProvider: @ContentAreaRelative BoundsProvider = DefaultBoundsProvider,
  val delegate: ZoomAndTranslationModifier,
) : ZoomAndTranslationModifier {
  /**
   * Modifies the min/max panning.
   * A visualization that describes the limits can be found in "DefaultPanLimiter.svg"
   */
  @ContentArea
  @px
  override fun modifyTranslation(@Zoomed @px translation: Distance, calculator: ChartCalculator): Distance {
    //The margin that is kept visible, too
    @Zoomed val margin = marginProvider(calculator)

    @ContentArea val contentAreaWidth = calculator.chartState.contentAreaWidth
    @ContentArea val contentAreaHeight = calculator.chartState.contentAreaHeight
    @Zoomed val windowWidth = calculator.chartState.windowWidth
    @Zoomed val windowHeight = calculator.chartState.windowHeight

    //The left bounds of the content that must be visible
    @Zoomed val contentLeft = calculator.contentAreaRelative2zoomedX(boundsProvider.left()) - margin.left
    //The right side of the content that must be visible
    @Zoomed val contentRight = calculator.contentAreaRelative2zoomedX(boundsProvider.right()) + margin.right
    //the top of the visible content
    @Zoomed val contentTop = calculator.contentAreaRelative2zoomedY(boundsProvider.top()) - margin.top
    //The bottom of the visible content
    @Zoomed val contentBottom = calculator.contentAreaRelative2zoomedY(boundsProvider.bottom()) + margin.bottom


    //The width of the content that shall be visible
    @Zoomed val contentWidth = contentRight - contentLeft
    //The height of the content that shall be visible
    @Zoomed val contentHeight = contentBottom - contentTop


    //If true, then the complete content is visible horizontally
    val everythingVisibleHorizontally = contentWidth <= windowWidth
    //If true, then the complete content is visible vertically
    val everythingVisibleVertically = contentHeight <= windowHeight


    val minX = if (everythingVisibleHorizontally) {
      //left side must always be visible
      0.0 - contentLeft
    } else {
      //right side must always be visible
      windowWidth - contentRight
    }

    val maxX = if (everythingVisibleHorizontally) {
      //left side must always be visible
      windowWidth - contentRight
    } else {
      //right side must always be visible
      0.0 - contentLeft
    }


    val minY = if (everythingVisibleVertically) {
      //top side must always be visible
      0.0 - contentTop
    } else {
      //bottom side must always be visible
      windowHeight - contentBottom
    }

    val maxY = if (everythingVisibleVertically) {
      //top side must always be visible
      windowHeight - contentBottom
    } else {
      //bottom side must always be visible
      0.0 - contentTop
    }

    return delegate.modifyTranslation(translation, calculator)
      .withMin(minX, minY, axisSelection)
      .withMax(maxX, maxY, axisSelection)
  }

  override fun modifyZoom(zoom: Zoom, calculator: ChartCalculator): Zoom {
    return delegate.modifyZoom(zoom, calculator)
  }
}

/**
 * The default bounds provider that returns the content area range from 0.0..1.0
 */
object DefaultBoundsProvider : BoundsProvider {
  override fun left(): @ContentAreaRelative Double {
    return 0.0
  }

  override fun right(): @ContentAreaRelative Double {
    return 1.0
  }

  override fun top(): @ContentAreaRelative Double {
    return 0.0
  }

  override fun bottom(): @ContentAreaRelative Double {
    return 1.0
  }
}

/**
 * Provides the bounds that shall be kept visible
 */
interface BoundsProvider {
  /**
   * Returns the left side (usually 0.0)
   */
  fun left(): @ContentAreaRelative Double {
    return 0.0
  }

  /**
   * Returns the right side (usually 1.0)
   */
  fun right(): @ContentAreaRelative Double {
    return 1.0
  }

  /**
   * Returns the top (usually 0.0)
   */
  fun top(): @ContentAreaRelative Double {
    return 0.0
  }

  /**
   * Returns the bottom (usually 1.0)
   */
  fun bottom(): @ContentAreaRelative Double {
    return 1.0
  }
}
