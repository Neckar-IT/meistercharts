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

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.fill
import com.meistercharts.color.Color
import com.meistercharts.canvas.saved
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableString
import it.neckar.geometry.Direction
import it.neckar.open.collections.fastForEachIndexed

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class WebColorDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("b7c904cd-e27c-4f4d-98b7-faa552236036")
  override val name: String = "Web Color"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  val colors = listOf(
    "AliceBlue",
    "#ff00ff",
    "#1b0",
    "#1b07",
    "#ff000080",
    "rgba(255, 0, 0, 0.3)",
    "hsl(120, 100%, 50%)",
    "hsla(120, 100%, 50%, 0.3)",
    "rgb(100%,0%,60%)",
    "rgb(100%,0%,60%, 0.5)",
    "rgb(2.55e2, 0e0, 1.53e2, 1e2%)",
    "hwb(1.5708rad 60% 0%)"
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var fillAsString: String = "#123456"

            fun paintSquare(paintingContext: LayerPaintingContext, colorString: String) {
              val unparsedColor: Color = try {
                Color.unparsed(colorString)
              } catch (ignored: Exception) {
                Color.black()
              }
              val parseResult = try {
                Color.web(colorString)
                "parsed"
              } catch (ignored: Exception) {
                "failed"
              }

              val gc = paintingContext.gc
              try {
                gc.fill(unparsedColor)
              } catch (ignored: Exception) {
                gc.fill(Color.black)
              }

              gc.saved {
                val fillStyle = gc.currentFillDebug()
                gc.fill(Color.black)
                gc.fillText("unparsed color: $unparsedColor", 10.0, 20.0, Direction.TopLeft)
                gc.fillText("context fill-style: $fillStyle", 10.0, 40.0, Direction.TopLeft)
                gc.fillText("parse-result: $parseResult", 10.0, 60.0, Direction.TopLeft)
              }

              gc.fillRect(80.0, 80.0, 50.0, 50.0)
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              paintSquare(paintingContext, fillAsString)
              gc.translate(0.0, 200.0)

              colors.fastForEachIndexed { index, color ->
                paintSquare(paintingContext, color)
                gc.translate(400.0, 0.0)
                if (index % 4 == 3) {
                  gc.translate(-400.0 * 4, 200.0)
                }
              }
            }
          }

          configurable("Fill color", layer::fillAsString)
          layers.addLayer(layer)
        }
      }
    }
  }
}
