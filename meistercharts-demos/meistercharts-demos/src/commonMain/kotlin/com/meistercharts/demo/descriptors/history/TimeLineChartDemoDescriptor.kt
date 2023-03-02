package com.meistercharts.demo.descriptors.history

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.crosswire.CrossWireLayer
import com.meistercharts.algorithms.layers.debug.ShowTimeRangeLayer
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.canvas.RoundingStrategy
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.generator.DecimalValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.offset
import com.meistercharts.history.generator.scaled
import it.neckar.open.kotlin.lang.random
import it.neckar.open.time.nowMillis
import it.neckar.open.provider.MultiProvider
import it.neckar.open.observable.ObservableBoolean
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Palette.getChartColor
import it.neckar.open.time.repeat
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

class TimeLineChartDemoDescriptor : ChartingDemoDescriptor<SamplingPeriod> {
  override val name: String = "Time Line Chart"
  override val category: DemoCategory = DemoCategory.ShowCase

  override val predefinedConfigurations: List<PredefinedConfiguration<SamplingPeriod>> = listOf(
    PredefinedConfiguration(SamplingPeriod.EveryHundredMillis),
    PredefinedConfiguration(SamplingPeriod.Every24Hours),
  )

  override fun createDemo(configuration: PredefinedConfiguration<SamplingPeriod>?): ChartingDemo {
    require(configuration != null) { "configuration must not be null" }

    val samplingPeriod: SamplingPeriod = configuration.payload

    return ChartingDemo {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage()
        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()

        val dataSeriesCount = 8
        val valueRanges = listOf(
          ValueRange.linear(PI - 0.1, PI + 0.1),
          ValueRange.linear(0.0, 10.0),
          ValueRange.linear(-10.0, 10.0),
          ValueRange.linear(0.0, 100.0),
          ValueRange.linear(0.0, 100.0),
          ValueRange.linear(0.0, 100.0),
          ValueRange.linear(0.0, 100.0),
          ValueRange.linear(0.0, 100.0)
        )

        val decimalValueGenerators = mutableListOf(
          DecimalValueGenerator.always(PI),
          DecimalValueGenerator.sine(valueRanges[1]),
          DecimalValueGenerator.cosine(valueRanges[2]),
          DecimalValueGenerator.normality(valueRanges[3]).offset(40.0),
          DecimalValueGenerator.normality(valueRanges[4], valueRanges[4].delta * 0.05).scaled(1.2, 20.0),
          DecimalValueGenerator.normality(valueRanges[5], valueRanges[5].delta * 0.075),
          DecimalValueGenerator.normality(valueRanges[6], valueRanges[6].delta * 0.005).offset(-20.0),
          DecimalValueGenerator.normality(valueRanges[7]).offset(-40.0)
        )

        val historyChunkGenerator = HistoryChunkGenerator(historyStorage = historyStorage, samplingPeriod = samplingPeriod, decimalValueGenerators = decimalValueGenerators, enumValueGenerators = emptyList(), referenceEntryGenerators = emptyList())
        val historyConfiguration = historyChunkGenerator.historyConfiguration


        // fill history with 50 samples
        historyChunkGenerator.forTimeRange(TimeRange.fromEndAndDuration(nowMillis(), samplingPeriod.distance * 50))?.let {
          historyStorage.storeWithoutCache(it, samplingPeriod)
        }

        val colors = IntRange(0, dataSeriesCount - 1).map { getChartColor(it) }

        val gestalt = TimeLineChartGestalt(chartId, data = TimeLineChartGestalt.Data(historyStorage, historyConfiguration)) {
          lineValueRanges = MultiProvider.forListModulo(valueRanges)
          lineStyles = MultiProvider.forListModulo(colors.map { LineStyle(color = it) })
          crossWireDecimalsLabelBoxStyles = MultiProvider.forListModulo(colors.map { BoxStyle(fill = it, padding = CrossWireLayer.Style.DefaultLabelBoxPadding) })
          requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 2 }
          requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { 2 }
        }

        gestalt.data.minimumSamplingPeriod = samplingPeriod
        historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(samplingPeriod.distance * 36000, samplingPeriod.toHistoryBucketRange())

        gestalt.configure(this)

        onDispose(historyStorage)

        configure {
          // start playing
          chartSupport.translateOverTime.animated = true

          layers.addLayer(ShowTimeRangeLayer(gestalt.style.contentAreaTimeRange))

          declare {
            button("Play/Pause") {
              chartSupport.translateOverTime.animated = !chartSupport.translateOverTime.animated
            }

            button("Home") {
              chartSupport.zoomAndTranslationSupport.resetToDefaults()
            }
          }


          val liveDataEnabled = ObservableBoolean(true)

          //Repeat in accordance to sampling period
          repeat(samplingPeriod.distance.milliseconds) {
            if (liveDataEnabled.value) {
              //The demo generator checks automatically how many and which values have to be added
              historyChunkGenerator.next()?.let {
                historyStorage.storeWithoutCache(it, samplingPeriod)
              }
            }

          }.let {
            chartSupport.onDispose(it)
          }

          declare {
            section("Model")
          }

          configurableBoolean("Live data", liveDataEnabled) {
          }

          configurableInt("History length (s)") {
            min = 1
            max = (samplingPeriod.distance * 7.2).roundToInt()

            value = (historyStorage.maxSizeConfiguration.getGuaranteedDuration(samplingPeriod.toHistoryBucketRange()) / 1000.0).roundToInt()

            onChange {
              historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(it * 1000.0, samplingPeriod.toHistoryBucketRange())
              markAsDirty()
            }
          }

          configurableDouble("Gap factor", (gestalt.data.historyGapCalculator as DefaultHistoryGapCalculator).factor) {
            min = 0.1
            max = 20.0
            onChange {
              gestalt.data.historyGapCalculator = DefaultHistoryGapCalculator(it)
              markAsDirty()
            }
          }

          declare {
            section("Visibility")
          }

          configurableInt("Visible value axes") {
            min = 0
            max = dataSeriesCount
            value = gestalt.style.requestedVisibleValueAxesIndices.size()
            onChange {
              gestalt.style.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { it }
              markAsDirty()
            }
          }

          configurableInt("Visible lines") {
            min = 0
            max = dataSeriesCount
            value = gestalt.style.requestedVisibleDecimalSeriesIndices.size()
            onChange {
              gestalt.style.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { it }
              markAsDirty()
            }
          }

          configurableList(
            "Content area duration (sec)", (gestalt.style.contentAreaDuration / 1000.0).roundToInt(), listOf(
              10,
              60,
              60 * 10,
              60 * 60,
              60 * 60 * 24
            )
          ) {
            onChange {
              gestalt.style.contentAreaDuration = it * 1000.0
              markAsDirty()
            }
          }

          declare {
            section("X-Axis")
          }

          configurableDouble("Cross wire position", gestalt.style::crossWirePositionX) {
            min = 0.0
            max = 1.0
          }

          configurableDouble("Time axis size", gestalt.style::timeAxisSize) {
            min = 0.0
            max = 200.0
          }

          declare {
            section("Style")

            button("Random colors and dashes") {
              val newLineStyles = listOf(
                LineStyle(color = Color.random(), dashes = Dashes.predefined[random.nextInt(Dashes.predefined.size)]),
                LineStyle(color = Color.random(), dashes = Dashes.predefined[random.nextInt(Dashes.predefined.size)]),
                LineStyle(color = Color.random(), dashes = Dashes.predefined[random.nextInt(Dashes.predefined.size)]),
                LineStyle(color = Color.random(), dashes = Dashes.predefined[random.nextInt(Dashes.predefined.size)])
              )
              gestalt.style.lineStyles = MultiProvider.forListModulo(
                newLineStyles
              )
              gestalt.style.crossWireDecimalsLabelBoxStyles = MultiProvider.forListModulo(newLineStyles.map {
                BoxStyle(fill = it.color, padding = CrossWireLayer.Style.DefaultLabelBoxPadding)
              })

              markAsDirty()
            }
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

          declare {
            button("Clear cache") {
              gestalt.tileProvider.clear()
              markAsDirty()
            }
          }
        }
      }
    }
  }
}

