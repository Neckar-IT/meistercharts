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
package com.meistercharts.demo.descriptors.history

import com.meistercharts.canvas.RoundingStrategy
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.charts.timeline.TimeLineChartWithToolbarGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableIndices
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.demo.toList
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import kotlin.math.roundToInt

class TimeLineChartGestaltWithToolbarDemoDescriptor : ChartingDemoDescriptor<TimeLineChartGestalt.() -> Unit> {
  override val name: String = "Time Line Chart with toolbar"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val predefinedConfigurations: List<PredefinedConfiguration<TimeLineChartGestalt.() -> Unit>> = listOf(
    PredefinedConfiguration({}, "empty"),
    TimeLineChartGestaltDemoDescriptor.oneSampleEvery100ms,
    TimeLineChartGestaltDemoDescriptor.oneSampleEvery16msCached500ms,
    TimeLineChartGestaltDemoDescriptor.neckarITHomePage,
  )

  override fun createDemo(configuration: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit>?): ChartingDemo {
    require(configuration != null) { "config required" }
    val gestaltConfig: TimeLineChartGestalt.() -> Unit = configuration.payload


    return ChartingDemo {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage().also {
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
        }

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()


        val gestalt = TimeLineChartWithToolbarGestalt(chartId, historyStorage)

        gestalt.configure(this)
        gestalt.timeLineChartGestalt.gestaltConfig()

        onDispose(historyStorage)

        configure {
          chartSupport.translateOverTime.animated = true

          configurableBoolean("Show toolbar", gestalt.style::showToolbar)

          configurableList(
            "Content area duration (sec)", (gestalt.timeLineChartGestalt.style.contentAreaDuration / 1000.0).roundToInt(), listOf(
              10, 60, 60 * 10, 60 * 60, 60 * 60 * 24
            )
          ) {
            onChange {
              gestalt.timeLineChartGestalt.style.contentAreaDuration = it * 1000.0
              markAsDirty()
            }
          }

          configurableDouble("Cross-wire location", gestalt.timeLineChartGestalt.style.crossWirePositionXProperty)

          configurableIndices(
            this@ChartingDemo,
            "Visible lines",
            "line visible",
            gestalt.timeLineChartGestalt.style.requestedVisibleDecimalSeriesIndices.toList().map { it.value }
          ) {
            gestalt.timeLineChartGestalt.style.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.forList(it.map { DecimalDataSeriesIndex(it) })
          }

          configurableIndices(
            this@ChartingDemo,
            "Visible value axes",
            "axis visible",
            initial = gestalt.timeLineChartGestalt.style.requestedVisibleValueAxesIndices.toList().map { it.value },
          ) {
            gestalt.timeLineChartGestalt.style.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.forList(it.map { DecimalDataSeriesIndex(it) })
          }

          configurableFont("Time axis tick font", gestalt.timeLineChartGestalt.timeAxisLayer.style::tickFont) {
          }

          configurableListWithProperty("Refresh rate", chartSupport::targetRefreshRate, TargetRefreshRate.predefined)
          configurableListWithProperty("Translation Anim Rounding", chartSupport.translateOverTime::roundingStrategy, RoundingStrategy.predefined) {
            converter {
              when (it) {
                RoundingStrategy.exact -> "exact"
                RoundingStrategy.round -> "1 px"
                RoundingStrategy.half -> "0.5 px"
                RoundingStrategy.quarter -> "0.25 px"
                RoundingStrategy.tenth -> "0.1 px"
                else -> it.toString()
              }
            }
          }
        }
      }
    }
  }

}

