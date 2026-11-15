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
package com.meistercharts.demojs.descriptors

import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTexts
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.js.CanvasJS
import com.meistercharts.js.MeisterchartJS
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.kotlin.lang.requireNotNull

class DisposeDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("25bbe4a4-57a2-42e5-a204-a8e7c1e8aabe")
  override val name: String = "Dispose Holder"
  override val category: DemoCategory = DemoCategory.Platform
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addTexts { textService, i18nConfiguration ->
            val canvasJs = chartSupport.canvas as CanvasJS
            val meistercharts: MeisterchartJS = chartSupport.meisterchart as MeisterchartJS

            listOf(
              "Canvas: ${chartSupport.canvas}",
              "Canvas.canvasElement: ${canvasJs.canvasElement}",
              "Canvas.canvasElement.id: ${canvasJs.canvasElement.id}",
              "Canvas.canvasElement.className: ${canvasJs.canvasElement.className}",
              "meistercharts: ${meistercharts}",
              "meistercharts.holder: ${meistercharts.holder}",
              "meistercharts.holder.id: ${meistercharts.holder.id}",
              "meistercharts.holder.className: ${meistercharts.holder.className}",
            )
          }

          declare {
            button("Dispose") {
              val canvasElement = (chartSupport.canvas as CanvasJS).canvasElement
              val holderElement = (chartSupport.meisterchart as MeisterchartJS).holder
              val holderParentElement = holderElement.parentNode.requireNotNull()

              require(canvasElement.isConnected) {
                "canvasElement must be connected before dispose"
              }
              require(holderElement.isConnected) {
                "Holder must be connected before dispose"
              }
              require(holderParentElement.isConnected) {
                "holderParent must be connected (before dispose)"
              }

              //This call delegates to chartSupport.dispose(). But this is the method that will be called by the API users
              chartSupport.meisterchart.dispose() //Delegates to chartSupport.dispose()


              require(canvasElement.isConnected.not()) {
                "canvasElement must not be connected after dispose"
              }
              require(holderElement.isConnected.not()) {
                "Holder must not be connected after dispose"
              }

              //Parent should still exist!
              require(holderParentElement.isConnected) {
                "holderParent must be connected (after dispose)"
              }

              require(canvasElement.parentNode == null) {
                "canvasElement must not have a parent"
              }
              require(holderElement.parentNode == null) {
                "canvasElement must not have a parent"
              }
              require(holderElement.childNodes.length == 0) {
                "Holder must not have any child nodes"
              }

              require(holderParentElement.childNodes.length == 0) {
                "holderParent must not have any child nodes"
              }
            }
          }
        }
      }
    }
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.demojs.descriptors.DisposeDemoDescriptor")
  }
}
