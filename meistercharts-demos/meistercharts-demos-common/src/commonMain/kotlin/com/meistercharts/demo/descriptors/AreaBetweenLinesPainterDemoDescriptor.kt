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
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.SimpleAreaBetweenLinesPainter
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.strokeStyle
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Insets
import com.meistercharts.painter.AreaBetweenLinesPainter
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.zoom.FittingWithMargin
import it.neckar.open.collections.fastForEachIndexed

/**
 *
 */
class AreaBetweenLinesPainterDemoDescriptor : MeisterchartsDemoDescriptor<AreaBetweenLinesPainter> {
  override val uuid: Uuid = Uuid.parse("f780a691-dd01-4bb2-80d5-62931562f9d9")

  override val name: String = "Area between Line Painter"
  override val category: DemoCategory = DemoCategory.Painters

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<AreaBetweenLinesPainter>> = listOf<PredefinedConfiguration<AreaBetweenLinesPainter>>(
    PredefinedConfiguration<AreaBetweenLinesPainter>(Uuid.parse("be5d1da9-3b74-4352-9d28-1ba037da635a"), SimpleAreaBetweenLinesPainter(false, false), "Simple"),
    PredefinedConfiguration<AreaBetweenLinesPainter>(Uuid.parse("fc44fb62-3fd0-455b-9327-b629e9534af6"), SimpleAreaBetweenLinesPainter(true, true), "Snapped"),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<AreaBetweenLinesPainter>?): MeisterchartsDemo {
    requireNotNull(configuration) { "Configuration required" }

    val painter = configuration.payload

    return MeisterchartsDemo(this) {
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

          configurable("line width", linePainterLayer.configuration::lineWidth) {
            max = 20.0
          }

          configurable("line Color", linePainterLayer.configuration::lineColor)
          configurable("Area Color", linePainterLayer.configuration::areaColor)
          configurable("Stroke Line", linePainterLayer::stroke)
        }
      }
    }
  }
}

private class MyAreaBetweenLinesPainterLayer(
  val painter: AreaBetweenLinesPainter,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  val configuration: Configuration = Configuration().also(additionalConfiguration)

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

    gc.lineWidth = configuration.lineWidth
    gc.strokeStyle(configuration.lineColor)
    gc.fill(configuration.areaColor)

    painter.paint(gc, stroke)
  }

  @ConfigurationDsl
  class Configuration {
    var lineWidth: Double = 1.0
    var lineColor: ColorProvider = Color.red
    var areaColor: ColorProvider = Color.blue
  }
}

