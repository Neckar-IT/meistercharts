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
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.fx.CanvasRenderingContextFX

/**
 */
class SvgGcDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "SVG GC Demo"
  override val description: String = "SVG GC Demo"
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addLayer(SvgGcLayer())
        }
      }
    }
  }
}

private class SvgGcLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val fxContext = paintingContext.gc as CanvasRenderingContextFX
    fxContext.stroke(Color.orange)
    fxContext.context.appendSVGPath("M248 167.5l64.9 98.8H183.1l64.9-98.8zM496 256c0 136.9-111.1 248-248 248S0 392.9 0 256 111.1 8 248 8s248 111.1 248 248zm-99.8 82.7L248 115.5 99.8 338.7h30.4l33.6-51.7h168.6l33.6 51.7h30.2z")
    fxContext.stroke()
  }
}
