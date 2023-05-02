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
import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.model.Rectangle
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import it.neckar.logging.LoggerFactory

/**
 * Class to demonstrate the consumption of layer-interactions
 */
class InteractionConsumedDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Interactive Consumed"
  override val description: String = "## How to consume events (see console)"
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(createInteractionLayer(layers.size.toString(), Color.orange, Rectangle(10.0, 10.0, 50.0, 50.0)))
          layers.addLayer(createInteractionLayer(layers.size.toString(), Color.red, Rectangle(25.0, 25.0, 150.0, 150.0)))
          layers.addLayer(createInteractionLayer(layers.size.toString(), Color.yellow, Rectangle(50.0, 50.0, 50.0, 50.0)))
        }
      }
    }
  }

  fun createInteractionLayer(title: String, backgroundColor: Color, bounds: Rectangle): Layer {
    return object : AbstractLayer() {
      override val type: LayerType
        get() = LayerType.Content

      override val mouseEventHandler: CanvasMouseEventHandler? = object : CanvasMouseEventHandler {
        override fun onClick(event: MouseClickEvent, chartSupport: ChartSupport): EventConsumption {
          logger.debug("$title: onClick")
          if (bounds.contains(event.coordinates)) {
            logger.debug("$title: onClick consume")
            return EventConsumption.Consumed
          }

          return EventConsumption.Ignored
        }

        override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
          logger.debug("$title: onDoubleClick")
          if (bounds.contains(event.coordinates)) {
            logger.debug("$title: onDoubleClick consume")
            return EventConsumption.Consumed
          }
          return EventConsumption.Ignored
        }
      }

      override fun paint(paintingContext: LayerPaintingContext) {
        val gc = paintingContext.gc
        gc.fillStyle(backgroundColor)
        gc.fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight())

        gc.fillStyle(Color.black)
        gc.fillText(title, bounds.getX() + 3.0, bounds.getY() + 3.0, Direction.TopLeft, 0.0, 0.0, bounds.getWidth() - 6.0)
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.demo.descriptors.InteractionConsumedDemoDescriptor")
  }
}

