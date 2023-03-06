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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.animation.Easing
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import it.neckar.open.collections.fastForEachWithIndex
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.kotlin.lang.toIntFloor
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.ms
import kotlin.reflect.KProperty0

/**
 *
 */
class EasingDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Easing"
  override val category: DemoCategory = DemoCategory.Calculations
  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val easingDemoLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            /**
             * The animation duration in milli seconds
             */
            var duration: @ms Double = 1_000.0

            /**
             * The height and width of the rectangle that shows the easing curve
             */
            val rectWidthHeight = 150

            val widthPerElement = rectWidthHeight + 50.0
            val heightPerElement = rectWidthHeight + 70.0

            val margin = Insets.of(50.0)

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(margin.left, margin.top)

              @Zoomed val netWidth = gc.width - margin.offsetWidth
              @Zoomed val netHeight = gc.height - margin.offsetHeight

              val rowCount = (netHeight / heightPerElement).toIntFloor()
              val colCount = (netWidth / widthPerElement).toIntFloor()


              availableEasings.fastForEachWithIndex { index, easingWithDescription ->
                gc.saved {
                  val columnIndex = index % colCount
                  val rowIndex = index / colCount

                  gc.translate(columnIndex * widthPerElement, rowIndex * heightPerElement)

                  //Paint the rectangle
                  gc.stroke(Color.gray)
                  gc.strokeRect(0.0, 0.0, rectWidthHeight.toDouble(), rectWidthHeight.toDouble())

                  gc.fillText(easingWithDescription.description, rectWidthHeight / 2.0, rectWidthHeight.toDouble(), Direction.TopCenter, 5.0, 5.0)

                  //Paint the curve
                  gc.beginPath()
                  rectWidthHeight.fastFor {
                    @pct val pctIn = it.toDouble() / rectWidthHeight
                    @pct val value = easingWithDescription.easing(pctIn)
                    gc.lineTo(it.toDouble(), value * rectWidthHeight)
                  }
                  gc.stroke(Color.blue)
                  gc.stroke()


                  //Draw the balls for the current position
                  @ms var relativeTime = paintingContext.frameTimestamp % (duration * 2)
                  if (relativeTime > duration) {
                    //revert
                    relativeTime = 2 * duration - relativeTime
                  }

                  @pct val animationPercentage = 1.0 / duration * relativeTime
                  @pct val animationYPercentage = easingWithDescription.easing(animationPercentage)

                  //Paint the current position (in the square)
                  gc.fill(Color.orange)
                  gc.fillOvalCenter(animationPercentage * rectWidthHeight, animationYPercentage * rectWidthHeight, 4.0, 4.0)

                  //Paint the ball (on the right side - only y)
                  gc.stroke(Color.lightgray)
                  gc.strokeLine(rectWidthHeight + 15.0, 0.0, rectWidthHeight + 15.0, rectWidthHeight.toDouble())

                  gc.fill(Color.blue)
                  gc.fillOvalCenter(rectWidthHeight + 15.0, animationYPercentage * rectWidthHeight, 10.0, 10.0)
                }
              }

              markAsDirty()
            }
          }

          layers.addLayer(easingDemoLayer)

          configurableDouble("Duration", easingDemoLayer::duration) {
            min = 10.0
            max = 5000.0
          }
        }
      }
    }
  }
}


/**
 * Contains the predefined easing
 */
val availableEasings: List<EasingWithDescription> = listOf(
  EasingWithDescription.create(Easing.Companion::linear),
  EasingWithDescription.create(Easing.Companion::sin),
  EasingWithDescription.create(Easing.Companion::smooth),
  EasingWithDescription.create(Easing.Companion::sin),
  EasingWithDescription.create(Easing.Companion::out),
  EasingWithDescription.create(Easing.Companion::inOut),
  EasingWithDescription.create(Easing.Companion::inBack),
  EasingWithDescription.create(Easing.Companion::outBack),
  EasingWithDescription.create(Easing.Companion::inOutBack),
  EasingWithDescription.create(Easing.Companion::outInBack),
  EasingWithDescription.create(Easing.Companion::inElastic),
  EasingWithDescription.create(Easing.Companion::outElastic),
  EasingWithDescription.create(Easing.Companion::inOutElastic),
  EasingWithDescription.create(Easing.Companion::inBounce),
  EasingWithDescription.create(Easing.Companion::outBounce),
  EasingWithDescription.create(Easing.Companion::inOutBounce),
  EasingWithDescription.create(Easing.Companion::outInBounce),
  EasingWithDescription.create(Easing.Companion::inQuad),
  EasingWithDescription.create(Easing.Companion::outQuad),
  EasingWithDescription.create(Easing.Companion::inOutQuad)
)

data class EasingWithDescription(
  //Use a provider to work around strange issue with missing initialization in JS
  val easingProvider: () -> Easing,
  val description: String
) {

  val easing: Easing
    get() {
      return easingProvider()
    }

  override fun toString(): String {
    return description
  }

  companion object {
    fun create(property: KProperty0<Easing>): EasingWithDescription {
      @Suppress("SENSELESS_COMPARISON")
      return EasingWithDescription({
        val easing = property.get()
        require(easing != null) {
          "easing must not be null for property <${property.name}>"
        }
        easing
      }, property.name)
    }
  }
}
