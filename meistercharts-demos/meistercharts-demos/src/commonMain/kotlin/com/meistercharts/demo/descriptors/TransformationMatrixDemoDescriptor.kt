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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.AbstractCanvasRenderingContext
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.geometry.Matrix
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom
import kotlin.math.PI

/**
 *
 */
class TransformationMatrixDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Transformation Matrix"
  override val category: DemoCategory
    get() = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()
          layers.addLayer(MyShowCurrentTransformLayer())
        }
      }
    }
  }
}

private class MyShowCurrentTransformLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc as AbstractCanvasRenderingContext
    gc.font(FontDescriptorFragment.XS)

    var matrices: Array<Matrix> = emptyArray()
    var depth: Int = 0

    gc.resetTransform()

    verifyTranslationNative(gc)

    gc.saved {
      verifyTranslationNative(gc)

      gc.translateToCenter()
      gc.paintMark()


      gc.saved {
        verifyTranslationNative(gc)
        gc.translate(100.0, 0.0)
        verifyTranslationNative(gc)

        gc.rotateRadians(PI / 4.0)

        gc.paintMark()
        gc.fillText("Rotated Text", 0.0, 0.0, Direction.Center)

        //save the current transform
        matrices = gc.currentTransform.matrices.copyOf()
        depth = gc.currentTransform.stackDepth
      }
    }

    gc.paintMatrix(depth, matrices)
  }

  private fun verifyTranslationNative(gc: AbstractCanvasRenderingContext) {
    require(gc.translation == gc.nativeTranslation) {
      "Translation: ${gc.translation.format()} - native: ${gc.nativeTranslation?.format()}"
    }
  }

  private fun AbstractCanvasRenderingContext.paintMatrix(depth: Int, matrices: Array<Matrix>) {
    for (i in 0..depth) {
      val matrix = matrices[i]

      val translation = Distance(matrix.tx, matrix.ty)
      val scale = Zoom(matrix.a, matrix.d)

      paintTextBox(
        listOf(
          "Stack Depth: $i:",
          "\tTranslation: ${translation.format()}",
          "\tScale: ${scale.format()}"
        ),
        Direction.TopLeft
      )

      translate(0.0, 60.0)
    }
  }
}
