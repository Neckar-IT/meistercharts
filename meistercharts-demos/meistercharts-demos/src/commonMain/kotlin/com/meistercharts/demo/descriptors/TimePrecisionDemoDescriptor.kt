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
import com.meistercharts.algorithms.layers.debug.MarkAsDirtyLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.model.Direction
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 *
 */
class TimePrecisionDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Time Precision Test"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = MyTimePrecisionTimerDemoDescriptor()
          layers.addLayer(layer)
          layers.addLayer(MarkAsDirtyLayer())

          configurableDouble("Speed factor", layer::speedFactor) {
            max = 10.0
            min = 0.1
          }

          configurableListWithProperty("Refresh rate", this.chartSupport::targetRefreshRate, TargetRefreshRate.predefined) {
            converter {
              "${it.refreshRate} FPS"
            }
          }
        }
      }
    }
  }
}

private class MyTimePrecisionTimerDemoDescriptor : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  var speedFactor = 1.0

  /**
   * Contains the last deltas
   */
  val deltaTimes = mutableListOf<@ms Double>()

  /**
   * The deltas of the x translations
   */
  val deltaXs = mutableListOf<@px Double>()

  override fun paint(paintingContext: LayerPaintingContext) {
    @ms val now = nowMillis()
    @ms val delta = now - paintingContext.frameTimestamp

    deltaTimes.add(delta)
    deltaTimes.ensureSize(100)

    val gc = paintingContext.gc
    gc.translate(10.0, 10.0)

    //First paint a rect that is moved based on the exact time
    gc.fill(Color.blue)
    @px val xFrameTimestamp = paintingContext.paintRectForTimestamp(paintingContext.frameTimestamp, "frame timestamp")

    gc.translate(0.0, 100.0)

    //Second a rect that is moved based on now
    gc.fill(Color.green)
    @px val xNow = paintingContext.paintRectForTimestamp(now, "now")


    //Paint the time deltas
    gc.translate(0.0, 200.0)
    gc.fill(Color.red)

    paintDeltaValues(gc, deltaTimes, "ms")

    //Paint the x deltas
    deltaXs.add((xNow - xFrameTimestamp) % gc.width)
    deltaXs.ensureSize(100)

    gc.translate(0.0, 50.0)
    gc.fill(Color.black)

    paintDeltaValues(gc, deltaXs, "px")

  }

  private fun paintDeltaValues(gc: CanvasRenderingContext, values: List<Double>, unit: String) {
    values.forEachIndexed { index, delta ->
      gc.fillRect(index * 6.0, 0.0, 5.0, -delta)
    }

    values.maxOrNull()?.let {
      gc.fillText("Max: $it $unit", 10.0, 10.0, Direction.TopLeft)
    }
    values.minOrNull()?.let {
      gc.fillText("Min: $it $unit", 10.0, 30.0, Direction.TopLeft)
    }
  }

  private fun LayerPaintingContext.paintRectForTimestamp(nowMillis: @ms Double, description: String): @px Double {
    @px val x = (nowMillis * speedFactor % (gc.width - 100))
    gc.fillRect(x, 0.0, 100.0, 100.0)

    gc.fill(Color.white)
    gc.fillText(description, x + 50.0, 50.0, Direction.Center)
    return x
  }
}

private fun <E> MutableList<E>.ensureSize(maxSize: Int) {
  while (size > maxSize) {
    removeAt(0)
  }
}

