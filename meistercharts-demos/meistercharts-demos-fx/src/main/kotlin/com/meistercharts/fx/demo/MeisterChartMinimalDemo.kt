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
package com.meistercharts.fx.demo

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.fx.MeisterChartBuilderFX
import com.meistercharts.fx.MeisterChartsPlatform
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

fun main() {
  Application.launch(MeisterChartMinimalDemo::class.java)
}

/**
 * Minimal example for MeisterCharts
 */
internal class MeisterChartMinimalDemo : Application() {
  override fun start(primaryStage: Stage) {
    MeisterChartsPlatform.init()

    val meisterChart = MeisterChartBuilderFX("MeisterChartMinimalDemo")
      .apply {
        configure {
          layers.addClearBackground()
          layers.addTextUnresolved("Hello World!!!", Color.cadetblue)
        }

      }.build()

    val root = BorderPane(meisterChart)
    root.style = "-fx-background-color: ORANGE;"
    root.padding = Insets(10.0)

    primaryStage.scene = Scene(root)


    primaryStage.width = 1000.0
    primaryStage.height = 800.0
    primaryStage.show()
  }
}
