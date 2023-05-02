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

import javafx.application.Application
import javafx.stage.Stage

/**
 * Runs all charting demos
 */
fun main(args: Array<String>) {
  DemoMessages.registerEnumTranslator()
  Application.launch(ChartingDemosFX::class.java, *args)
}

class ChartingDemosFX : Application() {
  lateinit var chartingDemosFxSupport: ChartingDemosFxSupport

  val demoDescriptors: List<ChartingDemoDescriptor<*>> = buildList {
    addAll(DemoDescriptors.descriptors)
    addAll(DemoDescriptorsFx.descriptors)
  }

  override fun start(primaryStage: Stage) {
    chartingDemosFxSupport = ChartingDemosFxSupport(demoDescriptors)
    chartingDemosFxSupport.start(primaryStage, parameters)
  }

  override fun stop() {
    super.stop()
    chartingDemosFxSupport.stop()
  }
}
