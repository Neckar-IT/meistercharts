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
import com.meistercharts.algorithms.layers.HistoryEnumLayer
import com.meistercharts.algorithms.painter.stripe.enums.RectangleEnumStripePainter
import com.meistercharts.algorithms.layers.axis.time.TimeAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.algorithms.tile.HistoryRenderPropertiesCalculatorLayer
import com.meistercharts.algorithms.tile.MaxDistanceSamplingPeriodCalculator
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
class HistoryEnumLayerDemoDescriptor : MeisterchartsDemoDescriptor<HistoryEnumLayerDemoDescriptor.HistoryEnumDemoConfig> {
  override val name: String = "History Enum Layer"
  override val category: DemoCategory = DemoCategory.Layers
  override val uuid: Uuid = Uuid.parse("0d72589b-28c7-471b-88c6-420b55f19c30")
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<HistoryEnumDemoConfig>> = listOf(
    PredefinedConfiguration(
      Uuid.parse("b605a53d-2538-4703-b33e-fa2a61aac981"),
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
      Uuid.parse("e65b14b7-f30a-4d3c-a8c5-a56b62694569"),
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


  override fun prepareDemo(configuration: PredefinedConfiguration<HistoryEnumDemoConfig>?): MeisterchartsDemo {
    requireNotNull(configuration) {
      "Config required"
    }

    val demoConfig = configuration.payload

    return MeisterchartsDemo(this) {
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

          val visibleIndices = EnumDataSeriesIndexProvider.forList(listOf(EnumDataSeriesIndex.zero, EnumDataSeriesIndex.one, EnumDataSeriesIndex.two, EnumDataSeriesIndex.three))
          val historyEnumLayer = HistoryEnumLayer(HistoryEnumLayer.Configuration(historyStorage, { historyConfiguration }, visibleIndices) { contentAreaTimeRange }) {
            this.stripePainters = MultiProvider.always(enumBarPainter)
          }

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())
          layers.addLayer(HistoryRenderPropertiesCalculatorLayer(MaxDistanceSamplingPeriodCalculator().withMinimum(samplingPeriod), DefaultHistoryGapCalculator()) { contentAreaTimeRange })
          layers.addLayer(historyEnumLayer)
          layers.addLayer(TimeAxisLayer(configuration = TimeAxisLayer.Configuration(contentAreaTimeRange)))

          declare {
            button("Move to now") {
              chartSupport.translateOverTime.translateTo(nowMillis(), reason = UpdateReason.UserInteraction) //translate once
            }
          }

          configurable("Enum Height", historyEnumLayer.configuration::stripeHeight) {
            max = 50.0
          }

          configurable("Layout Direction", historyEnumLayer.configuration::layoutDirection)

          section("Stripe")

          configurable("Distance", historyEnumLayer.configuration::stripesDistance) {
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
