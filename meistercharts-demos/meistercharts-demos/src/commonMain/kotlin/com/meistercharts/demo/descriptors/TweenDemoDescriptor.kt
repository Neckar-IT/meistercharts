package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.interpolate
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.animation.Easing
import com.meistercharts.canvas.animation.AnimationRepeatType
import com.meistercharts.canvas.animation.Tween
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section
import com.meistercharts.model.Rectangle
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.si.ms
import kotlin.math.max
import kotlin.reflect.KMutableProperty0

/**
 *
 */
class TweenDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Tween"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val width = 200.0
          val height = 200.0

          val layer = object : AbstractLayer() {
            var tweenX: Tween = Tween(nowMillis() + 500, 2800.0, Easing.inOutQuad, AnimationRepeatType.RepeatAutoReverse)
            var tweenY: Tween = Tween(nowMillis(), 2000.0, Easing.inOutQuad, AnimationRepeatType.RepeatAutoReverse)

            override val type: LayerType = LayerType.Content
            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(gc.width / 2.0 - width / 2.0, gc.height / 2.0 - height / 2.0)


              gc.stroke(Color.blue)
              gc.strokeRect(Rectangle(0.0, 0.0, width, height))

              //draw the line
              kotlin.run {
                val maxDuration = max(tweenX.duration, tweenY.duration) * 8
                val startTimeX = tweenX.startTime

                @ms var time = startTimeX
                while (time <= startTimeX + maxDuration) {
                  val x = width * tweenX.interpolate(time)
                  val y = width * tweenY.interpolate(time)

                  gc.fill(Color.lightgray)
                  gc.fillOvalCenter(x, y, 1.0, 1.0)

                  time += 10.0
                }
              }

              //Paint the current position
              val x = width * tweenX.interpolate(paintingContext)
              val y = width * tweenY.interpolate(paintingContext)
              gc.fill(Color.orangered)
              gc.fillOvalCenter(x, y, 10.0, 10.0)

              chartSupport.markAsDirty()
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

  private fun ChartingDemo.configureTween(tweenProperty: KMutableProperty0<Tween>) {
    configurableDouble("Duration", tweenProperty.get().duration) {
      max = 10_000.0

      onChange {
        tweenProperty.set(tweenProperty.get().withDuration(duration = it))
      }
    }

    configurableEnum("Repeat Type", tweenProperty.get().repeatType, enumValues()) {
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
        tweenProperty.set(tweenProperty.get().copy(startTime = nowMillis()))
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

private fun Tween.withDuration(duration: Double): Tween {
  return this.copy(definition = definition.copy(duration = duration))
}
