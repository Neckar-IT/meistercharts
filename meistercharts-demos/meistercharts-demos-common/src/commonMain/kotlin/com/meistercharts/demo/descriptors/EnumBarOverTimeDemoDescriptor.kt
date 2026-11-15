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
@file:Suppress("RETURN_VALUE_NOT_USED") // Demo-scope subscriptions tied to the demo lifetime

package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.time.TimeRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.axis.time.TimeAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.timeChartCalculator
import com.meistercharts.algorithms.painter.stripe.enums.RectangleEnumStripePainter
import com.meistercharts.annotations.Window
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
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
class EnumBarOverTimeDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("005d6660-fde9-4680-904a-65000bc8f20c")
  override val name: String = "Enum Bar over time"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          @ms val interval = 1000.0 //paint for every second

          layers.addClearBackground()

          val contentAreaTimeRange = TimeRange.oneMinuteSinceReference
          val timeAxisLayer = TimeAxisLayer(TimeAxisLayer.Configuration(contentAreaTimeRange = contentAreaTimeRange))
          layers.addLayer(timeAxisLayer)

          val historyConfiguration = historyConfiguration {
            enumDataSeries(DataSeriesId(0), "Enum with 5 options", HistoryEnum.createSimple("Simple", listOf("zero", "one", "two", "three", "four", "five", "six")))
            // enumDataSeries(DataSeriesId(1), "Enum with 2 options", HistoryEnum.Boolean)
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
                enumBarsPainter.layoutValueChange(paintingContext, EnumDataSeriesIndex.zero, startX, endX, start, end, Double.NaN, enumValue, enumOrdinalMostTime)

                current += interval
              }

              enumBarsPainter.layoutFinish(paintingContext, EnumDataSeriesIndex.zero)
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartSupport = paintingContext.chartSupport
              val chartCalculator = paintingContext.chartCalculator

              enumBarsPainter.paint(paintingContext, EnumDataSeriesIndex.zero)
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

