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
package com.meistercharts.demo.descriptors.history

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.canvas.RoundingStrategy
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartWithToolbarGestalt
import com.meistercharts.charts.timeline.setUpDemo
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableIndices
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.demo.toList
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import kotlin.math.roundToInt

class TimeLineChartGestaltWithToolbarDemoDescriptor : MeisterchartsDemoDescriptor<TimelineChartGestaltDemoConfiguration> {
  override val uuid: Uuid = Uuid.parse("dad11db6-b946-4f59-a414-28dd852b7c20")
  override val name: String = "Time Line Chart with toolbar"
  override val category: DemoCategory = DemoCategory.Gestalt
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable


  override val predefinedConfigurations: List<PredefinedConfiguration<TimelineChartGestaltDemoConfiguration>> = listOf(
    PredefinedConfiguration(Uuid.parse("b4b1cab1-20a0-49c2-9de1-e9aa4779be5d"), {}, "empty"),
    PredefinedConfiguration(Uuid.parse("99c54643-d1cb-49f0-9b3c-1e120e6a7e89"), { this.setUpDemo(it) }, "demo setup"),
    TimeLineChartGestaltDemoDescriptor.oneSampleEvery100ms.copy(uuid = Uuid.parse("818119b3-fb0a-4477-8b22-c25acd55cf13")),
    TimeLineChartGestaltDemoDescriptor.oneSampleEvery16msCached500ms.copy(uuid = Uuid.parse("be734e48-e0cf-47a8-b609-e2bfede0ffbf")),
    TimeLineChartGestaltDemoDescriptor.neckarITHomePage.copy(uuid = Uuid.parse("171f5bdf-5d40-47bb-8ef8-9a77cd101589")),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<TimelineChartGestaltDemoConfiguration>?): MeisterchartsDemo {
    require(configuration != null) { "config required" }
    val gestaltConfig: TimelineChartGestaltDemoConfiguration = configuration.payload


    return MeisterchartsDemo(this) {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage().also {
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
        }

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()


        val gestalt = TimeLineChartWithToolbarGestalt(historyStorage)

        gestalt.configure(this)
        gestalt.timeLineChartGestalt.gestaltConfig(historyStorage)

        onDispose(historyStorage)

        configure {
          chartSupport.translateOverTime.animated = true

          configurable("Show toolbar", gestalt.configuration::showToolbar)
          configurable("Show Mouse Wheel Hint", gestalt.configuration::showMouseWheelModifierHint)

          configurableList(
            "Content area duration (sec)", (gestalt.timeLineChartGestalt.configuration.contentAreaDuration / 1000.0).roundToInt(), listOf(
              10, 60, 60 * 10, 60 * 60, 60 * 60 * 24
            )
          ) {
            onChange {
              gestalt.timeLineChartGestalt.configuration.contentAreaDuration = it * 1000.0
              markAsDirty()
            }
          }

          configurableDouble("Cross-wire location", gestalt.timeLineChartGestalt.configuration.crossWirePositionXProperty)

          configurableIndices(
            this@MeisterchartsDemo,
            "Visible lines",
            "line visible",
            gestalt.timeLineChartGestalt.configuration.requestedVisibleDecimalSeriesIndices.toList().map { it.value }
          ) {
            gestalt.timeLineChartGestalt.configuration.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.forList(it.map { DecimalDataSeriesIndex(it) })
          }

          configurableIndices(
            this@MeisterchartsDemo,
            "Visible value axes",
            "axis visible",
            initial = gestalt.timeLineChartGestalt.configuration.requestedVisibleValueAxesIndices.toList().map { it.value },
          ) {
            gestalt.timeLineChartGestalt.configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.forList(it.map { DecimalDataSeriesIndex(it) })
          }

          configurable("Time axis tick font", gestalt.timeLineChartGestalt.timeAxisLayer.configuration::tickFont) {
          }

          configurableListWithProperty("Refresh rate", chartSupport::targetRenderRate, TargetRefreshRate.predefined)
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

