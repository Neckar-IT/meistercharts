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
package com.meistercharts.charts

import com.meistercharts.model.ValueRange
import com.meistercharts.algorithms.layers.ThresholdsLayer
import com.meistercharts.algorithms.layers.ThresholdsLayer.ThresholdValues
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.canvas.MeisterChartsBuilderDsl
import com.meistercharts.provider.ValueRangeProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.DoublesProvider.Companion.forDoubles
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.delegate
import it.neckar.open.formatting.decimalFormat

/**
 * Configures a chart with thresholds
 *
 * Attention: You should probably use [ThresholdsSupport] instead.
 */
@Deprecated("Use ThresholdsHudGestalt instead")
@MeisterChartsBuilderDsl
class ThresholdsGestalt(
  /**
   * Provides the threshold values
   */
  thresholdValues: @Domain DoublesProvider = forDoubles(25.0, 50.0, 75.0),

  /**
   * Provides the threshold labels
   */
  thresholdLabels: MultiProvider<ThresholdValues, List<String>> = MultiProvider.invoke { index -> listOf(decimalFormat.format(thresholdValues.valueAt(index))) },

  /**
   * Provides the value range to convert domain values into domain relative values
   */
  valueRangeProvider: ValueRangeProvider = { ValueRange.default },

  styleConfiguration: Configuration.() -> Unit = {},
) : ChartGestalt {

  val configuration: Configuration = Configuration(
    thresholdValues, thresholdLabels, valueRangeProvider
  ).also(styleConfiguration)

  /**
   * Converts the threshold values to domain relative values
   */
  private val thresholdValuesDomainRelative: @DomainRelative DoublesProvider = object : DoublesProvider {
    override fun size(): Int {
      return configuration.thresholdValues.size()
    }

    override fun valueAt(index: Int): Double {
      @Domain val domainValue = configuration.thresholdValues.valueAt(index)
      return configuration.valueRangeProvider().toDomainRelative(domainValue)
    }
  }

  val thresholdsLayer: ThresholdsLayer = ThresholdsLayer(ThresholdsLayer.Data(this.thresholdValuesDomainRelative, configuration::thresholdLabels.delegate()))

  override fun configure(meisterChartBuilder: MeisterChartBuilder) {
    meisterChartBuilder.configure {
      layers.addLayer(this@ThresholdsGestalt.thresholdsLayer)
    }
  }

  inner class Configuration(
    /**
     * Provides the threshold values
     */
    var thresholdValues: @Domain DoublesProvider,

    /**
     * Provides the threshold labels
     */
    var thresholdLabels: MultiProvider<ThresholdValues, List<String>>,

    /**
     * Provides the value range to convert domain values into domain relative values
     */
    var valueRangeProvider: ValueRangeProvider,
  ) {
    /**
     * Changes the chart orientation to horizontal.
     * This method modifies the threshold layer to match the new orientation
     */
    fun applyHorizontalConfiguration() {
      thresholdsLayer.style.applyHorizontalConfiguration()
    }

    /**
     * Changes the chart orientation to vertical.
     * This method modifies the threshold layer to match the new orientation
     */
    fun applyVerticalConfiguration() {
      thresholdsLayer.style.applyVerticalConfiguration()
    }
  }
}
