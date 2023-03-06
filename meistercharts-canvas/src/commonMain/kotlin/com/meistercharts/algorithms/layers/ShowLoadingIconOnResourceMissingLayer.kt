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
package com.meistercharts.algorithms.layers

import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import com.meistercharts.resources.Icons

/**
 * Shows a loading icon if a resource is missing
 */
class ShowLoadingIconOnResourceMissingLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  val style: Style = Style().also(styleConfiguration)


  override fun paint(paintingContext: LayerPaintingContext) {
    if (!paintingContext.missingResources.isEmpty()) {
      val gc = paintingContext.gc
      gc.clear()
      style.icon.paintInBoundingBox(paintingContext, gc.centerX, gc.centerY, Direction.Center)
    }
  }

  class Style {
    /**
     * The icon that is painted
     */
    var icon: Paintable = Icons.hourglass(size = Size.PX_60)
  }
}

/**
 * Adds a layer that paints an icon if resources are missing
 */
fun Layers.addShowLoadingOnMissingResources(styleConfiguration: ShowLoadingIconOnResourceMissingLayer.Style.() -> Unit = {}): ShowLoadingIconOnResourceMissingLayer {
  return ShowLoadingIconOnResourceMissingLayer(styleConfiguration).also {
    addLayer(it)
  }
}
