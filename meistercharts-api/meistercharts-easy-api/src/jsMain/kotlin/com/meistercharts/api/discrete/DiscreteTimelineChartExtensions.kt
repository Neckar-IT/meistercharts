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

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.painter.stripe.refentry.RectangleReferenceEntryStripePainter
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.api.DiscreteAxisStyle
import com.meistercharts.api.StripeStyle
import com.meistercharts.api.applyDiscreteAxisStyle
import com.meistercharts.api.applyTimeAxisStyle
import com.meistercharts.api.line.DiscreteDataSeriesConfiguration
import com.meistercharts.api.toColor
import com.meistercharts.api.toHistoryEnum
import com.meistercharts.api.toModel
import com.meistercharts.charts.refs.DiscreteTimelineChartGestalt
import com.meistercharts.design.Theme
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDataSeriesIndexProvider
import com.meistercharts.history.historyConfiguration
import it.neckar.commons.kotlin.js.debug
import it.neckar.logging.LoggerFactory
import it.neckar.logging.ifDebug
import it.neckar.open.charting.api.sanitizing.sanitize
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.MultiProvider


private val logger = LoggerFactory.getLogger("com.meistercharts.api.discrete.DiscreteTimelineChartExtensions")


fun DiscreteTimelineChartGestalt.applySickDefaults() {
  //configuration.applyAxisTitleOnTop(40.0)
  chartSupport().windowResizeBehavior = ResetToDefaultsOnWindowResize //always reset to the defaults, we do not have any live data
}

/**
 * Applies the configuration to the gestalt
 */
fun DiscreteTimelineChartGestalt.applyConfiguration(jsConfiguration: DiscreteTimelineChartConfiguration) {
  logger.ifDebug {
    console.debug("DiscreteTimelineChartGestalt.applyConfiguration", jsConfiguration)
  }

  jsConfiguration.visibleDiscreteStripes?.let { jsVisibleStripes ->
    if (jsVisibleStripes.size == 1 && jsVisibleStripes[0] == -1) { //check for magic "-1" value
      historyReferenceEntryLayer.configuration.showAllReferenceEntryDataSeries()
    } else {
      val map = jsVisibleStripes.map { ReferenceEntryDataSeriesIndex(it) }
      historyReferenceEntryLayer.configuration.requestedVisibleReferenceEntryDataSeriesIndices = ReferenceEntryDataSeriesIndexProvider.forList(map.toList())
    }
  }

  jsConfiguration.discreteAxisStyle?.let { jsDiscreteAxisStyle: DiscreteAxisStyle ->
    categoryAxisLayer.style.applyDiscreteAxisStyle(jsDiscreteAxisStyle)
  }

  jsConfiguration.timeAxisStyle?.let { jsTimeAxisStyle ->
    this.timeAxisLayer.style.applyTimeAxisStyle(jsTimeAxisStyle)

    //Apply the size of the axis at the gestalt, too.
    //This is necessary to update clipping etc.
    jsTimeAxisStyle.axisSize?.let {
      //this.configuration.timeAxisSize = it //TODO
    }
  }

  jsConfiguration.discreteDataSeriesConfigurations?.let { jsConfigurations ->
    val stripePainters = jsConfigurations.map { jsConfiguration ->
      RectangleReferenceEntryStripePainter {

        jsConfiguration.stripeStyles?.let { jsStripeStyles ->
          //TODO support other fill types, too

          val fillColors = jsStripeStyles.mapIndexed { index: Int, jsStripeStyle: StripeStyle? ->
            jsStripeStyle?.backgroundColor?.toColor() ?: Theme.enumColors().valueAt(index)
          }

          fillProvider = { value, statusEnumSet, historyConfiguration ->
            //TOOD use index instead?
            fillColors.getOrNull(value.id) ?: Theme.enumColors().valueAt(value.id)
          }
        }

        jsConfiguration.aggregationMode?.let {
          //aggregationMode = it.sanitize()
        }
      }
    }

    historyReferenceEntryLayer.configuration.stripePainters = MultiProvider.forListOr(stripePainters, RectangleReferenceEntryStripePainter())
  }

  jsConfiguration.discreteStripeHeight?.let {
    historyReferenceEntryLayer.configuration.stripeHeight = it
  }

  jsConfiguration.discreteStripeGap?.let {
    historyReferenceEntryLayer.configuration.stripesDistance = it
  }

  //jsConfiguration.discreteDataSeriesConfigurations?.let {
  //  it.first().name
  //  val aggregationMode = it.first().aggregationMode
  //  it.first().statusEnumConfiguration
  //  it.first().stripeStyles
  //}
  //
  //(this.categoryAxisLayer.style.axisLabelPainter as DefaultCategoryAxisLabelPainter).let { painter ->
  //  painter.style.wrapMode =
  //}

  jsConfiguration.visibleTimeRange?.toModel()?.let { timeRange ->
    @DomainRelative val startDateRelative = configuration.contentAreaTimeRange.time2relative(timeRange.start)
    @DomainRelative val endDateRelative = configuration.contentAreaTimeRange.time2relative(timeRange.end)
    chartSupport().zoomAndTranslationSupport.fitX(startDateRelative, endDateRelative)
  }

  jsConfiguration.visibleDiscreteStripes?.let { jsVisibleStripes ->
    if (jsVisibleStripes.size == 1 && jsVisibleStripes[0] == -1) { //check for magic "-1" value
      this.configuration.showAllReferenceEntrySeries()
    } else {
      val map = jsVisibleStripes.map { ReferenceEntryDataSeriesIndex(it) }
      this.configuration.requestVisibleReferenceEntrySeriesIndices = ReferenceEntryDataSeriesIndexProvider.forList(map.toList())
    }
  }


  //Apply the history configuration
  jsConfiguration.discreteDataSeriesConfigurations?.toHistoryConfiguration()?.let { historyConfiguration ->
    this.configuration.historyConfiguration = historyConfiguration.asProvider()
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
      val statusEnum = jsDataSeriesConfiguration.statusEnumConfiguration.toHistoryEnum()

      referenceEntryDataSeries(DataSeriesId(id), name, statusEnum)
    }
  }
}
