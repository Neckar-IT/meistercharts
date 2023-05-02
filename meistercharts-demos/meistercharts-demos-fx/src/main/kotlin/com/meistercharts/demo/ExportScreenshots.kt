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
package com.meistercharts.demo

import com.meistercharts.algorithms.mainScreenDevicePixelRatio
import com.meistercharts.demo.descriptors.MapGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.MapWithPaintablesGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.ScatterPlotGestaltDemoDescriptor
import it.neckar.open.javafx.JavaFxTimer
import it.neckar.open.javafx.saveScreenshot
import com.meistercharts.fx.MeisterChartBuilderFX
import com.meistercharts.fx.MeisterChartFX
import com.meistercharts.fx.MeisterChartsPlatform
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.io.File
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 *
 */
fun main(args: Array<String>) {
  DemoMessages.registerEnumTranslator()
  Application.launch(ExportScreenshots::class.java, *args)
}

class ExportScreenshots : Application() {
  /**
   * The descriptors the screenshots are generated for
   */
  private val descriptors = DemoDescriptors.descriptors.filter {
    it.category == DemoCategory.Gestalt || it.category == DemoCategory.ShowCase || it.category == DemoCategory.Automation
  }.toMutableList()

  /**
   * The demo descriptors that are delayed
   */
  private val delayed = setOf(MapGestaltDemoDescriptor::class, MapWithPaintablesGestaltDemoDescriptor::class, ScatterPlotGestaltDemoDescriptor::class)

  override fun start(primaryStage: Stage) {
    MeisterChartsPlatform.init()
    //Overwrite device pixel ratio for screenshots
    mainScreenDevicePixelRatio = 2.0

    val root = BorderPane()

    primaryStage.scene = Scene(root, 1920.0, 1080.0)
    primaryStage.show()

    scheduleNextScreenshot(root)
  }

  private fun scheduleNextScreenshot(root: BorderPane) {
    if (descriptors.isEmpty()) {
      exitProcess(0)
    }

    val demoDescriptor = descriptors.removeAt(0)

    val demo = demoDescriptor.createDemo(null)
    val builder = MeisterChartBuilderFX("Export screenshot - ${demoDescriptor.name}")

    with(demo) {
      builder.configure()
    }

    val meisterChart = builder.build()
    root.center = meisterChart

    val waitDuration: Duration = if (delayed.contains(demoDescriptor::class)) {
      2500.milliseconds
    } else {
      500.milliseconds
    }

    JavaFxTimer.delay(waitDuration) {
      saveScreenshot(meisterChart, demoDescriptor.name)
      scheduleNextScreenshot(root)
    }
  }

  private fun saveScreenshot(meisterChart: MeisterChartFX, name: String) {
    val targetFile = File("/tmp/screenshots/$name.png")
    targetFile.parentFile.mkdir()
    meisterChart.saveScreenshot(targetFile)
  }
}

