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


import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Path
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.fillStyle
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.color.get
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Direction
import kotlin.uuid.Uuid

/**
 */
class StrokeFillVariantsPathDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("8fe2050f-f526-406b-8acd-0313084b4666")
  override val name: String = "Drawing Path: stroke and fill variants"

  override val description: String = "Shows how different stroke and fill settings affect a non-closed path."
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()


          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc: CanvasRenderingContext = paintingContext.gc
              gc.translate(50.0, 10.0)

              // paint and stroke with no fillStyle or strokeStyle manually set

              val fills = listOf(DemoColor.TRANSPARENT, DemoColor.NULL, DemoColor.BLUE)
              val strokes = listOf(DemoColor.TRANSPARENT, DemoColor.NULL, DemoColor.GREEN)

              for (elemFill in fills) {
                for (elemStroke in strokes) {

                  gc.translate(0.0, 40.0)
                  gc.saved {
                    paintExample(gc, PaintingMethod.STROKE, elemFill, elemStroke)
                  }

                  gc.translate(0.0, 40.0)
                  gc.saved {
                    paintExample(gc, PaintingMethod.FILL, elemFill, elemStroke)
                  }
                  gc.translate(0.0, 20.0)

                }
              }
            }
          })
        }
      }
    }
  }

  /**
   * if fillStyle or strokeStyle are null, the styles will not be set and the default will be used instead
   */
  fun paintExample(gc: CanvasRenderingContext, method: PaintingMethod, fillStyle: DemoColor, strokeStyle: DemoColor) {
    val path = Path().also {
      it.moveTo(10.0, 10.0)
      it.lineTo(30.0, 10.0)
      it.lineTo(30.0, 30.0)
      it.lineTo(10.0, 30.0)
      it.lineTo(30.0, 40.0)

    }
    val fillStyleColor = fillStyle.color
    val stokeStyleColor = strokeStyle.color

    if (method.equals(PaintingMethod.FILL)) {
      if (fillStyleColor != null) {
        gc.fillStyle(fillStyleColor)
      }
      if (stokeStyleColor != null) {
        gc.stroke(stokeStyleColor)
      }
      gc.fill(path)
      printDescription(gc, PaintingMethod.FILL, fillStyle, strokeStyle)
    }

    if (method.equals(PaintingMethod.STROKE)) {
      if (fillStyleColor != null) {
        gc.fillStyle(fillStyleColor)
      }
      if (stokeStyleColor != null) {
        gc.stroke(stokeStyleColor)
      }
      gc.stroke(path)
      printDescription(gc, PaintingMethod.STROKE, fillStyle, strokeStyle)
    }
  }


  fun printDescription(gc: CanvasRenderingContext, paintMethod: PaintingMethod, fillStyle: DemoColor, strokeStyle: DemoColor, yOffset: Double = 25.0) {
    gc.fillStyle(Color.black)
    gc.stroke(Color.black)
    gc.fillText("method: <${paintMethod.name}> | fill style: <${fillStyle.name}> | stroke style: <${strokeStyle.name}>", 100.0, yOffset, Direction.CenterLeft)
  }

  enum class PaintingMethod {
    STROKE, FILL
  }

  enum class DemoColor(val color: Color?) {
    NULL(null), TRANSPARENT(Color.transparent.get()), BLACK(Color.black.get()), GREEN(Color.green.get()), BLUE(Color.blue.get())
  }

}
