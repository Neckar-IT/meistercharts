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

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.SimpleAreaBetweenLinesPainter
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Insets
import com.meistercharts.painter.AreaBetweenLinesPainter
import it.neckar.open.collections.fastForEachIndexed

/**
 *
 */
class AreaBetweenLinesPainterDemoDescriptor : ChartingDemoDescriptor<AreaBetweenLinesPainter> {
  override val name: String = "Area between Line Painter"
  override val category: DemoCategory = DemoCategory.Painters

  override val predefinedConfigurations: List<PredefinedConfiguration<AreaBetweenLinesPainter>> = listOf<PredefinedConfiguration<AreaBetweenLinesPainter>>(
    PredefinedConfiguration<AreaBetweenLinesPainter>(SimpleAreaBetweenLinesPainter(false, false), "Simple"),
    PredefinedConfiguration<AreaBetweenLinesPainter>(SimpleAreaBetweenLinesPainter(true, true), "Snapped"),
  )

  override fun createDemo(configuration: PredefinedConfiguration<AreaBetweenLinesPainter>?): ChartingDemo {
    requireNotNull(configuration) { "Configuration required" }

    val painter = configuration.payload

    return ChartingDemo {
      meistercharts {
        val margin = Insets(20.0, 20.0, 20.0, 70.0)

        zoomAndTranslationDefaults {
          FittingWithMargin(margin)
        }
        contentAreaSizingStrategy = BindContentAreaSize2ContentViewport()

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          val linePainterLayer = MyAreaBetweenLinesPainterLayer(painter)
          layers.addLayer(linePainterLayer)

          //if (painter is SplineLinePainter) {
          //  configurableDouble("Smoothing factor", painter::smoothingFactor) {
          //    min = -2.0
          //    max = 2.0
          //  }
          //}

          configurableDouble("line width", linePainterLayer.style::lineWidth) {
            max = 20.0
          }

          configurableColorPicker("line Color", linePainterLayer.style::lineColor)
          configurableColorPicker("Area Color", linePainterLayer.style::areaColor)
          configurableBoolean("Stroke Line", linePainterLayer::stroke)
        }
      }
    }
  }
}

private class MyAreaBetweenLinesPainterLayer(
  val painter: AreaBetweenLinesPainter,
  styleConfigurer: Style.() -> Unit = {},
) : AbstractLayer() {

  val style: Style = Style().also(styleConfigurer)

  var stroke: Boolean = true

  override val type: LayerType
    get() = LayerType.Content

  val y1List: List<@DomainRelative Double> = listOf(0.05, 0.10, 0.30, 0.25, 0.05, 0.27)
  val y2List: List<@DomainRelative Double> = listOf(0.15, 0.80, 0.35, 0.55, 0.12, 0.93)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    painter.begin(gc)

    y1List.fastForEachIndexed { index, y1DomainRelative: @DomainRelative Double ->
      @DomainRelative val y2DomainRelative = y2List[index]

      @Window val y1 = chartCalculator.domainRelative2windowY(y1DomainRelative)
      @Window val y2 = chartCalculator.domainRelative2windowY(y2DomainRelative)

      @Window val x = chartCalculator.domainRelative2windowX(0.1 + 0.1 * index)

      painter.addCoordinates(gc, x, y1, y2)
    }

    gc.lineWidth = style.lineWidth
    gc.strokeStyle(style.lineColor)
    gc.fill(style.areaColor)

    painter.paint(gc, stroke)
  }

  @StyleDsl
  class Style {
    var lineWidth: Double = 1.0
    var lineColor: Color = Color.red
    var areaColor: Color = Color.blue
  }
}

