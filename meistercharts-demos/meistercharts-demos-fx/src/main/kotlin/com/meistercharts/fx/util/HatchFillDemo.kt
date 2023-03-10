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
package com.meistercharts.fx.util

import it.neckar.open.javafx.FxPaintingUtils
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.stage.Stage

fun main() {
  Application.launch(HatchFillDemo::class.java)
}

/**
 */
class HatchFillDemo : Application() {
  @Throws(Exception::class)
  override fun start(primaryStage: Stage) {
    val root = BorderPane()

    val canvas = Canvas()
    canvas.widthProperty().bind(root.widthProperty())
    canvas.heightProperty().bind(root.heightProperty())

    val repaintButton = Button("Repaint me")
    repaintButton.setOnAction { event ->
      val gc = canvas.graphicsContext2D
      gc.fill = FxPaintingUtils.createHatch(Color.BLUE, 2.0, 30.0, 30.0)
      gc.fillRect(0.0, 0.0, 100.0, 100.0)
    }

    val moveButton = Button("Move")
    moveButton.onAction = object : EventHandler<ActionEvent> {
      private var x: Int = 0

      override fun handle(event: ActionEvent) {
        val gc = canvas.graphicsContext2D
        gc.clearRect(0.0, 0.0, 800.0, 600.0)

        gc.fill = FxPaintingUtils.createHatch(Color.BLUE, 2.0, 30.0, 30.0)
        x++
        gc.fillRect(x.toDouble(), 0.0, 100.0, 100.0)
      }
    }

    root.top = HBox(repaintButton, moveButton)
    root.center = canvas

    primaryStage.scene = Scene(root)

    primaryStage.width = 1024.0
    primaryStage.height = 768.0
    primaryStage.show()
  }
}
