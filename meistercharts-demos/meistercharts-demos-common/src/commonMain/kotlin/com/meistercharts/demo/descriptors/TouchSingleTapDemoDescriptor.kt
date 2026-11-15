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
package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.color.Color
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandlerBroker
import com.meistercharts.canvas.paintMark
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Coordinates
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.gesture.SingleTapGestureSupport
import it.neckar.logging.LoggerFactory

/**
 *
 */
class TouchSingleTapDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("ebc9b3e2-07ae-46a9-9f70-3cbc7eef972c")
  override val name: String = "Touch: Single Tap"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override val description: String = "Detects and visualizes single-tap touch gestures."

  override val category: DemoCategory = DemoCategory.Interaction

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          this.layers.addClearBackground()

          this.layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var lastTabCoordinates: @Window Coordinates? = null

            val singleTapGestureSupport = SingleTapGestureSupport().also {
              it.onTap {
                logger.debug("Tap detected")
                lastTabCoordinates = it
                markAsDirty()
                EventConsumption.Consumed
              }
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              lastTabCoordinates?.let {
                gc.paintMark(it, color = Color.red())
              }
            }

            override val touchEventHandler: CanvasTouchEventHandler = CanvasTouchEventHandlerBroker().also {
              it.delegate(singleTapGestureSupport)
            }
          })
        }
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.demo.descriptors.TouchSingleTapDemoDescriptor")
  }
}
