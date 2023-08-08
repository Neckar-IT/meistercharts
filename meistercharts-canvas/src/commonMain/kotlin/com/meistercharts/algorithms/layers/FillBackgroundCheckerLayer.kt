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

import com.meistercharts.color.Color
import com.meistercharts.annotations.Zoomed
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.kotlin.lang.toIntCeil

/**
 * Fills the canvas with a background checker pattern
 */
class FillBackgroundCheckerLayer(
  configuration: Configuration.() -> Unit = {}
) : AbstractLayer() {


  override val type: LayerType = LayerType.Background

  val style: Configuration = Configuration().also(configuration)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val cols = (gc.width / style.segmentWith).toIntCeil()
    val rows = (gc.height / style.segmentHeight).toIntCeil()

    cols.fastFor { col ->
      rows.fastFor { row ->
        gc.fill(
          if ((col + row) % 2 == 0) style.background0 else style.background1
        )

        gc.fillRect(col * style.segmentWith, row * style.segmentHeight, style.segmentWith, style.segmentHeight)
      }
    }
  }

  class Configuration {
    /**
     * The color to be used as background
     */
    var background0: Color = Color.lightgray
    var background1: Color = Color.white

    var segmentWith: @Zoomed Double = 15.0
    var segmentHeight: @Zoomed Double = 15.0
  }
}

/**
 * Adds a background layer with checkers
 */
fun Layers.addBackgroundChecker(): FillBackgroundCheckerLayer {
  return FillBackgroundCheckerLayer().also {
    addLayer(it)
  }
}
