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

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.impl.MoveDomainValueToLocation
import com.meistercharts.algorithms.layers.HistoryEnumLayer
import com.meistercharts.algorithms.painter.stripe.enums.RectangleEnumStripePainter
import com.meistercharts.algorithms.layers.TimeAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.algorithms.tile.HistoryRenderPropertiesCalculatorLayer
import com.meistercharts.algorithms.tile.MaxDistanceSamplingPeriodCalculator
import com.meistercharts.algorithms.tile.withMinimum
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.section
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndexProvider
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.generator.EnumValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.model.Zoom
import it.neckar.open.time.nowMillis
import it.neckar.open.provider.MultiProvider

/**
 *
 */
class HistoryEnumLayerDemo : ChartingDemoDescriptor<HistoryEnumLayerDemo.HistoryEnumDemoConfig> {
  override val name: String = "History Enum Layer"
  override val category: DemoCategory = DemoCategory.Layers

  override val predefinedConfigurations: List<PredefinedConfiguration<HistoryEnumDemoConfig>> = listOf(
    PredefinedConfiguration(
      object : HistoryEnumDemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis

        override fun historyChunkGenerator(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          return HistoryChunkGenerator(
            historyStorage = historyStorage,
            samplingPeriod = samplingPeriod,
            decimalValueGenerators = emptyList(),
            enumValueGenerators = List(3) {
              EnumValueGenerator.random()
            },
            referenceEntryGenerators = emptyList(),
          )
        }
      }, "Default"
    ),
    PredefinedConfiguration(
      object : HistoryEnumDemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis

        override fun historyChunkGenerator(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          return HistoryChunkGenerator(
            historyStorage = historyStorage,
            samplingPeriod = samplingPeriod,
            decimalValueGenerators = emptyList(),
            enumValueGenerators = List(3) {
              EnumValueGenerator.always(HistoryEnumSet.NoValue)
            },
            referenceEntryGenerators = emptyList(),
          )
        }
      }, "Only NoValue"
    )
  )


  override fun createDemo(configuration: PredefinedConfiguration<HistoryEnumDemoConfig>?): ChartingDemo {
    requireNotNull(configuration) {
      "Config required"
    }

    val demoConfig = configuration.payload

    return ChartingDemo {
      val enumBarPainter = RectangleEnumStripePainter()

      meistercharts {
        val contentAreaTimeRange = TimeRange.oneMinuteSinceReference

        val historyStorage = InMemoryHistoryStorage()
        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()

        val samplingPeriod = demoConfig.samplingPeriod

        val historyChunkGenerator = demoConfig.historyChunkGenerator(historyStorage)
        val historyConfiguration = historyChunkGenerator.historyConfiguration

        val historyStorageCache = HistoryStorageCache(historyStorage)
        historyStorageCache.scheduleForStore(historyChunkGenerator.forTimeRange(TimeRange.oneHourUntilNow())!!, samplingPeriod)

        zoomAndTranslationDefaults {
          MoveDomainValueToLocation(
            defaultZoomProvider = { _ -> Zoom(1.0, 0.8) },
            domainRelativeValueProvider = { contentAreaTimeRange.time2relative(nowMillis()) },
            targetLocationProvider = { chartCalculator -> chartCalculator.windowRelative2WindowX(0.8) }
          )
        }

        configure {
          layers.addClearBackground()

          chartSupport.onDispose(historyStorage)
          chartSupport.translateOverTime.contentAreaTimeRangeX = contentAreaTimeRange

          val visibleIndices = EnumDataSeriesIndexProvider.forList(listOf(EnumDataSeriesIndex.zero, EnumDataSeriesIndex.one, EnumDataSeriesIndex.two, EnumDataSeriesIndex.three))
          val historyEnumLayer = HistoryEnumLayer(HistoryEnumLayer.Configuration(historyStorage, { historyConfiguration }, visibleIndices) { contentAreaTimeRange }) {
            this.stripePainters = MultiProvider.always(enumBarPainter)
          }

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())
          layers.addLayer(HistoryRenderPropertiesCalculatorLayer(MaxDistanceSamplingPeriodCalculator().withMinimum(samplingPeriod), DefaultHistoryGapCalculator()) { contentAreaTimeRange })
          layers.addLayer(historyEnumLayer)
          layers.addLayer(TimeAxisLayer(data = TimeAxisLayer.Data(contentAreaTimeRange)))

          declare {
            button("Move to now") {
              chartSupport.translateOverTime.translateTo(nowMillis()) //translate once
            }
          }

          configurableDouble("Enum Height", historyEnumLayer.configuration::stripeHeight) {
            max = 50.0
          }

          configurableEnum("Layout Direction", historyEnumLayer.configuration::layoutDirection)

          section("Stripe")

          configurableDouble("Distance", historyEnumLayer.configuration::stripesDistance) {
            max = 100.0
          }
        }
      }
    }
  }


  interface HistoryEnumDemoConfig {
    val samplingPeriod: SamplingPeriod

    fun historyChunkGenerator(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator

  }
}
