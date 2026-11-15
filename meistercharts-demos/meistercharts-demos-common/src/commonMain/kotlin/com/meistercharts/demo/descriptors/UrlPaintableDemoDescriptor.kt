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

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.color.Color
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableList
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import it.neckar.open.http.Url

class UrlPaintableDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("ba2eebca-f818-4d8c-b738-684196e7ecf3")
  override val name: String = "URL paintable"
  override val description: String = "Displays an image loaded from a URL as a paintable."
  override val category: DemoCategory = DemoCategory.Paintables
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  val urls = listOf(
    Url.absolute("https://a.tile.openstreetmap.de/12/2138/1420.png"),
    Url.absolute("https://www.neckar.it/img/bg-cta.jpg"),
    Url.data("data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0Ij4KICA8cGF0aCBkPSJNMCAwaDI0djI0SDB6IiBmaWxsPSJub25lIiAvPgogIDxwYXRoIGQ9Ik0xOS4zNSAxMC4wNEMxOC42NyA2LjU5IDE1LjY0IDQgMTIgNCA5LjExIDQgNi42IDUuNjQgNS4zNSA4LjA0IDIuMzQgOC4zNiAwIDEwLjkxIDAgMTRjMCAzLjMxIDIuNjkgNiA2IDZoMTNjMi43NiAwIDUtMi4yNCA1LTUgMC0yLjY0LTIuMDUtNC43OC00LjY1LTQuOTZ6TTE0IDEzdjRoLTR2LTRIN2w1LTUgNSA1aC0zeiIgLz4KPC9zdmc+Cg==")
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {

        configure {
          layers.addClearBackground()

          val layer = MyUrlPaintableSizeLayer(urls[0])
          layers.addLayer(layer)

          configurableList("URL", layer.url, urls) {
            onChange {
              layer.url = it
              markAsDirty()
            }
          }

          configurable("Natural Size", layer::naturalSize)

          configurableDouble("image width", layer.imageSize.width) {
            max = 1000.0

            onChange {
              layer.imageSize = layer.imageSize.withWidth(it)
              markAsDirty()
            }
          }
          configurableDouble("image height", layer.imageSize.height) {
            max = 1000.0

            onChange {
              layer.imageSize = layer.imageSize.withHeight(it)
              markAsDirty()
            }
          }

          configurableDouble("Base Point X", layer.basePoint.x) {
            min = -500.0
            max = 1000.0

            onChange {
              layer.basePoint = layer.basePoint.withX(it)
              markAsDirty()
            }
          }
          configurableDouble("Base Point Y", layer.basePoint.y) {
            min = -500.0
            max = 1000.0

            onChange {
              layer.basePoint = layer.basePoint.withY(it)
              markAsDirty()
            }
          }

          configurable("Paint in BoundingBox", layer::paintInBoundingBox)
        }
      }
    }
  }
}

private class MyUrlPaintableSizeLayer(var url: Url) : AbstractLayer() {
  var imageSize: Size = Size.PX_120

  var naturalSize: Boolean = true

  var basePoint: Coordinates = Coordinates.origin

  var paintInBoundingBox: Boolean = false

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val paintable = if (naturalSize) {
      UrlPaintable.naturalSize(url, basePoint)
    } else {
      UrlPaintable.fixedSize(url, imageSize, basePoint)
    }


    gc.translateToCenter()

    if (paintable.imageLoaded) {

      if (paintInBoundingBox) {
        val size = Size.PX_90
        gc.saved {
          paintable.paintInBoundingBox(paintingContext, Coordinates.none, Direction.TopLeft, size)
        }
        gc.stroke(Color.blue)
        gc.fill(Color.blue)
        gc.strokeRect(Coordinates.none, size)
        gc.fillText("Forced Bounding box", 0.0, 0.0, Direction.BottomLeft)
        gc.fillText("Paintable size: ${paintable.boundingBox(paintingContext).size.format()}", 0.0, 0.0, Direction.TopLeft)
      } else {
        paintable.paint(paintingContext, Coordinates.none)
      }

    } else {
      gc.fillText("Image not loaded", Coordinates.origin, Direction.Center)
    }

    gc.stroke(Color.orangered)
    gc.strokeRect(paintable.boundingBox(paintingContext))
    gc.paintMark(Coordinates.origin)
  }
}

