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
