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

import com.meistercharts.algorithms.KeepCenterOnWindowResize
import com.meistercharts.algorithms.KeepOriginOnWindowResize
import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.impl.debugEnabled
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.clipped
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.mock.MockCanvasRenderingContext
import com.meistercharts.canvas.PaintingLoopIndex
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Insets

class ClippingLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Clipping"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        fun Insets.toStringWithInts(): String {
          return "top=${top.toInt()}, right=${right.toInt()}, bottom=${bottom.toInt()}, left=${left.toInt()}"
        }

        configure {
          chartSupport.pixelSnapSupport.snapConfiguration = SnapConfiguration.None

          layers.addClearBackground()
          val clippingLayer = MyFillBgLayer().clipped(Insets.of(10.0, 20.0, 30.0, 40.0)).also {
            layers.addLayer(it)
          }

          val mockContext = LayerPaintingContext(MockCanvasRenderingContext(), this, 0.0, 0.0, PaintingLoopIndex(17))

          layers.addText({ _, _ -> listOf("insets: ${clippingLayer.style.insets(mockContext).toStringWithInts()}") }, { textColor = Color.gray })

          configurableList("Snap config", SnapConfiguration.None, listOf(SnapConfiguration.None, SnapConfiguration.Both, SnapConfiguration.OnlyX, SnapConfiguration.OnlyY)) {
            onChange {
              chartSupport.pixelSnapSupport.snapConfiguration = it
              markAsDirty()
            }
            converter = {
              it.name
            }
          }

          configurableList("Resize behavior", chartSupport.windowResizeBehaviorProperty.value, listOf(KeepOriginOnWindowResize, KeepCenterOnWindowResize, ResetToDefaultsOnWindowResize)) {
            onChange {
              chartSupport.windowResizeBehaviorProperty.value = it
            }
            converter = {
              it::class.simpleName ?: it.toString()
            }
          }

          configurableInsetsSeparate("Insets") {
            value = clippingLayer.style.insets(mockContext)
            min = 0.0
            max = 200.0
            onChange { newValue ->
              clippingLayer.style.insets = { newValue }
              markAsDirty()
            }
          }
        }

        zoomAndTranslationModifier {
          minZoom(0.1, 0.1)
          maxZoom(5.0, 5.0)
          contentAlwaysBarelyVisible()
          debugEnabled()
        }
      }
    }
  }
}

private class MyFillBgLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content
  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.fill(Color.orange)
    gc.fillRect(gc.boundingBox)
  }
}
