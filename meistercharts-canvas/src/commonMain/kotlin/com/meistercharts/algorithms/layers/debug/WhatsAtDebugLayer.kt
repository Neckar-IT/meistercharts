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
package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.whatsAt
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.asDistance
import com.meistercharts.whatsat.WhatsAtResult
import com.meistercharts.whatsat.WhatsAtSupport
import it.neckar.open.collections.fastForEach
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseDownEvent

/**
 * Layer that visualizes the results of the [com.meistercharts.whatsat.WhatsAtSupport] at a given location
 */
class WhatsAtDebugLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  /**
   * The coordinates of the last request to the [WhatsAtSupport]
   */
  private var resultsCoords: Coordinates? = null

  /**
   * The results from the whatsAt service
   */
  private val results = mutableMapOf<WhatsAtSupport.Precision, WhatsAtResult?>()

  private var resultStrings = listOf<String>()

  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      val whatsAtSupport = chartSupport.whatsAt

      //Update all results on mouse down
      resultsCoords = event.coordinates

      results.clear()
      results[WhatsAtSupport.Exact] = whatsAtSupport.whatsAt(event.coordinates, WhatsAtSupport.Exact, chartSupport)
      results[WhatsAtSupport.Nearest] = whatsAtSupport.whatsAt(event.coordinates, WhatsAtSupport.Nearest, chartSupport)
      results[WhatsAtSupport.CloseTo.VeryClose] = whatsAtSupport.whatsAt(event.coordinates, WhatsAtSupport.CloseTo.VeryClose, chartSupport)
      results[WhatsAtSupport.CloseTo.CloseTo] = whatsAtSupport.whatsAt(event.coordinates, WhatsAtSupport.CloseTo.CloseTo, chartSupport)

      updateResultStrings()

      chartSupport.markAsDirty()
      return EventConsumption.Consumed
    }
  }

  private fun updateResultStrings() {
    resultStrings = results.values
      .asSequence()
      .filterNotNull()
      .flatMap {
        buildList {
          add(it.precision.toString())
          it.elements.fastForEach { resultElement ->
            add("\tType: ${resultElement.type.type}")
            resultElement.label?.let { label -> add("\tLabel: $label") }
            resultElement.valueFormatted?.let { valueFormatted -> add("\tValue: $valueFormatted") }
            resultElement.location?.let { valueFormatted -> add("\tLocation: $valueFormatted") }
            resultElement.boundingBox?.let { valueFormatted -> add("\tBounding Box: $valueFormatted") }
            resultElement.data?.let { data -> add("\tData: $data") }
          }
        }
      }.toList()
  }


  override fun paint(paintingContext: LayerPaintingContext) {
    val chartSupport = paintingContext.chartSupport
    val gc = paintingContext.gc

    //Paint the whatsAt results
    resultsCoords?.let {
      gc.translate(it.asDistance())
      gc.paintMark()

      if (resultStrings.isEmpty()) {
        gc.paintTextBox("No results found!", Direction.TopLeft, 5.0, 5.0)
      } else {
        gc.paintTextBox(resultStrings, Direction.TopLeft, 5.0, 5.0)
      }

    }
  }
}

fun Layers.addWhatsAtDebugLayer(): WhatsAtDebugLayer {
  return WhatsAtDebugLayer().also {
    addLayer(it)
  }
}
