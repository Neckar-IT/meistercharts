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
package com.meistercharts.api.discrete

import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.zoom.UpdateReason
import com.meistercharts.axis.AxisSelection
import com.meistercharts.color.Color
import com.meistercharts.algorithms.painter.stripe.refentry.RectangleReferenceEntryStripePainter
import com.meistercharts.algorithms.painter.stripe.refentry.ReferenceEntryStatusColorProvider
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.api.DiscreteAxisStyle
import com.meistercharts.api.StripeStyle
import com.meistercharts.api.applyDiscreteAxisStyle
import com.meistercharts.api.applyTimeAxisStyle
import com.meistercharts.api.line.DiscreteDataSeriesConfiguration
import com.meistercharts.api.toColor
import com.meistercharts.api.toFontDescriptorFragment
import com.meistercharts.api.toHistoryEnum
import com.meistercharts.api.toModel
import com.meistercharts.api.toModelSizes
import com.meistercharts.api.toNumberFormat
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.charts.refs.DiscreteTimelineChartGestalt
import com.meistercharts.design.Theme
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDataSeriesIndexProvider
import com.meistercharts.history.historyConfiguration
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.open.charting.api.sanitizing.sanitize
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.getModuloOrNull
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.cached


private val logger = LoggerFactory.getLogger("com.meistercharts.api.discrete.DiscreteTimelineChartExtensions")

/**
 * Overwrites defaults set by the gestalt
 */
fun MeisterChartBuilder.applyDiscreteTimelineChartEasyApiDefaults() {
  zoomAndTranslationConfiguration {
    //ATTENTION: Copied from DiscreteTimelineChartGestalt#init
    translateAxisSelection = AxisSelection.X
    mouseWheelZoom = false //Disable zoom for Easy API
  }
}

fun DiscreteTimelineChartGestalt.applyEasyApiDefaults() {
  //configuration.applyAxisTitleOnTop(40.0)
  chartSupport().windowResizeBehavior = ResetToDefaultsOnWindowResize //always reset to the defaults, we do not have any live data
}

/**
 * Applies the configuration to the gestalt
 */
fun DiscreteTimelineChartGestalt.applyConfiguration(jsConfiguration: DiscreteTimelineChartConfiguration) {
  logger.debug("DiscreteTimelineChartGestalt.applyConfiguration", jsConfiguration)

  jsConfiguration.visibleDiscreteStripes?.let { jsVisibleStripes ->
    if (jsVisibleStripes.size == 1 && jsVisibleStripes[0] == -1) { //check for magic "-1" value
      configuration.showAllReferenceEntryDataSeries()
    } else {
      val visibleStripeIndices = jsVisibleStripes.map { ReferenceEntryDataSeriesIndex(it) }
      configuration.requestedVisibleReferenceEntrySeriesIndices = ReferenceEntryDataSeriesIndexProvider.forList(visibleStripeIndices)
    }
  }

  jsConfiguration.discreteAxisStyle?.let { jsDiscreteAxisStyle: DiscreteAxisStyle ->
    categoryAxisLayer.style.applyDiscreteAxisStyle(jsDiscreteAxisStyle)
  }

  jsConfiguration.timeAxisStyle?.let { jsTimeAxisStyle ->
    timeAxisLayer.style.applyTimeAxisStyle(jsTimeAxisStyle)
    //Recalculate the viewport margin - the time axis size might have changed
    recalculateContentViewportMargin()
  }


  jsConfiguration.discreteDataSeriesConfigurations?.let { dataSeriesConfigurations: Array<DiscreteDataSeriesConfiguration> ->

    //Create the stripe painters - one for each discrete data series
    val stripePainters: List<RectangleReferenceEntryStripePainter> = dataSeriesConfigurations.map { jsDiscreteDataSeriesConfiguration ->
      //Create the stripe painter for this data series
      RectangleReferenceEntryStripePainter {
        //The stripe styles for the ordinals
        jsDiscreteDataSeriesConfiguration.stripeStyles?.let { jsStripeStyles ->
          //TODO support other fill types, too

          //The fill colors for the different state ordinals
          val fillColors = jsStripeStyles.mapIndexed { index: Int, jsStripeStyle: StripeStyle? ->
            jsStripeStyle?.backgroundColor?.toColor() ?: Theme.enumColors().valueAt(index)
          }
          this.fillProvider = ReferenceEntryStatusColorProvider { _, _, statusEnumSet, _ ->
            val firstOrdinal = statusEnumSet.firstSetOrdinal()
            fillColors.getModuloOrNull(firstOrdinal.value) ?: Theme.enumColors().valueAt(firstOrdinal.value)
          }

          val labelColors = jsStripeStyles.map { jsStripeStyle: StripeStyle? ->
            jsStripeStyle?.labelColor?.toColor() ?: Color.white
          }
          labelColorProvider = { _, statusEnumSet, _ ->
            val firstOrdinal = statusEnumSet.firstSetOrdinal()
            labelColors.getModuloOrNull(firstOrdinal.value) ?: Color.white
          }
        }

        jsDiscreteDataSeriesConfiguration.stripeLabelFont?.toFontDescriptorFragment()?.let {
          labelFont = it
        }

        jsDiscreteDataSeriesConfiguration.stripeSegmentSeparatorColor?.toColor()?.let {
          separatorStroke = it
        }
        jsDiscreteDataSeriesConfiguration.stripeSegmentSeparatorWidth?.sanitize()?.let {
          separatorSize = it
        }

        jsDiscreteDataSeriesConfiguration.aggregationMode?.let {
          //TODO support aggregation mode somehow
          //aggregationMode = it.sanitize()
        }
      }
    }

    //Delegate to the stripe painters
    configuration.tooltipStatusColorProviders = MultiProvider { index ->
      stripePainters[index].configuration.fillProvider
    }

    referenceEntryStripePainters = MultiProvider.forListOr<ReferenceEntryDataSeriesIndex, RectangleReferenceEntryStripePainter>(values = stripePainters) { RectangleReferenceEntryStripePainter() }.cached()
  }

  jsConfiguration.discreteStripeHeight?.let {
    historyReferenceEntryLayer.configuration.stripeHeight = it
  }

  jsConfiguration.discreteStripeGap?.let {
    historyReferenceEntryLayer.configuration.stripesDistance = it
  }

  jsConfiguration.visibleTimeRange?.toModel()?.let { timeRange ->
    @DomainRelative val startDateRelative = configuration.contentAreaTimeRange.time2relative(timeRange.start)
    @DomainRelative val endDateRelative = configuration.contentAreaTimeRange.time2relative(timeRange.end)
    chartSupport().zoomAndTranslationSupport.fitX(startDateRelative, endDateRelative, reason = UpdateReason.ApiCall)
  }


  //Apply the history configuration
  jsConfiguration.discreteDataSeriesConfigurations?.toHistoryConfiguration()?.let { historyConfiguration ->
    configuration.historyConfiguration = historyConfiguration.asProvider()
    logger.debug {
      "Setting history configuration:\n${historyConfiguration.dump()}"
    }
  }


  jsConfiguration.tooltipStyle?.let { jsTooltipStyle ->
    jsTooltipStyle.tooltipFormat?.toNumberFormat()?.let {
      // this.configuration.balloonTooltipValueLabelFormat = it
    }

    jsTooltipStyle.tooltipBoxStyle?.toModel()?.let {
      this.balloonTooltipLayer.delegate.tooltipPainter.configuration.boxStyle = it
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

    jsTooltipStyle.symbolSizes?.toModelSizes()?.firstOrNull()?.let {
      balloonTooltipSupport.applyLegendSymbolSize(it)
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
}

/**
 * Creates the history configuration
 */
private fun Array<DiscreteDataSeriesConfiguration>.toHistoryConfiguration(): HistoryConfiguration {
  return historyConfiguration {
    fastForEach { jsDataSeriesConfiguration ->
      val id = jsDataSeriesConfiguration.id.sanitize()
      val name = jsDataSeriesConfiguration.name.sanitize()
      val statusEnum = jsDataSeriesConfiguration.statusEnumConfiguration?.toHistoryEnum()

      referenceEntryDataSeries(DataSeriesId(id), name, statusEnum)
    }
  }
}
