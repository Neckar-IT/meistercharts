package com.meistercharts.demo.descriptors.history

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.debug.ShowTimeRangeLayer
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.tile.DefaultHistoryGapCalculator
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.charts.timeline.TimeLineChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.HistoryUnit
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.chunk
import it.neckar.open.collections.asDoubles
import it.neckar.open.provider.MultiProvider
import it.neckar.open.formatting.formatUtc
import it.neckar.open.i18n.TextKey
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.si.ms
import it.neckar.logging.LoggerFactory

class CoronaChartDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Corona Time Line Chart"
  override val category: DemoCategory = DemoCategory.Automation

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val historyStorage = InMemoryHistoryStorage()
        historyStorage.scheduleDownSampling()
        historyStorage.scheduleCleanupService()

        val historyConfiguration = historyConfiguration {
          decimalDataSeries(DataSeriesId(1), TextKey.simple("recovered"), HistoryUnit.None)
          decimalDataSeries(DataSeriesId(2), TextKey.simple("confirmed"), HistoryUnit.None)
          decimalDataSeries(DataSeriesId(3), TextKey.simple("deaths"), HistoryUnit.None)
        }

        val gestalt = TimeLineChartGestalt(chartId, data = TimeLineChartGestalt.Data(historyStorage, historyConfiguration)) {
          crossWirePositionX = 0.85
          requestedVisibleValueAxesIndices = DecimalDataSeriesIndexProvider.indices { 3 }
          requestedVisibleDecimalSeriesIndices = DecimalDataSeriesIndexProvider.indices { 3 }


          lineStyles = MultiProvider.forListModulo(
            listOf(
              LineStyle(color = Color.green),
              LineStyle(color = Color.orange),
              LineStyle(color = Color.red)
            )
          )
          crossWireDecimalsLabelBoxStyles = MultiProvider.forListModulo(
            listOf(
              BoxStyle(fill = Color.green),
              BoxStyle(fill = Color.orange),
              BoxStyle(fill = Color.red)
            )
          )

          lineValueRanges = MultiProvider.always(ValueRange.linear(0.0, 20_000_000.0))
        }

        val expectedSamplingPeriod = SamplingPeriod.Every24Hours
        gestalt.data.minimumSamplingPeriod = expectedSamplingPeriod

        historyStorage.naturalSamplingPeriod = expectedSamplingPeriod
        historyStorage.maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(43200000 * 1000.0, expectedSamplingPeriod.toHistoryBucketRange())

        gestalt.data.historyGapCalculator = DefaultHistoryGapCalculator(10.0)

        val timestamps = doubleArrayOf(
          1579647600000.0,
          1580252400000.0,
          1580857200000.0,
          1581462000000.0,
          1582066800000.0,
          1582671600000.0,
          1583276400000.0,
          1583881200000.0,
          1584486000000.0,
          1585090800000.0,
          1585692000000.0,
          1586296800000.0,
          1586901600000.0,
          1587506400000.0,
          1588111200000.0,
          1588716000000.0,
          1589320800000.0,
          1589925600000.0,
          1590530400000.0,
          1591135200000.0,
          1591740000000.0,
          1592344800000.0,
          1592949600000.0,
          1593554400000.0,
          1594159200000.0,
          1594764000000.0,
          1595368800000.0,
          1595973600000.0,
          1596578400000.0
        )

        val recovered = intArrayOf(28, 126, 1124, 5150, 16119, 30384, 51171, 67005, 83315, 116043, 195731, 325430, 509311, 709050, 970673, 1238857, 1539693, 1887486, 2327646, 2773422, 3445430, 4029616, 4696244, 5401296, 6511930, 7479335, 8545069, 9847460, 11207047).asDoubles()
        val confirmed = intArrayOf(555, 6167, 27637, 45223, 75642, 81451, 95181, 125950, 214939, 456486, 926095, 1470002, 2023684, 2579766, 3124774, 3707350, 4295733, 4936349, 5617475, 6430667, 7280801, 8251432, 9313253, 10510308, 11894293, 13411754, 15014754, 16820961, 18610735).asDoubles()
        val deaths = intArrayOf(17, 133, 564, 1118, 2122, 2770, 3254, 4615, 8733, 21035, 46413, 87706, 133354, 182569, 226771, 262746, 295713, 326662, 353296, 383273, 413913, 446320, 479888, 512788, 546437, 581197, 619241, 662733, 703022).asDoubles()
        check(timestamps.size == recovered.size && recovered.size == confirmed.size && confirmed.size == deaths.size)

        val chunk = historyConfiguration.chunk(timestamps, recovered, confirmed, deaths)

        historyStorage.storeWithoutCache(
          chunk, SamplingPeriod.Every24Hours
        )



        gestalt.configure(this)

        onDispose(historyStorage)

        configure {
          // stop playing
          chartSupport.translateOverTime.animated = false

          layers.addLayer(ShowTimeRangeLayer(gestalt.style.contentAreaTimeRange))

          declare {
            button("Play/Pause") {
              chartSupport.translateOverTime.animated = !chartSupport.translateOverTime.animated
            }

            button("Home") {
              chartSupport.zoomAndTranslationSupport.resetToDefaults()
            }

            button("Visible time range") {
              @ms val firstTimestamp = timestamps.first()
              @ms val lastTimestamp = timestamps.last()
              // align visible time range with cross wire position
              @ms val visibleSpan = ((lastTimestamp - firstTimestamp) / gestalt.style.crossWirePositionX)
              @ms val visibleTimeRangeEnd = firstTimestamp + visibleSpan

              logger.debug("set visible time range to ${firstTimestamp.formatUtc()} -  ${visibleTimeRangeEnd.formatUtc()}")
              @DomainRelative val startDateRelative = gestalt.style.contentAreaTimeRange.time2relative(firstTimestamp)
              @DomainRelative val endDateRelative = gestalt.style.contentAreaTimeRange.time2relative(visibleTimeRangeEnd)
              chartSupport.zoomAndTranslationSupport.fitX(
                startDateRelative, endDateRelative
              )
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

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.demo.descriptors.history.CoronaChartDemoDescriptor")
  }
}

