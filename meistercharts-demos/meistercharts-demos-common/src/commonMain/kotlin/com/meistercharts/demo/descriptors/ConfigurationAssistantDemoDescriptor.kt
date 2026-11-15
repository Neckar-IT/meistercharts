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
package com.meistercharts.demo.descriptors

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.linechart.PointStyle
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.ConfigurationAssistant
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.descriptors.history.setUpHistoryChunkGenerator
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.painter.PointPainter
import com.meistercharts.painter.PointStylePainter
import com.meistercharts.time.TimeRange
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.cached
import it.neckar.open.time.nowMillis
import it.neckar.open.time.repeat
import kotlin.time.Duration.Companion.milliseconds

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class ConfigurationAssistantDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("659a44e4-2cac-414e-a5c5-7307e89d3f2f")

  override val name: String = "Configuration Assistant"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Support
  override val variabilityType: VariabilityType = VariabilityType.Stable
  override val quality: DemoQuality = DemoQuality.High

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage()
        onDispose(historyStorage)

        historyStorage.scheduleCleanupService()
        historyStorage.scheduleDownSampling()

        val timelineChartGestalt = TimeLineChartGestalt(initialHistoryStorage = historyStorage)
        timelineChartGestalt.configuration.pointPainters = MultiProvider<DecimalDataSeriesIndex, PointPainter?> {
          PointStylePainter(PointStyle.Dot, 2.0, snapXValues = false, snapYValues = false).apply {
            color = timelineChartGestalt.configuration.lineStyles.valueAt(it).color
          }
        }.cached()

        val assistant = ConfigurationAssistant.withDataPointCountPerMinute(6.0)
        assistant.useDots()

        timelineChartGestalt.createDataForDemo(historyStorage, assistant.calculator.recordingSamplingPeriod)
        timelineChartGestalt.configure(this)

        val calculatorValues = object {
          var minDistanceBetweenDataPoints: Double = assistant.calculator.minDistanceBetweenSamples
          var maxDistanceBetweenDataPoints: Double = assistant.calculator.maxDistanceBetweenSamples
          var idealDistanceBetweenDataPoints: Double = assistant.calculator.idealDistanceBetweenSamples
        }

        configure {

          configurable("Play Mode", chartSupport.translateOverTime::animated) {
            value = true
          }

          configurable("Duration Between Recorded Data Points", assistant.calculator::durationBetweenSamples) {
            max = 1_000_000.0

            onChange {
              assistant.applyToGestalt(timelineChartGestalt)
            }
          }

          configurable("Gap Factor", assistant.calculator::gapFactor) {
            max = 50.0

            onChange {
              assistant.applyToGestalt(timelineChartGestalt)
            }
          }

          configurable("Min Distance Between Data Points", calculatorValues::minDistanceBetweenDataPoints) {
            max = 100.0

            onChange {
              assistant.setMinDistanceBetweenDataPoints(value)
              assistant.applyToGestalt(timelineChartGestalt)
            }
          }

          configurable("Max Distance Between Data Points", calculatorValues::maxDistanceBetweenDataPoints) {
            max = 100.0

            onChange {
              assistant.setMaxDistanceBetweenDataPoints(value)
              assistant.applyToGestalt(timelineChartGestalt)
            }
          }

          configurable("Ideal Distance Between Data Points", calculatorValues::idealDistanceBetweenDataPoints) {
            max = 100.0

            onChange {
              assistant.setIdealDistanceBetweenSamples(value)
              assistant.applyToGestalt(timelineChartGestalt)
            }
          }

          //configurable("Manual Min Zoom", assistant.calculator::manualMinZoom, 0.00001) {
          //  max = 100.0
          //
          //  onChange {
          //    assistant.applyToGestalt(timelineChartGestalt)
          //  }
          //}
          //
          //configurable("Manual Max Zoom", assistant.calculator::manualMaxZoom, 12.5) {
          //  max = 100.0
          //
          //  onChange {
          //    assistant.applyToGestalt(timelineChartGestalt)
          //  }
          //}

        }

      }
    }
  }

  private fun TimeLineChartGestalt.createDataForDemo(historyStorage: InMemoryHistoryStorage, samplingPeriod: SamplingPeriod) {
    val historyChunkGenerator = this.setUpHistoryChunkGenerator(historyStorage, samplingPeriod)
    val writableHistoryStorage = this.configuration.historyStorage as InMemoryHistoryStorage

    //adjust the position of the cross wire
    this.configuration.crossWirePositionX = 0.85

    //add some samples for the last hour and set the max history size accordingly
    val duration = (samplingPeriod.distance * 1_000.0).milliseconds
    historyChunkGenerator.forTimeRange(TimeRange.fromEndAndDuration(nowMillis(), duration))?.let {
      writableHistoryStorage.storeWithoutCache(it, samplingPeriod)
    }

    repeat(100.milliseconds) {
      historyChunkGenerator.next()?.let {
        writableHistoryStorage.storeWithoutCache(it, samplingPeriod)
      }
    }.also {
      onDispose(it)
    }

    //we want three lines and three value axes to be visible
    this.configuration.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { 3 }
    this.configuration.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 3 }
  }
}
