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
package com.meistercharts.fx.svg

import it.neckar.open.resources.getResourceSafe
import javafx.application.Application
import javafx.embed.swing.SwingNode
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.apache.batik.swing.JSVGCanvas

fun main() {
  Application.launch(BatikSwingDemoRunner::class.java)
}

/**
 *
 */
class BatikSwingDemoRunner : Application() {
  override fun start(primaryStage: Stage) {
    val root = StackPane()

    val jsvgCanvas = JSVGCanvas()
    val swingNode = SwingNode()

    jsvgCanvas.uri = BatikDemoRunner::class.java.getResourceSafe("amazon.svg").toExternalForm()

    swingNode.content = jsvgCanvas

    root.children.add(swingNode)

    primaryStage.scene = Scene(root, 800.0, 600.0)
    primaryStage.show()
  }
}
