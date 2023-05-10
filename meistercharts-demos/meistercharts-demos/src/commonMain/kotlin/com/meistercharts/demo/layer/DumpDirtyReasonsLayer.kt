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
package com.meistercharts.demo.layer

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.model.Direction

class DumpDirtyReasonsLayer(
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  val configuration: Configuration = Configuration().also(additionalConfiguration)

  override val type: LayerType = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    gc.translate(configuration.originX, configuration.originY)

    println("----------------------- Paint Loop: ${paintingContext.loopIndex} - ${paintingContext.dirtyReasons} ----------------")
    DirtyReason.entryLabels.filter { (reason, label) ->
      paintingContext.dirtyReasons.isDirtyBecause(reason)
    }.forEach { (reason, label) ->
      println("\t$label")

      gc.fillText(label, 0.0, 0.0, Direction.TopLeft)
      gc.translate(0.0, configuration.lineHeight)
    }
  }

  class Configuration {
    var originX: Double = 120.0
    var originY: Double = 50.0

    var indentX: Double = 20.0
    var lineHeight: Double = 20.0
  }
}
