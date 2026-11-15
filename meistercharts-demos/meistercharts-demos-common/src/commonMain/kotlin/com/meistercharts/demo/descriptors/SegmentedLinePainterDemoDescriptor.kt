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


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.color.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.charts.FitContentInViewportGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Coordinates
import com.meistercharts.model.Insets
import com.meistercharts.painter.DirectLineSegmentPainter
import com.meistercharts.painter.SegmentedLinePainter
import it.neckar.open.collections.fastForEachIndexed

/**
 *
 */
class SegmentedLinePainterDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("f25cf973-8af4-4582-9c03-9e96eb5592e2")
  override val name: String = "Segmented Line Painter"
  override val category: DemoCategory = DemoCategory.Painters
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        FitContentInViewportGestalt(Insets.of(20.0)).configure(this)

        configure {
          layers.addClearBackground()
          layers.addLayer(MySegmentedLinePaintersLayer())
        }
      }
    }
  }
}


private class MySegmentedLinePaintersLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  var linePainter = SegmentedLinePainter() {
    val solid = DirectLineSegmentPainter(LineStyle.Continuous.copy(Color.red))
    val dotted = DirectLineSegmentPainter(LineStyle.Dotted.copy(Color.green))
    val small = DirectLineSegmentPainter(LineStyle.SmallDashes.copy(Color.blue))
    val large = DirectLineSegmentPainter(LineStyle.LargeDashes.copy(Color.magenta))

    when (it % 4) {
      0 -> dotted
      1 -> small
      2 -> large
      else -> solid
    }
  }

  val model: List<@DomainRelative Coordinates> = listOf(
    Coordinates.of(0.0, 0.0),
    Coordinates.of(0.1, 0.1),
    Coordinates.of(0.2, 0.2),
    Coordinates.of(0.35, 0.4),
    Coordinates.of(0.45, 0.25),
    Coordinates.of(0.66, 0.35),
    Coordinates.of(0.75, 0.17),
    Coordinates.of(0.58, 0.66), //backwards!
    Coordinates.of(0.85, 0.58),
    Coordinates.of(0.95, 0.79),
    Coordinates.of(0.99, 0.43),
    Coordinates.of(1.0, 0.99)
  )

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    linePainter.begin(gc)
    model.fastForEachIndexed { index, coordinates ->
      @Window val x = chartCalculator.domainRelative2windowX(coordinates.x)
      @Window val y = chartCalculator.domainRelative2windowY(coordinates.y)

      linePainter.addCoordinates(gc, x, y)
    }
    linePainter.paint(gc)
  }
}
