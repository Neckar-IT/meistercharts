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
package com.meistercharts.fx.performance

import javafx.scene.paint.Color as FxColor
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.saved
import com.meistercharts.fx.MeisterChartBuilderFX
import com.meistercharts.fx.size
import com.meistercharts.model.Direction
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.time.LocalDateTime

/**
 * Simple demo that measures the time it takes to paint an image to a canvas
 *
 */
fun main() {
  Application.launch(CanvasImagePerformanceDemo::class.java)
}

class CanvasImagePerformanceDemo : Application() {
  override fun start(primaryStage: Stage) {

    val meisterChart = MeisterChartBuilderFX("CanvasImagePerformanceDemo")
      .apply {
        configure {
          layers.addClearBackground()
          layers.addLayer(ImagePaintingBenchmarkLayer())
        }

      }.build()

    val root = BorderPane(meisterChart)

    root.style = "-fx-background-color: ORANGE;"
    root.padding = Insets(30.0)

    primaryStage.scene = Scene(root)

    primaryStage.width = 1000.0
    primaryStage.height = 800.0
    primaryStage.show()
  }
}

class ImagePaintingBenchmarkLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  private val imageToPaint = createImage()

  fun createImage(): Image {
    val canvas = Canvas()
    canvas.width = 500.0
    canvas.height = 500.0

    canvas.graphicsContext2D.fill = FxColor.ORANGE
    canvas.graphicsContext2D.fillText(LocalDateTime.now().toString(), 250.0, 50.0)

    val writableImage = WritableImage(canvas.width.toInt(), canvas.height.toInt())
    return canvas.snapshot(SnapshotParameters(), writableImage)
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.fill(Color.gray)
    gc.fillRect(0.0, 0.0, gc.width, gc.height)

    val start = System.nanoTime()
    val image = com.meistercharts.canvas.Image(imageToPaint, imageToPaint.size)

    paintingContext.gc.saved {
      image.paintInBoundingBox(paintingContext, 0.0, 0.0, Direction.TopLeft)
    }
    val end = System.nanoTime()

    val delta = end - start
    gc.fill(Color.red)
    gc.fillText("Draw image took: $delta ns", 20.0, 10.0, Direction.TopLeft)
  }
}
