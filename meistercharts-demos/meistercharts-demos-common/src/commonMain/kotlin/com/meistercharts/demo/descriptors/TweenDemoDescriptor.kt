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
import com.meistercharts.algorithms.layers.interpolate
import com.meistercharts.color.Color
import com.meistercharts.animation.Easing
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.animation.AnimationRepeatType
import com.meistercharts.canvas.animation.Tween
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.stroke
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section
import it.neckar.geometry.Rectangle
import kotlin.reflect.KMutableProperty0
import it.neckar.open.time.monotonicMillis
import it.neckar.open.unit.si.ms
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/**
 *
 */
class TweenDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("781d7dec-46c1-4664-b22d-5b6ea469f123")
  override val name: String = "Tween"
  override val category: DemoCategory = DemoCategory.Calculations
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val width = 200.0
          val height = 200.0

          val layer = object : AbstractLayer() {
            var tweenX: Tween = Tween(monotonicMillis() + 500.0, 2800.milliseconds, Easing.inOutQuad, AnimationRepeatType.RepeatAutoReverse)
            var tweenY: Tween = Tween(monotonicMillis(), 2000.milliseconds, Easing.inOutQuad, AnimationRepeatType.RepeatAutoReverse)

            override val type: LayerType = LayerType.Content
            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(gc.width / 2.0 - width / 2.0, gc.height / 2.0 - height / 2.0)


              gc.stroke(Color.blue)
              gc.strokeRect(Rectangle(0.0, 0.0, width, height))

              kotlin.run {
                @ms val maxDuration = maxOf(tweenX.definition.durationMillis, tweenY.definition.durationMillis) * 8
                @ms val startX = tweenX.start

                @ms var time = startX
                while (time <= startX + maxDuration) {
                  val x = width * tweenX.interpolate(time)
                  val y = width * tweenY.interpolate(time)

                  gc.fill(Color.lightgray)
                  gc.fillOvalCenter(x, y, 1.0, 1.0)

                  time += 10.0
                }
              }

              val x = width * tweenX.interpolate(paintingContext)
              val y = width * tweenY.interpolate(paintingContext)
              gc.fill(Color.orangered)
              gc.fillOvalCenter(x, y, 10.0, 10.0)

              chartSupport.markAsDirty(DirtyReason.Animation)
            }
          }
          layers.addLayer(layer)


          section("Tween X")
          configureTween(layer::tweenX)

          section("Tween Y")
          configureTween(layer::tweenY)
        }
      }
    }
  }

  private fun MeisterchartsDemo.configureTween(tweenProperty: KMutableProperty0<Tween>) {
    configurableDouble("Duration", tweenProperty.get().duration.toDouble(DurationUnit.MILLISECONDS)) {
      max = 10_000.0

      onChange {
        tweenProperty.set(tweenProperty.get().withDuration(duration = it.milliseconds))
      }
    }

    configurableEnum("Repeat Type", tweenProperty.get().repeatType, kotlin.enums.enumEntries<AnimationRepeatType>().toList()) {
      onChange {
        tweenProperty.set(tweenProperty.get().withRepeatType(repeatType = it))
      }
    }

    configurableList("Easing", availableEasings[0], availableEasings) {
      onChange {
        tweenProperty.set(tweenProperty.get().withEasing(it.easing))
      }

      converter {
        it.description
      }
    }

    declare {
      button("Restart") {
        tweenProperty.set(tweenProperty.get().copy(start = monotonicMillis()))
      }
    }
  }
}

private fun Tween.withEasing(easing: Easing): Tween {
  return this.copy(definition = definition.copy(interpolator = easing))
}

private fun Tween.withRepeatType(repeatType: AnimationRepeatType): Tween {
  return this.copy(definition = definition.copy(repeatType = repeatType))
}

private fun Tween.withDuration(duration: Duration): Tween {
  return this.copy(definition = definition.copy(duration = duration))
}
