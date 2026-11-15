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
import com.meistercharts.canvas.text.LineSpacing
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.geometry.HorizontalAlignment
import com.meistercharts.style.BoxStyle

/**
 *
 */
class CanvasLowLevelDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("92843414-0009-4e63-aa98-bb2e349d683f")
  override val name: String = "Canvas Low Level"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val category: DemoCategory
    get() = DemoCategory.LowLevelTests

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(MyLowLevelLayer())
        }
      }
    }
  }
}

private class MyLowLevelLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    gc.translate(20.0, 30.0)
    gc.translation = Distance(17.0, 22.3)

    gc.paintTextBox(
      listOf(
        "Scale: ${gc.scaleX} / ${gc.scaleY}",
        "Translation: ${gc.translation.format()}"
      ), LineSpacing.Single, HorizontalAlignment.Left, Direction.TopLeft, 5.0, 5.0, BoxStyle.none, Color.black()
    )

  }
}
