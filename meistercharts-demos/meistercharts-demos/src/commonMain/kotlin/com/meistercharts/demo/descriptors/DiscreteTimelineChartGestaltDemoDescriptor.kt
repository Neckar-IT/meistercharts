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
package com.meistercharts.demo.descriptors

import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.refs.DiscreteTimelineChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorNullable
import com.meistercharts.demo.configurableIndices
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.toList
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDataSeriesIndexProvider
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.ReferenceEntryGenerator
import kotlin.time.Duration.Companion.milliseconds

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class DiscreteTimelineChartGestaltDemoDescriptor : ChartingDemoDescriptor<DiscreteTimelineChartGestaltDemoDescriptor.DemoConfig> {
  override val name: String = "Reference Entries Gestalt"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Gestalt

  override val predefinedConfigurations: List<PredefinedConfiguration<DemoConfig>> = listOf(
    PredefinedConfiguration(
      object : DemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EverySecond

        override fun historyChunkGenerator(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          return HistoryChunkGenerator(
            historyStorage = historyStorage,
            samplingPeriod = samplingPeriod,
            decimalValueGenerators = emptyList(),
            enumValueGenerators = emptyList(),
            referenceEntryGenerators = List(12) {
              ReferenceEntryGenerator.random()
            },
          )
        }
      }, "Random - 100ms"
    ),

    PredefinedConfiguration(
      object : DemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EverySecond

        override fun historyChunkGenerator(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          return HistoryChunkGenerator(
            historyStorage = historyStorage,
            samplingPeriod = samplingPeriod,
            decimalValueGenerators = emptyList(),
            enumValueGenerators = emptyList(),
            referenceEntryGenerators = List(12) {
              ReferenceEntryGenerator.increasing(513.milliseconds)

            },
          )
        }
      }, "Increasing (every 513 ms)"
    ),

    PredefinedConfiguration(
      object : DemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EverySecond

        override fun historyChunkGenerator(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          return HistoryChunkGenerator(
            historyStorage = historyStorage,
            samplingPeriod = samplingPeriod,
            decimalValueGenerators = emptyList(),
            enumValueGenerators = emptyList(),
            referenceEntryGenerators = List(12) {
              ReferenceEntryGenerator.increasing(46_437.milliseconds)

            },
          )
        }
      }, "Increasing (every 46.437 s)"
    ),
  )

  override fun createDemo(configuration: PredefinedConfiguration<DemoConfig>?): ChartingDemo {
    val payload = requireNotNull(configuration?.payload)

    return ChartingDemo {
      meistercharts {

        val historyStorage = InMemoryHistoryStorage()
        onDispose(historyStorage)

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()


        val historyChunkGenerator = payload.historyChunkGenerator(historyStorage)
        val historyConfiguration = historyChunkGenerator.historyConfiguration

        it.neckar.open.time.repeat(payload.samplingPeriod.distance.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorage.storeWithoutCache(it, payload.samplingPeriod)
          }
        }.also {
          onDispose(it)
        }

        val gestalt = DiscreteTimelineChartGestalt(historyStorage, {
          historyConfiguration
        })
        gestalt.configure(this@meistercharts)

        //Set the zoom level to show the recording duration
        gestalt.configuration.contentAreaDuration = payload.samplingPeriod.distance * 20
        gestalt.configuration.minimumSamplingPeriod = payload.samplingPeriod

        val referenceEntryDataSeriesCount = gestalt.configuration.historyConfiguration().referenceEntryDataSeriesCount

        configure {
          configurableBoolean("Play Mode", chartSupport.translateOverTime::animated) {
            value = true
          }

          configurableBoolean("ShowTimeAxis", gestalt.configuration::showTimeAxis) {
          }

          configurableIndices(
            this@ChartingDemo,
            "Visible Data Series",
            "data series visible",
            initial = gestalt.configuration.requestVisibleReferenceEntrySeriesIndices.toList().map { it.value },
            maxSize = referenceEntryDataSeriesCount,
          ) {
            gestalt.configuration.requestVisibleReferenceEntrySeriesIndices = ReferenceEntryDataSeriesIndexProvider.forList(it.map { ReferenceEntryDataSeriesIndex(it) })
          }

          configurableColorNullable("Background", gestalt.historyReferenceEntryLayer.configuration::background)

          configurableInsetsSeparate("Viewport", gestalt::contentViewportMargin)
        }
      }
    }
  }

  interface DemoConfig {
    val samplingPeriod: SamplingPeriod

    fun historyChunkGenerator(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator

  }
}
