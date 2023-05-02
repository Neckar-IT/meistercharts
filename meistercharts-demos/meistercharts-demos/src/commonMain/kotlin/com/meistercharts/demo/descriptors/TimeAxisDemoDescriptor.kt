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
import com.meistercharts.algorithms.layers.Iso8601TickFormat
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.TickDistanceAwareTickFormat
import com.meistercharts.algorithms.layers.TimeAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import it.neckar.open.formatting.dateTimeFormat
import it.neckar.open.formatting.dateTimeFormatWithMillis
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 * Very simple demo that shows how to work with a time domain chart
 */
class TimeAxisDemoDescriptor : ChartingDemoDescriptor<TimeAxisLayer.() -> Unit> {
  override val name: String = "Time axis layer"
  override val description: String = "## Demonstration of the TimeAxisLayer"
  override val category: DemoCategory = DemoCategory.Axis

  override val predefinedConfigurations: List<PredefinedConfiguration<TimeAxisLayer.() -> Unit>> = listOf(
    PredefinedConfiguration({}, "Absolute"),
    PredefinedConfiguration({
      style.timestampsMode = TimeAxisLayer.TimestampsMode.Relative
    }, "Relative"),
  )

  override fun createDemo(configuration: PredefinedConfiguration<TimeAxisLayer.() -> Unit>?): ChartingDemo {
    require(configuration != null)

    return ChartingDemo {
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

          val timeAxisLayer = TimeAxisLayer(TimeAxisLayer.Data(contentAreaTimeRange)) {}
          timeAxisLayer.style.titleProvider = { _, _ -> "My Axis Titel" }
          configuration.payload(timeAxisLayer)

          layers.addLayer(timeAxisLayer)
          layers.addLayer(MyTimeAxisDebugLayer())

          passpartoutLayer.style.bind(timeAxisLayer.style)

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

          configurableList("Format", timeAxisLayer.style.absoluteTimestampTickFormat, listOf(Iso8601TickFormat, TickDistanceAwareTickFormat)) {
            converter = { dateTimeTickFormat -> dateTimeTickFormat::class.simpleName.orEmpty() }
            onChange {
              timeAxisLayer.style.absoluteTimestampTickFormat = it
              markAsDirty()
            }
          }

          configurableDouble("Axis size", timeAxisLayer.style::size) {
            max = 300.0
          }

          configurableInsets("Axis margin", timeAxisLayer.style.margin) {
            max = 300.0
            onChange {
              timeAxisLayer.style.margin = it
              markAsDirty()
            }
          }

          configurableBooleanProvider("Axis title visible", timeAxisLayer.style::titleVisible) {
          }

          configurableColorPickerProvider("Axis title color", timeAxisLayer.style::titleColor)

          configurableFont("Axis title font", timeAxisLayer.style::titleFont) {
          }

          configurableDouble("Axis line width", timeAxisLayer.style::axisLineWidth) {
            max = 10.0
          }

          section("Offset")

          configurableDouble("Offset Area Size", timeAxisLayer.style::offsetAreaSize) {
            max = 70.0
          }

          configurableDouble("Offset Area Tick Label Gap", timeAxisLayer.style::offsetAreaTickLabelGap) {
            max = 20.0
          }

          configurableFont("Tick font", timeAxisLayer.style::tickFont) {
          }

          configurableDouble("Tick line width", timeAxisLayer.style::tickLineWidth) {
            max = 20.0
          }

          configurableDouble("Tick length", timeAxisLayer.style::tickLength) {
            max = 20.0
          }

          configurableDouble("Tick label gap", timeAxisLayer.style::tickLabelGap) {
            max = 20.0
          }

          configurableColorPickerProvider("Background", passpartoutLayer.style::color) {
          }

          configurableColorPickerProvider("Line color", timeAxisLayer.style::lineColor) {
          }

          section("Content Viewport Margin")
          configurableInsetsSeparate("CWP", contentViewportGestalt::contentViewportMargin)

        }
      }
    }
  }
}

private class MyTimeAxisDebugLayer() : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val timeAxisLayer: TimeAxisLayer? = paintingContext.layerSupport.layers.byType() ?: throw IllegalStateException("Could not find layer")
    val timeAxisPaintingProperties = timeAxisLayer?.paintingVariables() ?: return

    val lastTicks = timeAxisPaintingProperties.tickDomainValues

    val lines = mutableListOf<String>().also {
      it.add("Axis Start: ${timeAxisPaintingProperties.axisStart}")
      it.add("Axis End: ${timeAxisPaintingProperties.axisEnd}")
      it.add("Start Time: ${dateTimeFormatWithMillis.format(timeAxisPaintingProperties.startTimestamp, paintingContext.i18nConfiguration)}")
      it.add("End Time: ${dateTimeFormatWithMillis.format(timeAxisPaintingProperties.endTimestamp, paintingContext.i18nConfiguration)}")

      lastTicks.fastForEach { tick ->
        it.add(dateTimeFormatWithMillis.format(tick, paintingContext.i18nConfiguration))
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
