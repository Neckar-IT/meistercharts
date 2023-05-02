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
package com.meistercharts.fx

import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Size
import it.neckar.open.resources.getResourceSafe
import com.meistercharts.resources.JvmLocalResourcePaintableFactory
import it.neckar.open.unit.other.px
import javafx.scene.image.Image

/**
 * Provides JavaFX local resource paintable
 */
class LocalResourcePaintableProviderFX : JvmLocalResourcePaintableFactory {

  override fun get(relativePath: String, size: @px Size?, alignmentPoint: Coordinates): Paintable {
    return javaClass.getResourceSafe("/$relativePath").let {
      val javaFxImage = Image(it.toExternalForm())

      com.meistercharts.canvas.Image(javaFxImage, size ?: javaFxImage.naturalSize, alignmentPoint)
    }
  }
}

private val Image.naturalSize: @px Size
  get() {
    //TODO pixel ratio(?)
    return Size(this.width, this.height)
  }

