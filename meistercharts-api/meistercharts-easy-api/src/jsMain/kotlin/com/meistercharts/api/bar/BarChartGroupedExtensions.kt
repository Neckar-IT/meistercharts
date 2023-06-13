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


import com.meistercharts.algorithms.layers.barchart.CategoryModelBoxStylesProvider
import com.meistercharts.algorithms.layers.barchart.CategorySeriesModelColorsProvider
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.api.BoxStyle
import com.meistercharts.api.applyCategoryAxisStyle
import com.meistercharts.api.applyLinesStyle
import com.meistercharts.api.applyStyle
import com.meistercharts.api.applyThresholdStyles
import com.meistercharts.api.applyTitleStyle
import com.meistercharts.api.applyValueAxisStyle
import com.meistercharts.api.category.CategoryConverter
import com.meistercharts.api.toColor
import com.meistercharts.api.toFontDescriptorFragment
import com.meistercharts.api.toModel
import com.meistercharts.api.toModelSizes
import com.meistercharts.api.toNumberFormat
import com.meistercharts.api.toThresholdLabelsProvider
import com.meistercharts.api.toThresholdValuesProvider
import com.meistercharts.api.withValues
import com.meistercharts.charts.BarChartGroupedGestalt
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.getModuloOrNull
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.other.px
import it.neckar.commons.kotlin.js.debug
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.ifDebug

private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.api.bar.BarChartGroupedExtensions")


/**
 * Is called initially and applies the Easy API defaults
 */
fun BarChartGroupedGestalt.applyEasyApiDefaults() {
  style.applyAxisTitleOnTop(40.0)
  groupedBarsPainter.configuration.overflowIndicatorPainter?.configuration?.applyDefaultIndicators(
    Color.darkgray, Color.white, 1.0, 7.0, 7.0
  )
}

/**
 * Applies the JS style to the bart chart gestalt style
 */
fun BarChartGroupedGestalt.applyStyle(jsStyle: BarChartGroupedStyle) {
  logger.ifDebug {
    console.debug("BarChartGroupedGestalt.applyStyle", jsStyle)
  }

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

  //call apply-functions early
  jsStyle.valueRange?.toModel()?.let {
    if (this.style.valueRange != it) {
      this.style.applyValueRange(it)
    }
  }

  jsStyle.minGapBetweenGroups?.let {
    this.style.minGapBetweenGroups = it
  }

  jsStyle.maxGapBetweenGroups?.let {
    this.style.maxGapBetweenGroups = it
  }

  jsStyle.valueAxisStyle?.let { jsValueAxisStyle ->
    this.valueAxisTopTitleLayer.configuration.applyTitleStyle(jsValueAxisStyle)
    this.valueAxisLayer.style.applyValueAxisStyle(jsValueAxisStyle)

    jsValueAxisStyle.axisSize?.let {
      contentViewportMargin = contentViewportMargin.withSide(valueAxisLayer.style.side, it)
    }
  }

  jsStyle.categoryAxisStyle?.let { jsValueAxisStyle ->
    this.categoryAxisTopTitleLayer.configuration.applyTitleStyle(jsValueAxisStyle)
    this.categoryAxisLayer.style.applyCategoryAxisStyle(jsValueAxisStyle)

    jsValueAxisStyle.axisSize?.let {
      contentViewportMargin = contentViewportMargin.withSide(categoryAxisLayer.style.side, it)
    }
  }

  //Threshold
  this.thresholdsSupport.applyThresholdStyles(jsStyle.thresholds, Unit)
  this.configuration.thresholdValues = jsStyle.thresholds.toThresholdValuesProvider()
  this.configuration.thresholdLabels = jsStyle.thresholds.toThresholdLabelsProvider()

  jsStyle.barSize?.let { barWidth ->
    groupedBarsPainter.configuration.setBarSizeRange(barWidth, barWidth)
  }

  jsStyle.barGap?.let {
    groupedBarsPainter.configuration.barGap = it
  }

  this.gridLayer.configuration.applyLinesStyle(jsStyle.gridStyle) { this.style.valueRange }
  jsStyle.gridStyle?.visible?.let {
    this.style.showGrid = it
  }

  jsStyle.valueLabelsStyle?.let { valueLabelsStyle ->
    valueLabelsStyle.showValueLabels?.let {
      this.groupedBarsPainter.configuration.showValueLabel = it
    }

    valueLabelsStyle.valueLabelGapHorizontal?.let {
      this.groupedBarsPainter.configuration.valueLabelAnchorGapHorizontal = it
    }

    valueLabelsStyle.valueLabelGapVertical?.let {
      this.groupedBarsPainter.configuration.valueLabelAnchorGapVertical = it
    }

    valueLabelsStyle.valueLabelFormat?.toNumberFormat()?.let {
      this.groupedBarsPainter.configuration.valueLabelFormat = it
    }

    valueLabelsStyle.valueLabelFont?.toFontDescriptorFragment()?.let {
      this.groupedBarsPainter.configuration.valueLabelFont = it
    }

    valueLabelsStyle.valueLabelColor?.toColor()?.let {
      this.groupedBarsPainter.configuration.valueLabelColor = it
    }

    valueLabelsStyle.valueLabelStrokeColor?.toColor()?.let {
      this.groupedBarsPainter.configuration.valueLabelStrokeColor = it
    }
  }

  CategoryConverter.toCategoryModelColorsProvider(jsStyle.barColors)?.let {
    this.groupedBarsPainter.configuration.colorsProvider = it
  }

  //Ensure that the labels are painted in the window - but not overlapping the axis
  style.applyValueLabelsInWindowRespectingAxis()


  //Tooltip related stuff
  jsStyle.showTooltip?.let {
    this.style.showTooltip = it
  }

  jsStyle.tooltipStyle?.let { jsTooltipStyle ->
    jsTooltipStyle.tooltipFormat?.toNumberFormat()?.let {
      this.style.balloonTooltipValueLabelFormat = it
    }

    jsTooltipStyle.tooltipBoxStyle?.toModel()?.let {
      this.balloonTooltipLayer.tooltipPainter.configuration.boxStyle = it
    }

    jsTooltipStyle.tooltipBoxStyle?.color?.toColor()?.let {
      this.balloonTooltipSupport.symbolAndLegendPaintable.configuration.labelColors = MultiProvider.always(it)
      this.balloonTooltipSupport.tooltipContentPaintable.headlinePaintable.configuration.labelColor = it.asProvider()
    }

    jsTooltipStyle.labelWidth?.let {
      this.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration.maxLabelWidth = it
    }

    jsTooltipStyle.symbolSizes?.toModelSizes()?.let {
      require(it.size == 1) {
        "Require exact one size but got $it"
      }
      this.configuration.applyBalloonTooltipSize(it.first())
    }

    jsTooltipStyle.symbolLabelGap?.let {
      this.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration.symbolLabelGap = it
    }

    jsTooltipStyle.entriesGap?.let {
      this.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration.entriesGap = it
    }

    jsTooltipStyle.tooltipFont?.toFontDescriptorFragment()?.let { it ->
      this.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration.textFont = it.asProvider()
    }

    jsTooltipStyle.headlineFont?.toFontDescriptorFragment()?.let { it ->
      this.balloonTooltipSupport.tooltipContentPaintable.headlinePaintable.configuration.font = it
    }

    jsTooltipStyle.headlineMarginBottom?.let { it ->
      this.balloonTooltipSupport.tooltipContentPaintable.stackedPaintablesPaintable.configuration.entriesGap = it
    }
  }

  jsStyle.activeGroupBackgroundColor.toColor()?.let {
    this.categoryLayer.style.activeCategoryBackground = it
  }

  jsStyle.overflowIndicatorStyle?.let {
    this.groupedBarsPainter.configuration.overflowIndicatorPainter?.applyStyle(it)
  }

  @px val viewportMarginTop = valueAxisSupport.calculateContentViewportMarginTop(Unit, chartSupport())
    .coerceAtLeast(categoryAxisSupport.calculateContentViewportMarginTop(Unit, chartSupport()))
  contentViewportMargin = contentViewportMargin.withTop(viewportMarginTop)

  //Apply the content viewport margin
  jsStyle.contentViewportMargin?.let {
    contentViewportMargin = contentViewportMargin.withValues(it)
  }
}

@Deprecated("no required?")
fun toCategoryModelBoxStylesProvider(jsBoxStyles: Array<Array<BoxStyle>>): CategoryModelBoxStylesProvider {
  val boxStyles = jsBoxStyles.map { s -> s.map { t -> t.toModel() } }
  return CategoryModelBoxStylesProvider { categoryIndex, seriesIndex ->
    boxStyles.getModuloOrNull(categoryIndex.value)
      ?.getModuloOrNull(seriesIndex.value)
      ?: com.meistercharts.style.BoxStyle.none
  }
}

@Deprecated("no required?")
fun toCategoryModelColorsProvider(jsBoxStyles: Array<Array<BoxStyle>>): CategorySeriesModelColorsProvider {
  val colors = jsBoxStyles.map { s -> s.map { t -> t.color.toColor() ?: Color.white } }
  return CategorySeriesModelColorsProvider { categoryIndex, seriesIndex ->
    colors.getModuloOrNull(categoryIndex.value)
      ?.getModuloOrNull(seriesIndex.value)
      ?: Color.white
  }
}
