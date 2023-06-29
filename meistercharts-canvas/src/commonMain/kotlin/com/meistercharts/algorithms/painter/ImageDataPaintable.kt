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
package com.meistercharts.algorithms.painter

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.geometry.Rectangle

/**
 * A Paintable that is able to paint images with inline base64 encoded content.
 * The URI is expected to start with "data:image"
 */
@Suppress("unused")
class ImageDataPaintable(uri: String) : Paintable {
  init {
    require(uri.startsWith(prefix)) { "Invalid uri: <$uri>. Expected to start with $prefix" }
  }

  val delegate: Paintable = UrlPaintable.naturalSize(uri)

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return delegate.boundingBox(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    delegate.paint(paintingContext, x, y)
  }

  companion object {
    const val prefix: String = "data:image"
  }
}

