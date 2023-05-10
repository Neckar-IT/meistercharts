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
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.TimeAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.timeChartCalculator
import com.meistercharts.algorithms.painter.stripe.enums.RectangleEnumStripePainter
import com.meistercharts.annotations.Window
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.historyConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.kotlin.lang.roundDecimalPlaces
import it.neckar.open.unit.si.ms

/**
 *
 */
class EnumBarOverTimeDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Enum Bar over time"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          @ms val interval = 1000.0 //paint for every second

          layers.addClearBackground()

          val contentAreaTimeRange = TimeRange.oneMinuteSinceReference
          val timeAxisLayer = TimeAxisLayer(TimeAxisLayer.Data(contentAreaTimeRange = contentAreaTimeRange))
          layers.addLayer(timeAxisLayer)

          val historyConfiguration = historyConfiguration {
            enumDataSeries(DataSeriesId(0), "Enum with 5 options", HistoryEnum.createSimple("Simple", listOf("zero", "one", "two", "three", "four", "five", "six")))
            enumDataSeries(DataSeriesId(1), "Enum with 2 options", HistoryEnum.Boolean)
          }

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val enumBarsPainter: RectangleEnumStripePainter = RectangleEnumStripePainter()

            override fun layout(paintingContext: LayerPaintingContext) {
              super.layout(paintingContext)

              val timeChartCalculator = chartSupport.timeChartCalculator(contentAreaTimeRange)
              val visibleTimeRange = timeChartCalculator.visibleTimeRangeXinWindow()

              enumBarsPainter.layoutBegin(paintingContext, 20.0, EnumDataSeriesIndex.zero, historyConfiguration)

              @ms val start = (visibleTimeRange.start).roundDecimalPlaces(-3) - interval
              @ms val end = visibleTimeRange.end + interval

              var current = start
              while (current <= end) {
                val enumValue = getValueForTime(current)
                val enumOrdinalMostTime = getOrdinalMostTimeForTime(current)

                @Window val startX = timeChartCalculator.time2windowX(current)
                @Window val endX = timeChartCalculator.time2windowX(current + interval)
                enumBarsPainter.layoutValueChange(paintingContext, startX, endX, start, end, Double.NaN, enumValue, enumOrdinalMostTime, Unit, Unit)

                current += interval
              }

              enumBarsPainter.layoutFinish(paintingContext)
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartSupport = paintingContext.chartSupport
              val chartCalculator = paintingContext.chartCalculator

              enumBarsPainter.paint(paintingContext)
            }

            private fun getValueForTime(time: @ms Double): HistoryEnumSet {
              return HistoryEnumSet.forEnumValue((time / interval).toInt() % myHistoryEnum.valuesCount)
            }

            private fun getOrdinalMostTimeForTime(time: @ms Double): HistoryEnumOrdinal {
              return HistoryEnumOrdinal((time / interval).toInt() % HistoryEnumOrdinal.Max.value)
            }
          })
        }
      }
    }
  }
}

val myHistoryEnum: HistoryEnum = HistoryEnum.create(
  "MyEnum", listOf(
    TextKey("Option1"),
    TextKey("Option2"),
    TextKey("Option3"),
  )
)

