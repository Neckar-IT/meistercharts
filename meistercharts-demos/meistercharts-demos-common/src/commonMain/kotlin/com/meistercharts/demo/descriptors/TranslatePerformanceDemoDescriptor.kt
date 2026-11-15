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
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Direction
import it.neckar.open.formatting.decimalFormat
import com.meistercharts.style.BoxStyle
import kotlin.time.DurationUnit
import kotlin.time.measureTime

/**
 */
class TranslatePerformanceDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("81ca4ff6-7879-4344-b942-199ee0577600")
  override val name: String = "Translate Performance"
  override val category: DemoCategory = DemoCategory.LowLevelTests
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        configure {
          layers.addClearBackground()
          layers.addLayer(MyTranslateLayer())
        }
      }
    }
  }
}

private class MyTranslateLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc


    measureTime {
      for (i in 0..10_000) {
        callSavedRecursive(gc, 7)
      }
    }.let {
      gc.resetTransform()
      gc.paintTextBox("Recursive saved (10_000 * 7) took: ${decimalFormat.format(it.toDouble(DurationUnit.MILLISECONDS))} ms", Direction.TopLeft, 10.0, 10.0, BoxStyle.none, Color.black())
    }

    measureTime {
      for (i in 0..20_000) {
        callSavedRecursive(gc, 5)
      }
    }.let {
      gc.resetTransform()
      gc.paintTextBox("Recursive saved (20_000 * 5) took: ${decimalFormat.format(it.toDouble(DurationUnit.MILLISECONDS))} ms", Direction.TopLeft, 30.0, 30.0, BoxStyle.none, Color.black())
    }

    measureTime {
      for (i in 0..100_000) {
        callSavedRecursive(gc, 1)
      }
    }.let {
      gc.resetTransform()
      gc.paintTextBox("Recursive saved (100_000 * 1) took: ${decimalFormat.format(it.toDouble(DurationUnit.MILLISECONDS))} ms", Direction.TopLeft, 50.0, 50.0, BoxStyle.none, Color.black())
    }
  }

  private fun callSavedRecursive(gc: CanvasRenderingContext, count: Int) {
    gc.saved {
      val newCount = count - 1
      if (newCount == 0) {
        return@saved
      }

      callSavedRecursive(gc, newCount)
    }
  }
}
