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
package com.meistercharts.api.bullet

import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.api.applyCategoryAxisStyle
import com.meistercharts.api.applyLinesStyle
import com.meistercharts.api.applyStyle
import com.meistercharts.api.applyThresholdStyles
import com.meistercharts.api.applyTitleStyle
import com.meistercharts.api.applyValueAxisStyle
import com.meistercharts.api.category.CategoryBulletChartData
import com.meistercharts.api.category.CategoryConverter
import com.meistercharts.api.setImagesProvider
import com.meistercharts.api.toColor
import com.meistercharts.api.toFontDescriptorFragment
import com.meistercharts.api.toModel
import com.meistercharts.api.toModelSizes
import com.meistercharts.api.toNumberFormat
import com.meistercharts.api.toThresholdLabelsProvider
import com.meistercharts.api.toThresholdValuesProvider
import com.meistercharts.api.withValues
import com.meistercharts.charts.bullet.BulletChartGestalt
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.unit.other.px
import it.neckar.commons.kotlin.js.debug
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.ifDebug

private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.api.bullet.BulletChartExtensions")


/**
 * Applies the default configuration for Easy API
 */
fun BulletChartGestalt.applyEasyApiDefaults() {
  configuration.applyAxisTitleOnTop(40.0)
}

/**
 * Applies the configuration
 */
fun BulletChartGestalt.applyConfiguration(jsConfiguration: BulletChartConfiguration) {
  logger.ifDebug {
    console.debug("BulletChartGestalt.applyConfiguration", jsConfiguration)
  }

  CategoryConverter.toCurrentValuesProvider(jsConfiguration)?.let {
    this.configuration.currentValues = it
  }
  CategoryConverter.toAreaValueRangesProvider(jsConfiguration)?.let {
    this.configuration.areaValueRanges = it
  }

  jsConfiguration.horizontal?.let {
    if (it) {
      this.configuration.applyHorizontalConfiguration()
    } else {
      this.configuration.applyVerticalConfiguration()
    }

    //ensure that there is some space, the content are top is potentially overridden by a property later
    this.configuration.applyAxisTitleOnTop(40.0)
  }

  jsConfiguration.valueRange?.toModel()?.let {
    if (this.configuration.valueRange != it) {
      this.configuration.applyValueRange(it)
    }
  }

  ////////////////////////////////////////////////////
  //Axis styles
  ////////////////////////////////////////////////////
  jsConfiguration.valueAxisStyle?.let { jsValueAxisStyle ->
    this.valueAxisTopTitleLayer.configuration.applyTitleStyle(jsValueAxisStyle)
    this.valueAxisLayer.style.applyValueAxisStyle(jsValueAxisStyle)

    jsValueAxisStyle.axisSize?.let {
      this.contentViewportMargin = this.contentViewportMargin.withSide(valueAxisLayer.style.side, it)
    }
  }

  jsConfiguration.categoryAxisStyle?.let { jsValueAxisStyle ->
    this.categoryAxisTopTitleLayer.configuration.applyTitleStyle(jsValueAxisStyle)
    this.categoryAxisLayer.style.applyCategoryAxisStyle(jsValueAxisStyle)

    jsValueAxisStyle.axisSize?.let {
      contentViewportMargin = contentViewportMargin.withSide(categoryAxisLayer.style.side, it)
    }
  }
  CategoryConverter.toCategoryImages(jsConfiguration)?.let { images ->
    this.categoryAxisLayer.style.axisLabelPainter.setImagesProvider(MultiProvider.forListOrNull(images))
  }

  jsConfiguration.categories?.toCategoryNames()?.let {
    this.configuration.categoryNames = it
  }

  ////////////////////////////////////////////////////
  //Grid styles
  ////////////////////////////////////////////////////

  this.valueAxisGridLayer.configuration.applyLinesStyle(jsConfiguration.valuesGridStyle) { this.configuration.valueRange }
  jsConfiguration.valuesGridStyle?.visible?.let {
    this.configuration.showValuesGrid = it
  }

  this.categoryAxisGridLayer.data.applyLinesStyle(jsConfiguration.categoriesGridStyle)
  jsConfiguration.categoriesGridStyle?.visible?.let {
    this.configuration.showCategoryGrid = it
  }

  ////////////////////////////////////////////////////
  //Thresholds
  ////////////////////////////////////////////////////
  this.thresholdsSupport.applyThresholdStyles(jsConfiguration.thresholds, Unit)
  this.configuration.thresholdValues = jsConfiguration.thresholds.toThresholdValuesProvider()
  this.configuration.thresholdLabels = jsConfiguration.thresholds.toThresholdLabelsProvider()

  ////////////////////////////////////////////////////
  //Layout / Sizes
  ////////////////////////////////////////////////////
  jsConfiguration.minGapBetweenCategories?.let<@px Double, Unit> {
    this.configuration.minCategoryGap = it
  }
  jsConfiguration.maxGapBetweenCategories?.let<@px Double, Unit> {
    this.configuration.maxCategoryGap = it
  }
  jsConfiguration.barSize?.let<@px Double, Unit> { areaSize ->
    this.bulletChartPainter.configuration.barSize = areaSize
  }

  ////////////////////////////////////////////////////
  //Colors / Styles for bars and current value indicator
  ////////////////////////////////////////////////////
  jsConfiguration.currentValueIndicatorSize?.let<@px Double, Unit> { currentValueIndicatorSize ->
    this.bulletChartPainter.configuration.currentValueIndicatorSize = currentValueIndicatorSize
  }

  jsConfiguration.currentValueIndicatorColor?.toColor()?.let { color ->
    this.bulletChartPainter.configuration.currentValueColor = color
  }
  jsConfiguration.currentValueIndicatorOutlineColor?.toColor()?.let { color ->
    this.bulletChartPainter.configuration.currentValueOutlineColor = color
  }

  CategoryConverter.toCategoryColorProvider(jsConfiguration.barColors)?.let {
    this.bulletChartPainter.configuration.barColors = it
  }

  ////////////////////////////////////////////////////
  //Tooltips
  ////////////////////////////////////////////////////
  jsConfiguration.showTooltip?.let {
    this.configuration.showTooltips = it
  }
  jsConfiguration.tooltipStyle?.let { jsTooltipStyle ->
    jsTooltipStyle.tooltipFormat?.toNumberFormat()?.let {
      this.configuration.balloonTooltipValueLabelFormat = it
    }

    jsTooltipStyle.tooltipBoxStyle?.toModel()?.let {
      this.balloonTooltipLayer.tooltipPainter.configuration.boxStyle = it
    }

    jsTooltipStyle.tooltipBoxStyle?.color?.toColor()?.let {
      this.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration.labelColors = MultiProvider.always(it)
      this.balloonTooltipSupport.tooltipContentPaintable.headlinePaintable.configuration.labelColor = it.asProvider()
    }

    jsTooltipStyle.labelWidth?.let {
      this.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration.maxLabelWidth = it
    }

    jsTooltipStyle.entriesGap?.let {
      this.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration.entriesGap = it
    }

    jsTooltipStyle.symbolSizes?.toModelSizes()?.let {
      require(it.size == 2) {
        "Require exactly 2 symbol sizes: First for current value, second for area"
      }
      this.configuration.applyBalloonTooltipSizes(it[0], it[1])
    }

    jsTooltipStyle.symbolLabelGap?.let {
      this.balloonTooltipSupport.tooltipContentPaintable.delegate.configuration.symbolLabelGap = it
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
  jsConfiguration.activeCategoryBackgroundColor.toColor()?.let {
    this.categoryLayer.style.activeCategoryBackground = it
  }

  jsConfiguration.overflowIndicatorStyle?.let {
    this.bulletChartPainter.configuration.overflowIndicatorPainter?.applyStyle(it)
  }


  @px val viewportMarginTop = valueAxisSupport.calculateContentViewportMarginTop(Unit, chartSupport())
    .coerceAtLeast(categoryAxisSupport.calculateContentViewportMarginTop(Unit, chartSupport()))
  contentViewportMargin = contentViewportMargin.withTop(viewportMarginTop)

  //Apply the content viewport margin
  jsConfiguration.contentViewportMargin?.let {
    contentViewportMargin = contentViewportMargin.withValues(it)
  }
}

/**
 * Returns a multi provider that returns "" for all labels that do not exist
 */
fun Array<CategoryBulletChartData>.toCategoryNames(): MultiProvider<CategoryIndex, String> {
  val categoryLabels = this.map { categoryBulletChartData: CategoryBulletChartData ->
    categoryBulletChartData.label.orEmpty()
  }

  return MultiProvider.forListOr(categoryLabels, "")
}
