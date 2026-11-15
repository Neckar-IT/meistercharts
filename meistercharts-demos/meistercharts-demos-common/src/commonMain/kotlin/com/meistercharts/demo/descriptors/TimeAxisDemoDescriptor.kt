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
import com.meistercharts.demo.configurableSeparate
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.axis.time.Iso8601TickFormat
import com.meistercharts.algorithms.layers.axis.time.OffsetAwareTickFormat
import com.meistercharts.algorithms.layers.axis.time.TimeAxisLayer
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.algorithms.layers.timeChartCalculator
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.calc.TimeChartCalculator
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.stroke
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section
import com.meistercharts.events.EventConsumption
import com.meistercharts.model.Insets
import com.meistercharts.time.TimeRange
import it.neckar.geometry.Direction
import it.neckar.open.formatting.dateTimeFormat
import it.neckar.open.formatting.dateTimeFormatWithMillis
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 * Very simple demo that shows how to work with a time domain chart
 */
class TimeAxisDemoDescriptor : MeisterchartsDemoDescriptor<TimeAxisLayer.() -> Unit> {
  override val uuid: Uuid = Uuid.parse("e4b94cb7-59ca-47db-88f4-821f8cf7ba5c")
  override val name: String = "Time axis layer"
  override val description: String = "Demonstrates the time axis layer."
  override val category: DemoCategory = DemoCategory.Axis

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<TimeAxisLayer.() -> Unit>> = listOf(
    PredefinedConfiguration(Uuid.parse("9e69487d-7159-4e7f-803a-92661df4e762"), {}, "Absolute"),
    PredefinedConfiguration(Uuid.parse("763ba25a-0c7a-46c1-b232-8f3c1467f064"), {
      configuration.timestampsMode = TimeAxisLayer.TimestampsMode.Relative
    }, "Relative"),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<TimeAxisLayer.() -> Unit>?): MeisterchartsDemo {
    require(configuration != null) { "TimeAxisDemoDescriptor.prepareDemo requires a predefined configuration" }

    return MeisterchartsDemo(this) {
      meistercharts {
        configureAsTimeChart()

        val contentAreaTimeRange = TimeRange.oneHourSinceReference

        val contentViewportGestalt = ContentViewportGestalt(Insets.all15)
        contentViewportGestalt.configure(this)

        configure {
          layers.addClearBackground()

          val passpartoutLayer = PasspartoutLayer {
            color = { Color("rgba(126, 13, 204, 0.25)") }
          }
          layers.addLayer(passpartoutLayer)

          val timeAxisLayer = TimeAxisLayer(TimeAxisLayer.Configuration(contentAreaTimeRange)) {}
          timeAxisLayer.configuration.titleProvider = { _, _ -> "My Axis Titel" }
          configuration.payload(timeAxisLayer)

          layers.addLayer(timeAxisLayer)
          layers.addLayer(MyTimeAxisDebugLayer())

          passpartoutLayer.configuration.bind(timeAxisLayer.configuration)

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              @Domain @ms val now = paintingContext.frameTimestamp
              @Window @px val xNow = paintingContext.chartCalculator.domainRelative2windowX(contentAreaTimeRange.time2relative(now))
              @Window @px val yNow = paintingContext.chartCalculator.domainRelative2windowY(0.5)

              paintingContext.gc.fillText("now: ${dateTimeFormat.format(now, paintingContext.i18nConfiguration)}", xNow, yNow, Direction.CenterRight, 5.0, 5.0)

              paintingContext.gc.lineWidth = 1.0
              paintingContext.gc.stroke(Color.green)
              paintingContext.gc.strokeLine(xNow, 0.0, xNow, paintingContext.gc.height)
            }
          })

          configurableList("Format", timeAxisLayer.configuration.absoluteTimestampTickFormat, listOf(Iso8601TickFormat, OffsetAwareTickFormat)) {
            converter = { dateTimeTickFormat -> dateTimeTickFormat::class.simpleName.orEmpty() }
            onChange {
              timeAxisLayer.configuration.absoluteTimestampTickFormat = it
              markAsDirty()
            }
          }

          configurable("Axis size", timeAxisLayer.configuration::size) {
            max = 300.0
          }

          configurableInsets("Axis margin", timeAxisLayer.configuration.margin) {
            max = 300.0
            onChange {
              timeAxisLayer.configuration.margin = it
              markAsDirty()
            }
          }

          configurable("Axis title visible", timeAxisLayer.configuration::titleVisible) {
          }

          configurable("Axis title color", timeAxisLayer.configuration::titleColor)

          configurable("Axis title font", timeAxisLayer.configuration::titleFont) {
          }

          configurable("Axis line width", timeAxisLayer.configuration::axisLineWidth) {
            max = 10.0
          }

          section("Offset")

          configurable("Offset Area Size", timeAxisLayer.configuration::offsetAreaSize) {
            max = 70.0
          }

          configurable("Offset Area Tick Label Gap", timeAxisLayer.configuration::offsetAreaTickLabelGap) {
            max = 20.0
          }

          configurable("Tick font", timeAxisLayer.configuration::tickFont) {
          }

          configurable("Tick line width", timeAxisLayer.configuration::tickLineWidth) {
            max = 20.0
          }

          configurable("Tick length", timeAxisLayer.configuration::tickLength) {
            max = 20.0
          }

          configurable("Tick label gap", timeAxisLayer.configuration::tickLabelGap) {
            max = 20.0
          }

          configurable("Background", passpartoutLayer.configuration::color) {
          }

          configurable("Line color", timeAxisLayer.configuration::lineColor) {
          }

          section("Content Viewport Margin")
          configurableSeparate("CWP", contentViewportGestalt::contentViewportMargin)

        }
      }
    }
  }
}

private class MyTimeAxisDebugLayer() : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  private var xLocation: Double = Double.NaN

  override fun initialize(paintingContext: LayerPaintingContext) {
    super.initialize(paintingContext)

    paintingContext.chartSupport.mouseEvents.onMove {
      xLocation = it.x
      paintingContext.chartSupport.markAsDirty(DirtyReason.UserInteraction)
      EventConsumption.Ignored
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val chartSupport = paintingContext.chartSupport
    val chartCalculator = chartSupport.chartCalculator

    val timeAxisLayer: TimeAxisLayer = paintingContext.layerSupport.layers.byType() ?: throw IllegalStateException("Could not find layer")
    val timeAxisPaintingVariables = timeAxisLayer.paintingVariables()

    val lines = mutableListOf<String>().also {
      it.add("Axis Start: ${timeAxisPaintingVariables.axisStart}")
      it.add("Axis End: ${timeAxisPaintingVariables.axisEnd}")
      it.add("Start Time: ${dateTimeFormatWithMillis.format(timeAxisPaintingVariables.startTimestamp, paintingContext.i18nConfiguration)}")
      it.add("End Time: ${dateTimeFormatWithMillis.format(timeAxisPaintingVariables.endTimestamp, paintingContext.i18nConfiguration)}")

      val timeChartCalculator: TimeChartCalculator = chartSupport.currentChartState.timeChartCalculator(timeAxisLayer.configuration.contentAreaTimeRange)
      val cursorTime = timeChartCalculator.window2timeX(xLocation)
      it.add("Cursor Time: ${dateTimeFormatWithMillis.format(cursorTime, paintingContext.i18nConfiguration)}")
      it.add("Cursor Time (UTC): ${dateTimeFormatWithMillis.format(cursorTime, I18nConfiguration.US_UTC)}")

      it.add("## Offset Tick Domain Values (Local | UTC | asOffset):")
      timeAxisPaintingVariables.offsetTickLabels.fastForEachValue { tick ->
        it.add(dateTimeFormatWithMillis.format(tick, I18nConfiguration.US_UTC) + " | " + dateTimeFormatWithMillis.format(tick, paintingContext.i18nConfiguration) + " | " + timeAxisPaintingVariables.offsetTickDistance.formatAsOffset(tick, paintingContext.i18nConfiguration))
      }

      it.add("## Tick Domain Values (Local | UTC):")
      timeAxisPaintingVariables.tickLabels.fastForEachValue { tick ->
        it.add(dateTimeFormatWithMillis.format(tick, I18nConfiguration.US_UTC) + " | " + dateTimeFormatWithMillis.format(tick, paintingContext.i18nConfiguration))
      }
    }

    val gc = paintingContext.gc
    gc.translateToBottomLeft()
    gc.paintTextBox(
      lines = lines,
      anchorDirection = Direction.BottomLeft,
      anchorGapHorizontal = 100.0,
      anchorGapVertical = 150.0,
    )
  }
}
