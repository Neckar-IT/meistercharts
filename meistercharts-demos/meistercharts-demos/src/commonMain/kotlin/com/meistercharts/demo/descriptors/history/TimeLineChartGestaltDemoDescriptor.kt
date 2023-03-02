package com.meistercharts.demo.descriptors.history

import com.meistercharts.algorithms.LinearValueRange
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.HudElementIndex
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.TilesLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.barchart.DefaultCategoryAxisLabelPainter
import com.meistercharts.algorithms.tile.CachedTileProvider
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.animation.Easing
import com.meistercharts.canvas.RoundingStrategy
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.TimeBasedValueGeneratorBuilder
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableIndices
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.demo.section
import com.meistercharts.demo.toList
import com.meistercharts.demo.toMutableList
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndexProvider
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryStorageCache
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.WritableHistoryStorage
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.generator.DecimalValueGenerator
import com.meistercharts.history.generator.EnumValueGenerator
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.ReferenceEntryGenerator
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.historyConfigurationOnlyDecimals
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.chunk
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.kotlin.lang.fastMap
import it.neckar.open.kotlin.lang.getModulo
import it.neckar.open.kotlin.lang.random
import it.neckar.open.time.nowMillis
import it.neckar.open.provider.DoublesProvider1
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProvider2
import it.neckar.open.formatting.format
import it.neckar.open.i18n.TextKey
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class TimeLineChartGestaltDemoDescriptor : ChartingDemoDescriptor<TimeLineChartGestalt.() -> Unit> {
  override val name: String = "Time Line Chart"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val predefinedConfigurations: List<PredefinedConfiguration<TimeLineChartGestalt.() -> Unit>> = listOf(
    oneSampleEvery100ms,
    oneSampleEvery100msSick,
    PredefinedConfiguration({}, "empty"),
    oneSampleEvery100msLogarithmic,
    oneSampleEvery16msCached500ms,
    oneSampleEvery16msCached50ms,
    oneSampleEvery100msCached100ms,
    oneSampleEvery24h,
    neckarITHomePage,
    oneSampleEvery16msCached500msAverages,
    candle,
    withAxisTitle,
    outwardsTicks,
    valueAxisTitleOnTop,
  )

  override fun createDemo(configuration: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit>?): ChartingDemo {
    require(configuration != null) { "config required" }
    val gestaltConfig: TimeLineChartGestalt.() -> Unit = configuration.payload


    return ChartingDemo {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage().also {
          it.maxSizeConfiguration = MaxHistorySizeConfiguration(7)
          onDispose(it)
        }

        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()


        val gestalt = TimeLineChartGestalt(chartId, TimeLineChartGestalt.Data(historyStorage))

        val historyChunkBuilder = MyHistoryChunkBuilder {
          gestalt.data.historyConfiguration
        }

        gestalt.configure(this)
        gestalt.gestaltConfig()


        configure {
          configurableBoolean("Play Mode", chartSupport.translateOverTime::animated) {
            value = true
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

          configurableDouble("Cross-wire location", gestalt.style.crossWirePositionXProperty)

          val visibleDecimalLines = gestalt.style.requestedVisibleDecimalSeriesIndices.toMutableList().toMutableSet()
          if (visibleDecimalLines.isNotEmpty()) {
            declare {
              section("Visible lines")
            }

            visibleDecimalLines.forEach { lineIndex ->
              configurableBoolean("${lineIndex.value + 1}. line visible") {
                value = true
                onChange {
                  if (it) {
                    visibleDecimalLines.add(lineIndex)
                  } else {
                    visibleDecimalLines.remove(lineIndex)
                  }
                  gestalt.style.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.forList(visibleDecimalLines.toList())
                  markAsDirty()
                }
              }
            }
          }

          configurableList("Visible value axes", -1, listOf(0, 1, 2, 5, 8, 10, 15)) {
            converter { if (it == -1) "initial" else it.toString() }
            onChange { visibleValueAxes ->
              if (visibleValueAxes == -1) {
                // initial -> do nothing
              } else {
                gestalt.style.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { visibleValueAxes }
                markAsDirty()
              }
            }
          }

          val enumDataSeriesCount = gestalt.data.historyConfiguration.enumDataSeriesCount
          if (enumDataSeriesCount > 0) {
            declare {
              button("Show all enums") {
                gestalt.style.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.indices {
                  gestalt.data.historyConfiguration.enumDataSeriesCount
                }
              }
              button("Show 100 all enums") {
                gestalt.style.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.indices {
                  100
                }
              }
              button("Hide all enums") {
                gestalt.style.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.empty()
              }
            }

            configurableIndices(
              this@ChartingDemo,
              "Visible Enum lines",
              "enum visible",
              initial = gestalt.style.requestVisibleEnumSeriesIndices.toList().map { it.value },
              maxSize = enumDataSeriesCount,
            ) {
              gestalt.style.requestVisibleEnumSeriesIndices = EnumDataSeriesIndexProvider.forList(it.map { EnumDataSeriesIndex(it) })
            }
          }

          declare {
            section("Samples / Data") {
            }

            button("Add sample") {
              historyStorage.storeWithoutCache(historyChunkBuilder.createHistoryChunk(1, SamplingPeriod.EveryHundredMillis), SamplingPeriod.EveryHundredMillis)
            }
            button("Add 10 samples (100ms)") {
              historyStorage.storeWithoutCache(historyChunkBuilder.createHistoryChunk(10, SamplingPeriod.EveryHundredMillis), SamplingPeriod.EveryHundredMillis)
            }
            button("Add 100 samples (100ms)") {
              historyStorage.storeWithoutCache(historyChunkBuilder.createHistoryChunk(100, SamplingPeriod.EveryHundredMillis), SamplingPeriod.EveryHundredMillis)
            }
          }

          configurableFont("Time axis tick font", gestalt.timeAxisLayer.style::tickFont) {
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
            button("Clear Tiles cache") {
              chartSupport.layerSupport.layers.layers.firstOrNull {
                it is TilesLayer
              }?.let {
                val tileProvider = (it as TilesLayer).tileProvider
                val cachedTileProvider = tileProvider as CachedTileProvider
                cachedTileProvider.clear()
              }
            }
          }

          configurableColor("Value Axis Background", gestalt.style::valueAxesBackground)

          section("Enum")
          configurableDouble("Enum Height", gestalt.historyEnumLayer.configuration::stripeHeight) {
            max = 50.0
          }
          configurableDouble("Bar distance", gestalt.historyEnumLayer.configuration::stripesDistance) {
            max = 30.0
          }

          section("Enum-Axis")
          configurableDouble("Lines Gap", (gestalt.enumCategoryAxisLayer.style.axisLabelPainter as DefaultCategoryAxisLabelPainter).style::twoLinesGap) {
            max = 5.0
            min = -5.0
          }
          configurableEnum("Split lines mode", (gestalt.enumCategoryAxisLayer.style.axisLabelPainter as DefaultCategoryAxisLabelPainter).style::wrapMode) {
          }
        }
      }
    }
  }

  companion object {
    /**
     * Configures a [TimeLineChartGestalt] to receive a sample every 100 milliseconds stored into a [WritableHistoryStorage]
     */
    val oneSampleEvery100ms: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(samplingPeriod)
        val writableHistoryStorage = this.data.historyStorage as WritableHistoryStorage

        it.neckar.open.time.repeat(100.milliseconds) {
          historyChunkGenerator.next()?.let {
            writableHistoryStorage.storeWithoutCache(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }

        this.data.thresholdValueProvider = object : DoublesProvider1<DecimalDataSeriesIndex> {
          override fun size(param1: DecimalDataSeriesIndex): Int {
            return 1
          }

          override fun valueAt(index: Int, param1: DecimalDataSeriesIndex): Double {
            return (param1.value + 3) * 10.0
          }
        }

        this.data.thresholdLabelProvider = object : MultiProvider2<HudElementIndex, List<String>, DecimalDataSeriesIndex, LayerPaintingContext> {
          override fun valueAt(index: Int, param1: DecimalDataSeriesIndex, param2: LayerPaintingContext): List<String> {
            return listOf(
              "Threshold for $param1",
              data.thresholdValueProvider.valueAt(index, param1).format(3)
            )
          }
        }
      }, "1 sample / 100 ms"
    )

    val oneSampleEvery100msSick: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        oneSampleEvery100ms.payload(this)

        //Make three axis visible
        style.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 3 }

        //Title on top
        style.applyValueAxisTitleOnTop(40.0)

        style.valueAxisStyleConfiguration = { style: ValueAxisLayer.Style, dataSeriesIndex: DecimalDataSeriesIndex ->
          style.side = Side.Left
          style.tickOrientation = Vicinity.Outside
          style.paintRange = AxisStyle.PaintRange.Continuous
        }
      },
      "1 sample / 100 ms [SICK]"
    )

    val withAxisTitle: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(samplingPeriod)
        val writableHistoryStorage = this.data.historyStorage as WritableHistoryStorage

        it.neckar.open.time.repeat(100.milliseconds) {
          historyChunkGenerator.next()?.let {
            writableHistoryStorage.storeWithoutCache(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }

        timeAxisLayer.style.titleProvider = { _, _ -> "The Axis title!!! - Axis size is ${this.style.timeAxisSize}" }
        style.timeAxisSize = 100.0
      }, "with axis title"
    )

    val outwardsTicks: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        oneSampleEvery100ms.payload(this)

        this.enumCategoryAxisLayer.style.tickOrientation = Vicinity.Outside
        this.enumCategoryAxisLayer.style.showAxisLine()
        this.style.valueAxisStyleConfiguration = { style: ValueAxisLayer.Style, lineIndex: DecimalDataSeriesIndex ->
          style.tickOrientation = Vicinity.Outside
        }

      }, "Outwards ticks"
    )

    val valueAxisTitleOnTop: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        oneSampleEvery100ms.payload(this)

        this.style.applyValueAxisTitleOnTop()
      }, "Outwards ticks"
    )

    val oneSampleEvery100msLogarithmic: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        oneSampleEvery100ms.payload(this)

        val originalConfig = style.valueAxisStyleConfiguration

        this.style.valueAxisStyleConfiguration = { style, lineIndex ->
          originalConfig(style, lineIndex)
          style.applyLogarithmicScale()
        }

        this.data.historyConfiguration.let { historyConfiguration ->
          this.style.lineValueRanges = MultiProvider.forListModulo(historyConfiguration.decimalDataSeriesCount.fastMap { index ->
            val original = style.lineValueRanges.valueAt(index)
            ValueRange.logarithmic(1.0, original.end)
          })
        }
      }, "1 sample / 100 ms - logarithmic"
    )

    /**
     * Configures a [TimeLineChartGestalt] to receive a sample every 100 milliseconds stored into a [HistoryStorageCache]
     */
    val oneSampleEvery16msCached500ms: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(samplingPeriod)
        val writableHistoryStorage = this.data.historyStorage as WritableHistoryStorage
        val historyStorageCache = HistoryStorageCache(writableHistoryStorage, 500.milliseconds)

        it.neckar.open.time.repeat(16.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorageCache.scheduleForStore(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }
      }, "1 sample / 10 ms (stored every 500 ms)"
    )

    val oneSampleEvery16msCached500msAverages: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(samplingPeriod)
        val writableHistoryStorage = this.data.historyStorage as WritableHistoryStorage
        val historyStorageCache = HistoryStorageCache(writableHistoryStorage, 500.milliseconds)

        it.neckar.open.time.repeat(16.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorageCache.scheduleForStore(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }
      }, "1 sample / 10 ms (stored every 500 ms)"
    )

    /**
     * Configures a [TimeLineChartGestalt] to receive a sample every 100 milliseconds stored into a [HistoryStorageCache]
     */
    val oneSampleEvery16msCached50ms: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        val samplingPeriod = SamplingPeriod.EveryMillisecond
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(samplingPeriod)
        val writableHistoryStorage = this.data.historyStorage as WritableHistoryStorage
        val historyStorageCache = HistoryStorageCache(writableHistoryStorage, 50.milliseconds)

        it.neckar.open.time.repeat(16.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorageCache.scheduleForStore(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }
      }, "1 sample / 1 ms (stored every 50 ms)"
    )

    val oneSampleEvery100msCached100ms: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(samplingPeriod)
        val writableHistoryStorage = this.data.historyStorage as WritableHistoryStorage
        val historyStorageCache = HistoryStorageCache(writableHistoryStorage, 100.milliseconds)

        it.neckar.open.time.repeat(100.milliseconds) {
          historyChunkGenerator.next()?.let {
            historyStorageCache.scheduleForStore(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }
      }, "1 sample / 100 ms (stored every 100 ms)"
    )

    val oneSampleEvery24h: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        val samplingPeriod = SamplingPeriod.Every24Hours
        val writableHistoryStorage = this.data.historyStorage as WritableHistoryStorage

        style.applySamplingPeriod(samplingPeriod)

        //Fill with 100 data points
        data.historyConfiguration = historyConfigurationOnlyDecimals(8) { dataSeriesIndex ->
          when (dataSeriesIndex.value) {
            0 -> {
              decimalDataSeries(DataSeriesId(17), TextKey.simple("Mass Flow Rate [kg/h]"), HistoryUnit("kg/h"))
            }

            1 -> {
              decimalDataSeries(DataSeriesId(23), TextKey.simple("Flow Velocity [m/s]"), HistoryUnit("m/s"))
            }

            2 -> {
              decimalDataSeries(DataSeriesId(56), TextKey.simple("Volume [m³]"), HistoryUnit("m³"))
            }

            3 -> {
              decimalDataSeries(DataSeriesId(89), TextKey.simple("Volumetric Flow Rate [m³/h]"), HistoryUnit("m³/h"))
            }

            4 -> {
              decimalDataSeries(DataSeriesId(117), TextKey.simple("Mass [kg]"), HistoryUnit("kg"))
            }

            5 -> {
              decimalDataSeries(DataSeriesId(118), TextKey.simple("Energy [kWh]"), HistoryUnit("kWh"))
            }

            6 -> {
              decimalDataSeries(DataSeriesId(123), TextKey.simple("Temperature [°C]"), HistoryUnit("°C"))
            }

            7 -> {
              decimalDataSeries(DataSeriesId(143), TextKey.simple("Pressure [bar]"), HistoryUnit("bar"))
            }

            else -> throw IllegalArgumentException("$dataSeriesIndex")
          }
        }

        val baseMillis = nowMillis() - samplingPeriod.distance
        val decimalValueGenerator = DecimalValueGenerator.cosine(ValueRange.default)

        data.historyConfiguration.chunk(100) { timestampIndex ->
          this.addDecimalValues(
            baseMillis + timestampIndex.value * samplingPeriod.distance,
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
            decimalValueGenerator.generate(timestampIndex.value.toDouble()),
          )
        }.let {
          writableHistoryStorage.storeWithoutCache(it, samplingPeriod)
        }
      }, "1 sample / 24h"
    )

    /**
     * Configures a [TimeLineChartGestalt] for the Neckar IT home page
     */
    val neckarITHomePage: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(samplingPeriod)
        val writableHistoryStorage = this.data.historyStorage as InMemoryHistoryStorage

        //adjust the position of the cross wire
        style.crossWirePositionX = 0.85

        //add some samples for the last hour and set the max history size accordingly
        writableHistoryStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(70.0.minutes, samplingPeriod.toHistoryBucketRange())
        historyChunkGenerator.forTimeRange(TimeRange.oneHourUntilNow())?.let {
          writableHistoryStorage.storeWithoutCache(it, samplingPeriod)
        }

        it.neckar.open.time.repeat(100.milliseconds) {
          historyChunkGenerator.next()?.let {
            writableHistoryStorage.storeWithoutCache(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }

        //we want three lines and three value axes to be visible
        this.style.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { 3 }
        this.style.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 3 }
      }, "Neckar IT Home Page"
    )

    val candle: PredefinedConfiguration<TimeLineChartGestalt.() -> Unit> = PredefinedConfiguration(
      {
        val samplingPeriod = SamplingPeriod.EveryHundredMillis
        val historyChunkGenerator = this.setUpHistoryChunkGenerator(samplingPeriod)
        val writableHistoryStorage = this.data.historyStorage as InMemoryHistoryStorage

        //adjust the position of the cross wire
        style.crossWirePositionX = 0.85

        //add some samples for the last hour and set the max history size accordingly
        writableHistoryStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(70.minutes, samplingPeriod.toHistoryBucketRange())
        historyChunkGenerator.forTimeRange(TimeRange.oneHourUntilNow())?.let {
          writableHistoryStorage.storeWithoutCache(it, samplingPeriod)
        }

        it.neckar.open.time.repeat(100.milliseconds) {
          historyChunkGenerator.next()?.let {
            writableHistoryStorage.storeWithoutCache(it, samplingPeriod)
          }
        }.also {
          onDispose(it)
        }

        //we want three lines and three value axes to be visible
        style.requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { 3 }
        style.requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 3 }

        configureForCandle()
      }, "Candle"
    )
  }

}

private fun ValueRange.reduce(percentage: @pct Double): LinearValueRange {
  val diff = delta * percentage
  val newStart = start + diff
  val newEnd = end - diff
  return ValueRange.linear(newStart, newEnd)
}

internal class MyHistoryChunkBuilder(val historyConfigurationProvider: () -> HistoryConfiguration) {
  fun createHistoryChunk(size: Int, samplingPeriod: SamplingPeriod): HistoryChunk {
    @ms val nowMillis = nowMillis()

    val historyConfiguration = historyConfigurationProvider()

    return historyConfiguration.chunk(size) { timestampIndex ->
      addDecimalValues(nowMillis + timestampIndex.value * samplingPeriod.distance, *randomValues(historyConfiguration.decimalDataSeriesCount))
    }
  }

  private fun randomValues(size: Int): DoubleArray {
    return DoubleArray(size) { random.nextDouble(35.0, 75.0) }
  }
}


fun TimeLineChartGestalt.setUpHistoryChunkGenerator(samplingPeriod: SamplingPeriod): HistoryChunkGenerator {
  //Avoid gaps for the cross wire - when adding only
  data.historyGapCalculator = DefaultHistoryGapCalculator(10.0)

  val writableHistoryStorage = data.historyStorage as WritableHistoryStorage
  val historyConfiguration = historyConfiguration {
    decimalDataSeries(DataSeriesId(17), TextKey.simple("Mass Flow Rate [kg/h]"), HistoryUnit("kg/h"))
    decimalDataSeries(DataSeriesId(23), TextKey.simple("Flow Velocity [m/s]"), HistoryUnit("m/s"))
    decimalDataSeries(DataSeriesId(56), TextKey.simple("Volume [m³]"), HistoryUnit("m³"))
    decimalDataSeries(DataSeriesId(89), TextKey.simple("Volumetric Flow Rate [m³/h]"), HistoryUnit("m³/h"))
    decimalDataSeries(DataSeriesId(117), TextKey.simple("Mass [kg]"), HistoryUnit("kg"))
    decimalDataSeries(DataSeriesId(118), TextKey.simple("Energy [kWh]"), HistoryUnit("kWh"))
    decimalDataSeries(DataSeriesId(123), TextKey.simple("Temperature [°C]"), HistoryUnit("°C"))
    decimalDataSeries(DataSeriesId(143), TextKey.simple("Pressure [bar]"), HistoryUnit("bar"))

    enumDataSeries(DataSeriesId(1001), TextKey.simple("Global State"), HistoryEnum.createSimple("Warning State", listOf("Ok", "Warning", "Error")))
    enumDataSeries(DataSeriesId(1002), TextKey.simple("Engine running"), HistoryEnum.Boolean)
    enumDataSeries(DataSeriesId(1003), TextKey.simple("Compliance"), HistoryEnum.createSimple("Compliance State", listOf("Compliant", "Not Compliant", "Unknown")))
    enumDataSeries(DataSeriesId(1004), TextKey.simple("Boiler"), HistoryEnum.Boolean)
    enumDataSeries(DataSeriesId(1005), TextKey.simple("Auxiliary Engine 1"), HistoryEnum.Boolean)
    enumDataSeries(DataSeriesId(1006), TextKey.simple("Auxiliary Engine 2"), HistoryEnum.Boolean)
    enumDataSeries(DataSeriesId(1007), TextKey.simple("Auxiliary Engine 3"), HistoryEnum.Boolean)
    enumDataSeries(DataSeriesId(1008), TextKey.simple("Auxiliary Engine 4"), HistoryEnum.Boolean)
  }

  data.historyConfiguration = historyConfiguration

  val dataSeriesValueRanges = listOf(
    ValueRange.linear(0.0, 1000.0),
    ValueRange.linear(0.0, 300.0),
    ValueRange.linear(0.0, 999999.0),
    ValueRange.linear(0.0, 1000.0),
    ValueRange.linear(0.0, 999999.0),
    ValueRange.linear(0.0, 999999.0),
    ValueRange.linear(-50.0, 100.0),
    ValueRange.linear(-0.5, 17.0)
  )
  style.lineValueRanges = MultiProvider.forListModulo(dataSeriesValueRanges)

  style.valueAxisStyleConfiguration = { valueAxisStyle, lineIndex ->
    valueAxisStyle.size = when (lineIndex.value) {
      2, 4, 5 -> 165.0
      else -> 90.0
    }
  }

  val easings = listOf(
    Easing.inOut,
    Easing.smooth,
    Easing.inOutBack,
  )

  val decimalValueGenerators = historyConfiguration.decimalDataSeriesCount.fastMap {
    TimeBasedValueGeneratorBuilder {
      val dataSeriesValueRange = dataSeriesValueRanges[it]
      startValue = dataSeriesValueRange.center() + (random.nextDouble() - 0.5).coerceAtMost(0.2).coerceAtLeast(-0.2) * dataSeriesValueRange.delta
      minDeviation = dataSeriesValueRange.delta * (0.025 * (it + 1)).coerceAtMost(0.25)
      maxDeviation = (dataSeriesValueRange.delta * (0.05 * (it + 1)).coerceAtMost(0.25)).coerceAtLeast(minDeviation * 1.001)
      period = 2_000.0 * (it + 1)
      valueRange = dataSeriesValueRange.reduce(0.25)
      easing = easings.getModulo(it)
    }.build()
  }

  val enumValueGenerators: List<EnumValueGenerator> = historyConfiguration.enumDataSeriesCount.fastMap {
    EnumValueGenerator.random()
  }

  val referenceEntryGenerators: List<ReferenceEntryGenerator> = historyConfiguration.referenceEntryDataSeriesCount.fastMap {
    ReferenceEntryGenerator.random()
  }

  return HistoryChunkGenerator(
    historyStorage = writableHistoryStorage, samplingPeriod = samplingPeriod,
    decimalValueGenerators = decimalValueGenerators,
    enumValueGenerators = enumValueGenerators,
    referenceEntryGenerators = referenceEntryGenerators,
    historyConfiguration = historyConfiguration
  )
}
