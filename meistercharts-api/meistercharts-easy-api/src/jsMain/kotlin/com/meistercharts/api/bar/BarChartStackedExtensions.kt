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
package com.meistercharts.api.bar

import com.meistercharts.algorithms.layers.DefaultCategoryLayouter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.api.applyCategoryAxisStyle
import com.meistercharts.api.applyLinesStyle
import com.meistercharts.api.applyTitleStyle
import com.meistercharts.api.applyValueAxisStyle
import com.meistercharts.api.toColor
import com.meistercharts.api.toFontDescriptorFragment
import com.meistercharts.api.toModelLinear
import com.meistercharts.api.toNumberFormat
import com.meistercharts.api.withValues
import com.meistercharts.charts.BarChartStackedGestalt
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.other.px
import it.neckar.commons.kotlin.js.debug
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.ifDebug

private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.api.bar.BarChartStackedExtensions")


/**
 * Is called initially and applies the Easy Api defaults
 */
fun BarChartStackedGestalt.applyEasyApiDefaults() {
  style.applyAxisTitleOnTop(40.0)
  // Enable as soon as the bar chart stacked supports value axis
  // thresholdsSupport.getHudLayer().configuration.maxWidth = MultiDoublesProvider.always(Double.NaN)
}

/**
 * Applies the JS style to the bart chart gestalt style
 */
fun BarChartStackedGestalt.applyStyle(jsStyle: BarChartStackedStyle) {
  logger.debug("BarChartStackedGestalt.applyStyle", jsStyle)

  //Specific stuff for stacked

  //Beware that the order matters. Set the orientation before any other style
  //because a change in the orientation triggers a reset of the layout.
  jsStyle.horizontal?.let {
    if (it) {
      this.style.applyHorizontalConfiguration() //resets the content area margins!
    } else {
      this.style.applyVerticalConfiguration() //resets the content area margins!
    }

    //ensure that there is some space, the content are top is potentially overridden by a property later
    style.applyAxisTitleOnTop(40.0)
  }

  jsStyle.valueRange?.toModelLinear()?.let {
    this.style.applyValueRange(it)
  }

  jsStyle.barWidth?.let {
    stackedBarsPainter.style.maxBarSize = it
  }

  jsStyle.minBarDistance?.let {
    (this.categoryLayer.style.layoutCalculator as DefaultCategoryLayouter).style.minCategorySize = it
  }

  jsStyle.maxBarDistance?.let {
    (this.categoryLayer.style.layoutCalculator as DefaultCategoryLayouter).style.maxCategorySize = it
  }

  jsStyle.segmentColors?.let { colorCodes ->
    this.stackedBarsPainter.stackedBarPaintable.style.colorsProvider = MultiProvider.forListModulo(colorCodes.map { it.toColor() }, Color.silver)
  }

  jsStyle.remainderSegmentBorderColor?.toColor()?.let {
    this.stackedBarsPainter.stackedBarPaintable.style.remainderSegmentBorderColor = it
  }

  jsStyle.remainderSegmentBackgroundColor?.toColor()?.let {
    this.stackedBarsPainter.stackedBarPaintable.style.remainderSegmentBackgroundColor = it
  }

  jsStyle.remainderSegmentBorderWidth?.let {
    this.stackedBarsPainter.stackedBarPaintable.style.remainderSegmentBorderLineWidth = it
  }

  jsStyle.valueFormat?.toNumberFormat()?.let {
    this.stackedBarsPainter.stackedBarPaintable.style.valueLabelFormat = it
  }

  jsStyle.categoryAxisStyle?.let { jsValueAxisStyle ->
    this.categoryAxisTopTitleLayer.configuration.applyTitleStyle(jsValueAxisStyle)
    this.categoryAxisLayer.style.applyCategoryAxisStyle(jsValueAxisStyle)

    jsValueAxisStyle.axisSize?.let {
      contentViewportMargin = contentViewportMargin.withSide(categoryAxisLayer.style.side, it)
    }
  }

  jsStyle.valueAxisStyle?.let { jsValueAxisStyle ->
    this.valueAxisTopTitleLayer.configuration.applyTitleStyle(jsValueAxisStyle)
    this.valueAxisLayer.style.applyValueAxisStyle(jsValueAxisStyle)

    jsValueAxisStyle.axisSize?.let {
      contentViewportMargin = contentViewportMargin.withSide(valueAxisLayer.style.side, it)
    }
  }

  this.gridLayer.configuration.applyLinesStyle(jsStyle.gridStyle) { this.style.valueRange }
  jsStyle.gridStyle?.visible?.let {
    this.style.showGrid = it
  }

  jsStyle.valueLabelFont?.toFontDescriptorFragment()?.let {
    this.style.applyValueLabelFont(it)
  }

  jsStyle.valueLabelColor?.toColor()?.let {
    this.stackedBarsPainter.stackedBarPaintable.style.valueLabelColor = it
  }


  //Calculate the optimal content viewport margin top
  @px val viewportMarginTop = valueAxisSupport.calculateContentViewportMarginTop(Unit, chartSupport())
    .coerceAtLeast(categoryAxisSupport.calculateContentViewportMarginTop(Unit, chartSupport()))
  contentViewportMargin = contentViewportMargin.withTop(viewportMarginTop)

  //Is relevant to get space for the top title
  jsStyle.contentViewportMargin?.let {
    contentViewportMargin = contentViewportMargin.withValues(it)
  }

}
