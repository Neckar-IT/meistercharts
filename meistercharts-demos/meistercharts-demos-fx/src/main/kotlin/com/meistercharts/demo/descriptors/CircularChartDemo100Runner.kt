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

import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.circular.CircularChartLayer
import com.meistercharts.algorithms.layers.circular.CircularChartLegendLayer
import com.meistercharts.algorithms.layers.debug.MarkAsDirtyLayer
import com.meistercharts.demo.descriptors.CircularChartDemoDescriptor
import com.meistercharts.demo.descriptors.createCircularChartValues
import it.neckar.open.javafx.inScrollPane
import it.neckar.open.provider.DefaultDoublesProvider
import com.meistercharts.fx.CanvasHolder
import com.meistercharts.fx.MeisterChartBuilderFX
import com.meistercharts.fx.MeisterChartsPlatform
import it.neckar.commons.logback.LogbackConfigurer
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.GridPane
import javafx.stage.Stage

fun main() {
  LogbackConfigurer.setRootLoggerLevel(org.slf4j.event.Level.INFO)
  Application.launch(CircularChartDemo100Runner::class.java, CircularChartDemoDescriptor::class.qualifiedName)
}

class CircularChartDemo100Runner : Application() {
  override fun start(primaryStage: Stage) {
    MeisterChartsPlatform.init()

    val root = GridPane()

    primaryStage.scene = Scene(root.inScrollPane())

    for (i in 0 until 100) {
      root.add(createCircularChart().apply {
        this.minWidth = 150.0
        this.minHeight = 150.0
      }, i % 10, i / 10)
    }

    primaryStage.show()
  }
}

private fun createCircularChart(): CanvasHolder {
  return MeisterChartBuilderFX("CircularChart")
    .apply {
      zoomAndTranslationDefaults(ZoomAndTranslationDefaults.tenPercentMargin)
      configure {
        chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtTop

        layers.addClearBackground()

        val valuesProvider = DefaultDoublesProvider(createCircularChartValues(4))
        val layer = CircularChartLayer(valuesProvider)
        layer.style.maxDiameter = 200.0

        val legendLayer = CircularChartLegendLayer(valuesProvider)
        layers.addLayer(layer)
        layers.addLayer(legendLayer)
        layers.addLayer(MarkAsDirtyLayer())
      }
    }
    .build().canvasHolder
}

