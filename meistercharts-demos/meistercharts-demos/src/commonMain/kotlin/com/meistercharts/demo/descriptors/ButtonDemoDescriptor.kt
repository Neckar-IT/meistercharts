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
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.LayerSupport
import com.meistercharts.canvas.LineSpacing
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasPointerEventHandler
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.paintable.Button
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.paintable.DefaultPrimaryButtonPainter
import com.meistercharts.canvas.paintable.DefaultSecondaryButtonPainter
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.toButtonPainter
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.strokeCross
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.events.PointerCancelEvent
import com.meistercharts.events.PointerDownEvent
import com.meistercharts.events.PointerEnterEvent
import com.meistercharts.events.PointerLeaveEvent
import com.meistercharts.events.PointerMoveEvent
import com.meistercharts.events.PointerOutEvent
import com.meistercharts.events.PointerOverEvent
import com.meistercharts.events.PointerUpEvent
import it.neckar.open.i18n.TextKey
import com.meistercharts.resources.Icons
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.other.px

/**
 */
class ButtonDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Buttons"

  //language=HTML
  override val description: String = """<h3>Visualizes Buttons</h3>"""
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {

        contentAreaSizingStrategy = BindContentAreaSize2ContentViewport()

        configure {
          val layerWithButtons = LayerWithButtons(this)

          layers.addClearBackground()
          layers.addLayer(layerWithButtons)

          configurableBoolean("enable buttons") {
            value = true
            onChange {
              layerWithButtons.primaryButton.state = layerWithButtons.primaryButton.state.copy(enabled = it)
              layerWithButtons.secondaryButton.state = layerWithButtons.secondaryButton.state.copy(enabled = it)
              layerWithButtons.buttonImage1.state = layerWithButtons.buttonImage1.state.copy(enabled = it)
              layerWithButtons.buttonImageToggle.state = layerWithButtons.buttonImageToggle.state.copy(enabled = it)
              layerWithButtons.buttonZoomIn.state = layerWithButtons.buttonZoomIn.state.copy(enabled = it)
            }
          }
        }
      }
    }
  }

  private class LayerWithButtons(private val layerSupport: LayerSupport) : AbstractLayer() {
    override val type: LayerType
      get() = LayerType.Content

    val buttonCoordinates = mutableMapOf<Button, Coordinates>()

    private fun buttonBounds(button: Button, chartSupport: ChartSupport): Rectangle {
      return Rectangle(buttonCoordinates[button] ?: throw IllegalArgumentException("invalid button"), button.boundingBox.size)
    }

    private val buttonHitTest: (Coordinates, Button, ChartSupport) -> Boolean = { coordinates, button, chartSupport -> buttonBounds(button, chartSupport).contains(coordinates) }

    val primaryButton = Button(DefaultPrimaryButtonPainter(TextKey.simple("Primary")), 100.0, 40.0)
    val secondaryButton = Button(DefaultSecondaryButtonPainter(TextKey.simple("Secondary")), 100.0, 40.0)

    val buttonImage1 = Button({ _: ButtonState ->
      Icons.error(Size.PX_40, Color.red)
    }.toButtonPainter(), 40.0, 40.0)

    val buttonImageToggle = Button(
      { buttonState: ButtonState ->
        val color = if (buttonState.selected) Color.green else Color.blue
        val size = if (buttonState.hover || buttonState.selected) Size.PX_40 else Size.PX_30
        if (buttonState.selected) Icons.autoScale(size, color) else Icons.noAutoScale(size, color)
      }.toButtonPainter(), 40.0, 40.0
    )

    val buttonZoomIn = Button({ buttonState: ButtonState ->
      when {
        buttonState.disabled -> ZoomInPaintable(Color.web("#8A8A8A"), Color.web("#CCCCCC"), 44.0, 44.0)
        buttonState.pressed  -> ZoomInPaintable(Color.white, Color.orange, 44.0, 44.0)
        buttonState.hover    -> ZoomInPaintable(Color.white, Color.web("#9452F3"), 36.0, 36.0)
        buttonState.focused  -> ZoomInPaintable(Color.white, Color.web("#873DF2"), 36.0, 36.0)
        else                 -> ZoomInPaintable(Color.white, Color.web("#6200EE"), 36.0, 36.0)
      }
    }.toButtonPainter(), 44.0, 44.0)

    private var lastClicked = ""

    init {
      buttonCoordinates[primaryButton] = Coordinates.none
      primaryButton.action {
        lastClicked = "primary"
        layerSupport.markAsDirty()
      }
      primaryButton.stateProperty.consume { layerSupport.markAsDirty() }

      buttonCoordinates[secondaryButton] = Coordinates.none
      secondaryButton.action {
        lastClicked = "secondary"
        layerSupport.markAsDirty()
      }
      secondaryButton.stateProperty.consume { layerSupport.markAsDirty() }

      buttonCoordinates[buttonImage1] = Coordinates.none
      buttonImage1.action {
        lastClicked = "buttonImage1"
        layerSupport.markAsDirty()
      }
      buttonImage1.state = buttonImage1.state.copy(enabled = false)
      buttonImage1.stateProperty.consume { layerSupport.markAsDirty() }

      buttonCoordinates[buttonImageToggle] = Coordinates.none
      buttonImageToggle.action {
        lastClicked = "buttonImageToggle"
        buttonImageToggle.selected = !buttonImageToggle.selected
        layerSupport.markAsDirty()
      }
      buttonImageToggle.stateProperty.consume { layerSupport.markAsDirty() }

      buttonCoordinates[buttonZoomIn] = Coordinates.none
      buttonZoomIn.action {
        lastClicked = "buttonZoomIn"
        layerSupport.markAsDirty()
      }
      buttonZoomIn.stateProperty.consume { layerSupport.markAsDirty() }
    }

    override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
      override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
        return EventConsumption.consumeIf {
          buttonCoordinates.keys
            .filter { buttonHitTest(event.coordinates, it, chartSupport) }
            .onEach { it.onDown() }
            .any()
        }
      }

      override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
        return EventConsumption.consumeIf {
          buttonCoordinates.keys
            .filter { buttonHitTest(event.coordinates, it, chartSupport) }
            .onEach { it.onUp(chartSupport, event.coordinates) }
            .any()
        }
      }

      override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
        val coordinates = event.coordinates
        if (coordinates == null) {
          buttonCoordinates.keys.forEach { it.noHover() }
        } else {
          buttonCoordinates.keys
            .filter { buttonHitTest(coordinates, it, chartSupport) }
            .forEach { it.hover() }

          buttonCoordinates.keys
            .filter { !buttonHitTest(coordinates, it, chartSupport) }
            .forEach { it.noHover() }
        }

        return EventConsumption.Ignored
      }
    }

    override val pointerEventHandler: CanvasPointerEventHandler = object : CanvasPointerEventHandler {
      override fun onOver(event: PointerOverEvent, chartSupport: ChartSupport): EventConsumption {
        val coordinates = event.pointer.coordinates

        buttonCoordinates.keys
          .filter {
            buttonHitTest(coordinates, it, chartSupport)
          }
          .forEach { it.hover() }

        return EventConsumption.Ignored
      }

      override fun onEnter(event: PointerEnterEvent, chartSupport: ChartSupport): EventConsumption {
        val coordinates = event.pointer.coordinates
        buttonCoordinates.keys
          .filter { buttonHitTest(coordinates, it, chartSupport) }
          .forEach { it.hover() }

        return EventConsumption.Ignored
      }

      override fun onDown(event: PointerDownEvent, chartSupport: ChartSupport): EventConsumption {
        val coordinates = event.pointer.coordinates

        return EventConsumption.consumeIf {
          buttonCoordinates.keys
            .filter { buttonHitTest(coordinates, it, chartSupport) }
            .onEach { it.onDown() }
            .any()
        }
      }

      override fun onMove(event: PointerMoveEvent, chartSupport: ChartSupport): EventConsumption {
        val coordinates = event.pointer.coordinates

        buttonCoordinates.keys
          .filter { buttonHitTest(coordinates, it, chartSupport) }
          .forEach { it.hover() }

        buttonCoordinates.keys
          .filter { !buttonHitTest(event.pointer.coordinates, it, chartSupport) }
          .forEach { it.noHover() }

        return EventConsumption.Ignored
      }

      override fun onUp(event: PointerUpEvent, chartSupport: ChartSupport): EventConsumption {
        val coordinates = event.pointer.coordinates
        return EventConsumption.consumeIf {
          buttonCoordinates.keys
            .filter { buttonHitTest(coordinates, it, chartSupport) }
            .onEach { it.onUp(chartSupport, coordinates) }
            .any()
        }
      }

      override fun onCancel(event: PointerCancelEvent, chartSupport: ChartSupport): EventConsumption {
        buttonCoordinates.keys.forEach { it.noHover() }
        return EventConsumption.Ignored
      }

      override fun onOut(event: PointerOutEvent, chartSupport: ChartSupport): EventConsumption {
        buttonCoordinates.keys.forEach { it.noHover() }
        return EventConsumption.Ignored
      }

      override fun onLeave(event: PointerLeaveEvent, chartSupport: ChartSupport): EventConsumption {
        buttonCoordinates.keys.forEach { it.noHover() }
        return EventConsumption.Ignored
      }
    }

    override fun layout(paintingContext: LayerPaintingContext) {
      super.layout(paintingContext)
      buttonCoordinates[primaryButton] = Coordinates.of(10.0, 10.0)
      buttonCoordinates[secondaryButton] = Coordinates.of(60.0, 60.0)
      buttonCoordinates[buttonImage1] = Coordinates.of(110.0, 110.0)
      buttonCoordinates[buttonImageToggle] = Coordinates.of(160.0, 160.0)
      buttonCoordinates[buttonZoomIn] = Coordinates.of(210.0, 210.0)
    }

    override fun paint(paintingContext: LayerPaintingContext) {

      val gc = paintingContext.gc
      buttonCoordinates.forEach { entry ->
        gc.saved {
          entry.key.paint(paintingContext, entry.value)
        }
      }

      gc.saved {
        gc.translate(0.0, gc.height)
        gc.paintTextBox(
          listOf(
            "last clicked = $lastClicked",
            "primaryButton state = ${primaryButton.state}",
            "secondaryButton state = ${secondaryButton.state}",
            "buttonImage1 state = ${buttonImage1.state}",
            "buttonImageToggle state = ${buttonImageToggle.state}",
            "buttonZoomIn state = ${buttonZoomIn.state}"
          ), LineSpacing.Single, HorizontalAlignment.Left, Direction.BottomLeft, 5.0, 5.0, BoxStyle.none, Color.black
        )
      }
    }
  }

  private class ZoomInPaintable(
    private val foreground: Color,
    private val background: Color,
    val width: @px Double,
    val height: @px Double
  ) : Paintable {

    val boundingBox = Rectangle(0.0, 0.0, width, height)

    override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
      return boundingBox
    }

    override fun paint(paintingContext: LayerPaintingContext, x: @px Double, y: @px Double) {
      val gc = paintingContext.gc
      gc.fill(background)
      gc.fillRect(x, y, width, height)
      gc.stroke(foreground)
      gc.lineWidth = 3.0
      gc.strokeCross(x + width * 0.5, y + height * 0.5, 16.0)
    }
  }


}

