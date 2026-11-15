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
import com.meistercharts.algorithms.layers.clipped
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.DirtyReasonBitSet
import com.meistercharts.canvas.LayerIndex
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.mock.MockCanvasRenderingContext
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.loop.PaintingLoopIndex
import com.meistercharts.model.Insets
import com.meistercharts.resize.KeepCenterOnWindowResize
import com.meistercharts.resize.KeepOriginOnWindowResize
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.zoom.debugEnabled

class ClippingLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("b2453bb3-0a19-4e8f-b6d3-840ac5b65cb3")
  override val name: String = "Clipping"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
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

          val mockContext = LayerPaintingContext(
            gc = MockCanvasRenderingContext(), layerSupport = this, frameTimestamp = 0.0, frameMonotonicMillis = 0.0, frameDelta = 0.0, loopIndex = PaintingLoopIndex(17),
            layerLayoutIndex = LayerIndex.unknown, layerPaintIndex = LayerIndex.unknown,
            dirtyReasons = DirtyReasonBitSet.Unknown,
          )

          layers.addText({ _, _ -> listOf("insets: ${clippingLayer.configuration.insets(mockContext).toStringWithInts()}") }, { textColor = Color.gray })

          configurableList("Snap config", SnapConfiguration.None, listOf(SnapConfiguration.None, SnapConfiguration.Both, SnapConfiguration.OnlyX, SnapConfiguration.OnlyY)) {
            onChange {
              chartSupport.pixelSnapSupport.snapConfiguration = it
              markAsDirty()
            }
            converter = {
              it.name
            }
          }

          configurableListWithProperty("Resize behavior", chartSupport::windowResizeBehavior, listOf(KeepOriginOnWindowResize, KeepCenterOnWindowResize, ResetToDefaultsOnWindowResize)) {
            converter = {
              it::class.simpleName ?: it.toString()
            }
          }

          configurableInsetsSeparate("Insets") {
            value = clippingLayer.configuration.insets(mockContext)
            min = 0.0
            max = 200.0
            onChange { newValue ->
              clippingLayer.configuration.insets = { newValue }
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
