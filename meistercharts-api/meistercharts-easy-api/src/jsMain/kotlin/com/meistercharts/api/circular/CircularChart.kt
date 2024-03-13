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
package com.meistercharts.api.circular

import com.meistercharts.api.MeisterChartsApiLegacy
import com.meistercharts.api.Size
import com.meistercharts.api.toModelSize
import com.meistercharts.charts.CircularChartGestalt
import com.meistercharts.color.Color
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.combineWith
import com.meistercharts.js.MeisterchartJS
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.MutableDoublesProvider
import it.neckar.open.unit.other.px

/**
 * The api towards the browser that supports the creation and manipulation of a circular chart
 */
@JsExport
class CircularChart internal constructor(
  internal val gestalt: CircularChartGestalt,
  meisterChart: MeisterchartJS,
) : MeisterChartsApiLegacy<CircularChartData, CircularChartStyle>(meisterChart) {

  init {
    require(gestalt.configuration.absoluteValuesProvider is MutableDoublesProvider) { "please provide a mutable model" }
    gestalt.applyEasyApiDefaults()
  }

  /**
   * Sets and replaces the data of the circular chart model
   */
  override fun setData(jsData: CircularChartData) {
    val valuesProvider = gestalt.configuration.absoluteValuesProvider as MutableDoublesProvider
    valuesProvider.clear()
    valuesProvider.addAll(CircularChartConverter.toValues(jsData))

    CircularChartConverter.toSegmentsColorProvider(jsData).apply {
      gestalt.circularChartLayer.configuration.segmentsColorProvider = this
    }
    gestalt.legendLayer.configuration.segmentsImageProvider = CircularChartConverter.toSegmentsImageProvider(jsData)
    gestalt.legendLayer.configuration.segmentsLabelProvider = CircularChartConverter.toSegmentsLabelProvider(jsData)

    markAsDirty()
  }

  override fun setStyle(jsStyle: CircularChartStyle) {
    gestalt.applyStyle(jsStyle)

    markAsDirty()
  }
}

private fun CircularChartGestalt.applyStyle(jsStyle: CircularChartStyle) {
  jsStyle.maxDiameter?.let {
    this.circularChartLayer.configuration.maxDiameter = it
  }

  jsStyle.outerCircleWidth?.let {
    this.circularChartLayer.configuration.outerCircleWidth = it
  }

  jsStyle.outerCircleValueGap?.let {
    this.circularChartLayer.configuration.outerCircleValueGapPixels(it)
  }

  jsStyle.innerCircleWidth?.let {
    this.circularChartLayer.configuration.innerCircleWidth = it
  }

  jsStyle.gapInnerOuter?.let {
    this.circularChartLayer.configuration.gapInnerOuter = it
  }

  jsStyle.innerCircleColor?.let {
    this.circularChartLayer.configuration.innerCircleColor = Color.web(it).asProvider()
  }

  jsStyle.legend?.let { circularChartLegendStyle ->
    circularChartLegendStyle.showCaption?.let {
      this.legendLayer.configuration.showCaption = it
    }
    circularChartLegendStyle.fontColor?.let {
      this.legendLayer.configuration.fontColor = Color.web(it).asProvider()
    }
    circularChartLegendStyle.fontSize?.let {
      this.legendLayer.configuration.font = this.legendLayer.configuration.font.combineWith(FontDescriptorFragment(it))
    }
    circularChartLegendStyle.iconSize?.let {
      this.legendLayer.configuration.paintableSize = it.toModelSize()
    }
  }
}

@JsExport
external interface CircularChartStyle {
  /**
   * The max diameter
   */
  val maxDiameter: @px Double?

  val outerCircleWidth: @px Double?

  val outerCircleValueGap: @px Double?

  val innerCircleWidth: @px Double?

  /**
   * The color of the inner circle
   */
  val innerCircleColor: String?

  /**
   * The gap between inner and outer circle
   */
  val gapInnerOuter: @px Double?

  /**
   * The legend style
   */
  val legend: CircularChartLegendStyle?
}

@JsExport
external interface CircularChartLegendStyle {
  val showCaption: Boolean?

  val fontColor: String?

  val fontSize: Double?

  /**
   * The size for the icons in the legend
   */
  val iconSize: Size?
}


/**
 * The data for the chart
 */
@JsExport
external interface CircularChartData {
  val segments: Array<CircularChartSegment>?
}

/**
 * The data for one segment
 */
@JsExport
external interface CircularChartSegment {
  val value: Double?

  val caption: String?

  val color: String?

  /**
   * The icon (id or URL) as string
   */
  val icon: String?
}
