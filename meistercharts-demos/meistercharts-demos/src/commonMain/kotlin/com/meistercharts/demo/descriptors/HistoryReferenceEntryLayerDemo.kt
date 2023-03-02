package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.impl.MoveDomainValueToLocation
import com.meistercharts.algorithms.layers.HistoryReferenceEntryLayer
import com.meistercharts.algorithms.layers.TimeAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.painter.stripe.refentry.RectangleReferenceEntryStripePainter
import com.meistercharts.algorithms.painter.stripe.refentry.ReferenceEntryStripePainter
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.algorithms.tile.HistoryRenderPropertiesCalculatorLayer
import com.meistercharts.algorithms.tile.MinDistanceSamplingPeriodCalculator
import com.meistercharts.algorithms.tile.withMinimum
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.section
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDataSeriesIndexProvider
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.ReferenceEntryGenerator
import com.meistercharts.model.Zoom
import it.neckar.open.time.nowMillis
import it.neckar.open.provider.MultiProvider
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 *
 */
class HistoryReferenceEntryLayerDemo : ChartingDemoDescriptor<HistoryReferenceEntryLayerDemo.HistoryEnumDemoConfig> {
  override val name: String = "History Reference Entry Layer"
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
            enumValueGenerators = emptyList(),
            referenceEntryGenerators = List(3) {
              ReferenceEntryGenerator.random()
            },
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
            enumValueGenerators = emptyList(),
            referenceEntryGenerators = List(3) {
              ReferenceEntryGenerator.increasing(500.milliseconds)
            },
          )
        }
      }, "Increasing (every 500 ms)"
    ),
    PredefinedConfiguration(
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


  override fun createDemo(configuration: PredefinedConfiguration<HistoryEnumDemoConfig>?): ChartingDemo {
    requireNotNull(configuration) {
      "Config required"
    }

    val demoConfig = configuration.payload

    return ChartingDemo {
      val enumBarPainter: ReferenceEntryStripePainter = RectangleReferenceEntryStripePainter()

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

          val visibleIndices = ReferenceEntryDataSeriesIndexProvider.forList(listOf(ReferenceEntryDataSeriesIndex.zero, ReferenceEntryDataSeriesIndex.one, ReferenceEntryDataSeriesIndex.two, ReferenceEntryDataSeriesIndex.three))
          val layer = HistoryReferenceEntryLayer(HistoryReferenceEntryLayer.Configuration(historyStorage, { historyConfiguration }, visibleIndices) { contentAreaTimeRange }) {
            this.stripePainter = MultiProvider.always(enumBarPainter)
          }

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())
          layers.addLayer(HistoryRenderPropertiesCalculatorLayer(MinDistanceSamplingPeriodCalculator(20.0).withMinimum(samplingPeriod), DefaultHistoryGapCalculator()) { contentAreaTimeRange })
          layers.addLayer(layer)
          layers.addLayer(TimeAxisLayer(data = TimeAxisLayer.Data(contentAreaTimeRange)))

          declare {
            button("Move to now") {
              chartSupport.translateOverTime.translateTo(nowMillis()) //translate once
            }
          }

          configurableDouble("Enum Height", layer.configuration::stripeHeight) {
            max = 50.0
          }

          configurableEnum("Layout Direction", layer.configuration::layoutDirection)

          section("Stripe")

          configurableDouble("Distance", layer.configuration::stripesDistance) {
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
