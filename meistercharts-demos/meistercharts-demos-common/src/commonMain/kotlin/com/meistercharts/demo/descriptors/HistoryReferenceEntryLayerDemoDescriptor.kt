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
import com.meistercharts.time.TimeRange
import it.neckar.open.kotlin.lang.requireNotNull
import com.meistercharts.zoom.UpdateReason
import com.meistercharts.zoom.MoveDomainValueToLocation
import com.meistercharts.algorithms.layers.HistoryReferenceEntryLayer
import com.meistercharts.algorithms.layers.axis.time.TimeAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.painter.stripe.refentry.RectangleReferenceEntryStripePainter
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.algorithms.tile.HistoryRenderPropertiesCalculatorLayer
import com.meistercharts.algorithms.tile.MinDistanceSamplingPeriodCalculator
import com.meistercharts.algorithms.tile.withMinimum
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.section
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.ReferenceEntryDataSeriesIndexProvider
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.ReferenceEntryGenerator
import com.meistercharts.model.Zoom
import it.neckar.open.provider.MultiProvider
import it.neckar.open.time.nowMillis
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 *
 */
class HistoryReferenceEntryLayerDemoDescriptor : MeisterchartsDemoDescriptor<HistoryReferenceEntryLayerDemoDescriptor.HistoryEnumDemoConfig> {
  override val name: String = "History Reference Entry Layer"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val uuid: Uuid = Uuid.parse("1af6f666-5eaa-4e46-9cfc-8a6e4e399a8b")

  override val predefinedConfigurations: List<PredefinedConfiguration<HistoryEnumDemoConfig>> = listOf(
    PredefinedConfiguration(
      Uuid.parse("d42ced17-260a-4706-909f-12854ada885d"),
      object : HistoryEnumDemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis

        override fun historyChunkGenerator(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          return HistoryChunkGenerator(
            historyStorage = historyStorage,
            samplingPeriod = samplingPeriod,
            decimalValueGenerators = emptyList(),
            enumValueGenerators = emptyList(),
            referenceEntryGenerators = List(3) {
              ReferenceEntryGenerator.random()
            },
          )
        }
      }, "Default"
    ),
    PredefinedConfiguration(
      Uuid.parse("b978fc6d-da4c-48a1-b443-17eaed28edad"),
      object : HistoryEnumDemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis

        override fun historyChunkGenerator(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          return HistoryChunkGenerator(
            historyStorage = historyStorage,
            samplingPeriod = samplingPeriod,
            decimalValueGenerators = emptyList(),
            enumValueGenerators = emptyList(),
            referenceEntryGenerators = List(3) {
              ReferenceEntryGenerator.increasing(500.milliseconds)
            },
          )
        }
      }, "Increasing (every 500 ms)"
    ),
    PredefinedConfiguration(
      Uuid.parse("b9449736-e811-491e-8110-b58d1fedd30a"),
      object : HistoryEnumDemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis

        override fun historyChunkGenerator(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          return HistoryChunkGenerator(
            historyStorage = historyStorage,
            samplingPeriod = samplingPeriod,
            decimalValueGenerators = emptyList(),
            enumValueGenerators = emptyList(),
            referenceEntryGenerators = List(3) {
              ReferenceEntryGenerator.increasing(45.seconds)
            },
          )
        }
      }, "Increasing (every 45 s)"
    ),
    PredefinedConfiguration(
      Uuid.parse("bfff9fb8-a9c7-42c9-84c0-28d50642e3ab"),
      object : HistoryEnumDemoConfig {
        override val samplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis

        override fun historyChunkGenerator(historyStorage: InMemoryHistoryStorage): HistoryChunkGenerator {
          return HistoryChunkGenerator(
            historyStorage = historyStorage,
            samplingPeriod = samplingPeriod,
            decimalValueGenerators = emptyList(),
            enumValueGenerators = emptyList(),
            referenceEntryGenerators = List(3) {
              ReferenceEntryGenerator.always(ReferenceEntryId.NoValue)
            },
          )
        }
      }, "Only NoValue"
    ),
  )


  override fun prepareDemo(configuration: PredefinedConfiguration<HistoryEnumDemoConfig>?): MeisterchartsDemo {
    requireNotNull(configuration) {
      "Config required"
    }

    val demoConfig = configuration.payload

    return MeisterchartsDemo(this) {
      val enumBarPainter = RectangleReferenceEntryStripePainter()

      meistercharts {
        val contentAreaTimeRange = TimeRange.oneMinuteSinceReference

        val historyStorage = InMemoryHistoryStorage()
        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()

        val samplingPeriod = demoConfig.samplingPeriod

        val historyChunkGenerator = demoConfig.historyChunkGenerator(historyStorage)
        val historyConfiguration = historyChunkGenerator.historyConfiguration

        val historyStorageCache = HistoryStorageCache(historyStorage)
        historyStorageCache.scheduleForStore(historyChunkGenerator.forTimeRange(TimeRange.oneHourUntilNow()).requireNotNull { "historyChunkGenerator returned null for one-hour time range" }, samplingPeriod)

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

          val visibleIndices = ReferenceEntryDataSeriesIndexProvider.indices { historyConfiguration.referenceEntryDataSeriesCount.value }

          val layer = HistoryReferenceEntryLayer(
            configuration = HistoryReferenceEntryLayer.Configuration(
              historyStorage = historyStorage,
              historyConfiguration = { historyConfiguration },
              requestedVisibleIndices = visibleIndices
            ) { contentAreaTimeRange }
          ) {
            this.stripePainters = MultiProvider.always(enumBarPainter)
          }

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())
          layers.addLayer(HistoryRenderPropertiesCalculatorLayer(MinDistanceSamplingPeriodCalculator(20.0).withMinimum(samplingPeriod), DefaultHistoryGapCalculator()) { contentAreaTimeRange })
          layers.addLayer(layer)
          layers.addLayer(TimeAxisLayer(configuration = TimeAxisLayer.Configuration(contentAreaTimeRange)))

          declare {
            button("Move to now") {
              chartSupport.translateOverTime.translateTo(nowMillis(), reason = UpdateReason.UserInteraction) //translate once
            }
          }

          configurable("Enum Height", layer.configuration::stripeHeight) {
            max = 50.0
          }

          configurable("Label font", enumBarPainter.configuration::labelFont)

          configurable("Layout Direction", layer.configuration::layoutDirection)

          section("Stripe")

          configurable("Distance", layer.configuration::stripesDistance) {
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
