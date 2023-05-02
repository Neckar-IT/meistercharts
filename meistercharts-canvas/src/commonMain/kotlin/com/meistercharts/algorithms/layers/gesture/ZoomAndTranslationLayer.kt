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
package com.meistercharts.algorithms.layers.gesture

import com.meistercharts.algorithms.ZoomAndTranslationSupport
import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasMouseEventHandlerBroker
import com.meistercharts.canvas.events.CanvasTouchEventHandlerBroker
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Distance
import com.meistercharts.model.Size
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.EventConsumption.Ignored
import com.meistercharts.events.MouseClickEvent
import com.meistercharts.events.MouseDoubleClickEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseWheelEvent
import com.meistercharts.events.gesture.CanvasDragSupport
import com.meistercharts.events.gesture.CanvasTouchZoomAndPanSupport
import com.meistercharts.events.gesture.connectedMouseEventHandler
import com.meistercharts.events.gesture.delegate
import it.neckar.open.unit.other.pct


typealias MouseDoubleClickAction = (MouseDoubleClickEvent) -> EventConsumption
typealias MouseClickAction = (MouseClickEvent) -> EventConsumption
typealias MouseMoveAction = (MouseMoveEvent) -> EventConsumption
typealias MouseDragAction = (MouseDragEvent) -> EventConsumption
typealias MouseWheelAction = (MouseWheelEvent) -> EventConsumption
/**
 * A simple drag action that is only notified about the drags - but not the start/finish events
 * @return whether the event should be consumed
 */
typealias DragAction = (dragDistance: Distance) -> EventConsumption


/**
 * Will be called on translate using a touch screen
 */
typealias TouchPanAction = (oldCenter: @Window Coordinates, newCenter: @Window Coordinates, deltaCenter: @Zoomed Distance) -> EventConsumption

/**
 * Is called on pinch action using a touch screen
 */
typealias TouchPinchAction = (
  oldCenter: @Window Coordinates,
  newCenter: @Window Coordinates,
  oldDistanceBetweenTouches: @Zoomed Distance,
  newDistanceBetweenTouches: @Zoomed Distance,
  /**
   * The zoom factor change.
   *
   * Examples:
   * * 1.0: Nothing has changed
   * * 0.5: Zoomed out
   */
  zoomFactorChangeX: @pct Double,
  zoomFactorChangeY: @pct Double,
) -> EventConsumption

/**
 * Is called on double tap using a touch screen
 */
typealias TouchDoubleTapAction = (coordinates: @Window Coordinates) -> EventConsumption


/**
 * Zoom and translation layer that can be configured to handle different zoom and translation actions.
 *
 * It is necessary to call the different extension methods to enable any behavior. This is usually done using the configuration constructor parameter.
 */
class ZoomAndTranslationLayer(
  val zoomAndTranslationSupport: ZoomAndTranslationSupport,
  configuration: ZoomAndTranslationLayer.() -> Unit = {}
) : AbstractLayer() {
  /**
   * This layer shall be notified as late as possible.
   * Or: All other layers should have the chance to consume events *before* the zoom and translation layer is called.
   * Therefore we define the type as [LayerType.Content] and register as soon as possible.
   */
  override val type: LayerType = LayerType.Content

  override val mouseEventHandler: CanvasMouseEventHandlerBroker = CanvasMouseEventHandlerBroker().also { broker ->
    broker.delegate(object : CanvasMouseEventHandler {
      override fun onDoubleClick(event: MouseDoubleClickEvent, chartSupport: ChartSupport): EventConsumption {
        return doubleClickAction?.invoke(event) ?: Ignored
      }

      override fun onWheel(event: MouseWheelEvent, chartSupport: ChartSupport): EventConsumption {
        return mouseWheelAction?.invoke(event) ?: Ignored
      }
    }
    )
  }

  override val touchEventHandler: CanvasTouchEventHandlerBroker = CanvasTouchEventHandlerBroker()

  /**
   * The drag support detects dragging events
   */
  val dragSupport: CanvasDragSupport = CanvasDragSupport().also { dragSupport ->
    //touchEventHandler.delegate(dragSupport.connectedTouchEventHandler(2))
    mouseEventHandler.delegate(dragSupport.connectedMouseEventHandler())

    //Delegate the drag events
    dragSupport.handle(::mouseDragAction.delegate())
  }

  /**
   * Handles zooming and panning using
   */
  val canvasTouchZoomAndPanSupport: CanvasTouchZoomAndPanSupport = CanvasTouchZoomAndPanSupport().also {
    touchEventHandler.delegate(it.connectedTouchEventHandler())

    it.addHandler(object : CanvasTouchZoomAndPanSupport.Handler {
      override fun translate(oldCenter: Coordinates, newCenter: Coordinates, deltaCenter: Distance): EventConsumption {
        return touchPanAction?.invoke(oldCenter, newCenter, deltaCenter) ?: Ignored
      }

      override fun doubleTap(tapLocation: Coordinates): EventConsumption {
        return touchDoubleTapAction?.invoke(tapLocation) ?: Ignored
      }

      override fun zoomChange(oldCenter: Coordinates, newCenter: Coordinates, oldDistanceBetweenTouches: Distance, newDistanceBetweenTouches: Distance, zoomFactorChangeX: Double, zoomFactorChangeY: Double): EventConsumption {
        return touchPinchAction?.invoke(oldCenter, newCenter, oldDistanceBetweenTouches, newDistanceBetweenTouches, zoomFactorChangeX, zoomFactorChangeY) ?: Ignored
      }
    })
  }

  /**
   * The action that is notified on double clicks
   */
  private var doubleClickAction: MouseDoubleClickAction? = null

  /**
   * The double click action - only supported for mouse events
   */
  fun onDoubleClick(action: MouseDoubleClickAction) {
    doubleClickAction = action
  }

  /**
   * The action that is notified on mouse wheel
   */
  private var mouseWheelAction: MouseWheelAction? = null

  /**
   * Mouse wheel action - only supported for mouse events
   */
  fun onMouseWheel(action: MouseWheelAction) {
    mouseWheelAction = action
  }

  /**
   * The action that is notified on drag
   */
  private var mouseDragAction: CanvasDragSupport.Handler? = null

  /**
   * The drag handler - supported for mouse and pointer events
   */
  fun onMouseDrag(action: CanvasDragSupport.Handler) {
    mouseDragAction = action
  }

  /**
   * The (simplified) drag action - supported for mouse and pointer events
   */
  fun onMouseDrag(action: DragAction) {
    mouseDragAction = object : CanvasDragSupport.Handler {
      override fun isDraggingAllowedFromHere(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): Boolean {
        //Always allow dragging - we consume all events
        return true
      }

      override fun onDrag(source: CanvasDragSupport, location: Coordinates, distance: Distance, deltaTime: Double, chartSupport: ChartSupport): EventConsumption {
        return action(distance)
      }
    }
  }

  private var touchPanAction: TouchPanAction? = null

  /**
   * Sets the action that is called on panning by touch screen
   */
  fun onTouchPan(action: TouchPanAction) {
    this.touchPanAction = action
  }

  private var touchPinchAction: TouchPinchAction? = null

  /**
   * Sets the action that is called on pinching by touch screen
   */
  fun onTouchPinch(action: TouchPinchAction) {
    this.touchPinchAction = action
  }

  private var touchDoubleTapAction: TouchDoubleTapAction? = null

  /**
   * Sets the action that is called on doubleTaping by touch screen
   */
  fun onTouchDoubleTap(action: TouchDoubleTapAction) {
    this.touchDoubleTapAction = action
  }

  init {
    //Applies the configuration
    configuration()
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    //Do not paint anything
  }

  /**
   * The start location for the rubber band
   */
  var rubberBandStartLocation: @Window Coordinates? = null

  /**
   * The current location for the rubber band.
   * This location is updated on every drag
   */
  var rubberBandCurrentLocation: @Window Coordinates? = null
}

/**
 * Enable reset zoom and translation on double click
 */
fun ZoomAndTranslationLayer.resetToDefaultsOnDoubleClick() {
  onDoubleClick {
    zoomAndTranslationSupport.resetToDefaults()
    Consumed
  }
}

/**
 * Enable reset zoom and translation on double tap (touch screen)
 */
fun ZoomAndTranslationLayer.resetToDefaultsOnDoubleTap() {
  onTouchDoubleTap {
    zoomAndTranslationSupport.resetToDefaults()
    Consumed
  }
}

/**
 * Enables zooming on mouse wheel
 */
fun ZoomAndTranslationLayer.zoomOnMouseWheel(
  /**
   * The configuration for the mouse wheel zoom
   */
  config: MouseWheelZoomConfiguration = MouseWheelZoomConfiguration()
) {
  onMouseWheel { event ->
    if (event.delta + 0.0 == 0.0) {
      return@onMouseWheel Ignored
    }

    val doZoomX = config.zoomAxisSelection.containsX && (event.modifierCombination == config.zoomXModifier || event.modifierCombination == config.zoomXandYModifier)
    val doZoomY = config.zoomAxisSelection.containsY && (event.modifierCombination == config.zoomYModifier || event.modifierCombination == config.zoomXandYModifier)

    val zoomAxis = AxisSelection.get(doZoomX, doZoomY)

    if (zoomAxis == AxisSelection.None) {
      return@onMouseWheel Ignored
    }
    zoomAndTranslationSupport.modifyZoom(event.delta < 0, zoomAxis, event.coordinates)
    Consumed
  }
}

/**
 * Enable translate on drag
 */
fun ZoomAndTranslationLayer.translateOnMouseDrag(axis: AxisSelection = AxisSelection.Both) {
  onMouseDrag { distance ->
    zoomAndTranslationSupport.translateWindow(axis, distance.x, distance.y)
    Consumed
  }
}

/**
 * Enable translate on touch drag
 */
fun ZoomAndTranslationLayer.translateOnTouchDrag(axis: AxisSelection = AxisSelection.Both) {
  onTouchPan { _, _, deltaCenter ->
    zoomAndTranslationSupport.translateWindow(axis, deltaCenter.x, deltaCenter.y)
    Consumed
  }
}

/**
 * Enable zooming on pinch
 */
fun ZoomAndTranslationLayer.zoomOnPinch() {
  onTouchPinch { _, newCenter, _, _, zoomFactorChangeX, zoomFactorChangeY ->
    zoomAndTranslationSupport.modifyZoom(zoomFactorChangeX, zoomFactorChangeY, zoomCenter = newCenter)
    Consumed
  }
}

/**
 * Enables the rubber band zoom
 */
fun ZoomAndTranslationLayer.rubberBandZoom() {
  onMouseDrag(object : CanvasDragSupport.Handler {
    override fun isDraggingAllowedFromHere(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): Boolean {
      rubberBandStartLocation = location
      return true
    }

    override fun onDrag(source: CanvasDragSupport, location: @Window Coordinates, distance: Distance, deltaTime: Double, chartSupport: ChartSupport): EventConsumption {
      rubberBandCurrentLocation = location
      chartSupport.markAsDirty()
      return Consumed
    }

    override fun onFinish(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): EventConsumption {
      //Calculate the zoom
      rubberBandStartLocation?.let { rubberBandStartLocation: @Window Coordinates ->
        rubberBandCurrentLocation?.let { rubberBandCurrentLocation: @Window Coordinates ->

          //The size of the rubber band
          @Zoomed val rubberBandSize: Size = rubberBandStartLocation.delta(rubberBandCurrentLocation).abs().asSize()

          //Keep the same aspect ratio as before, find the size (larger) with the current aspect ratio
          @Zoomed val relevantRubberBandSize = rubberBandSize.containWithAspectRatio(chartSupport.rootChartState.zoom.aspectRatio)

          val factorX = 1 / relevantRubberBandSize.width * chartSupport.canvas.width * chartSupport.rootChartState.zoom.scaleX
          val factorY = 1 / relevantRubberBandSize.height * chartSupport.canvas.height * chartSupport.rootChartState.zoom.scaleY

          @Window val center = rubberBandStartLocation.center(rubberBandCurrentLocation)
          zoomAndTranslationSupport.setZoom(factorX, factorY, center)
        }
      }

      //Reset the values
      rubberBandStartLocation = null
      rubberBandCurrentLocation = null

      chartSupport.markAsDirty()
      return Consumed
    }
  })
}

/**
 * Adds a zoom and pan layer.
 *
 * If necessary also adds the [RubberBandVisualizationLayer]
 */
fun Layers.addZoomAndTranslation(
  zoomAndTranslationSupport: ZoomAndTranslationSupport,
  configuration: ZoomAndTranslationLayer.() -> Unit = {}
): ZoomAndTranslationLayer {
  return ZoomAndTranslationLayer(zoomAndTranslationSupport, configuration).apply {
    addLayer(this)

    //add the rubber band visualization layer
    val rubberBandVisualizationLayer = RubberBandVisualizationLayer(RubberBandVisualizationLayer.Data({ rubberBandStartLocation }, { rubberBandCurrentLocation }))
    addLayer(rubberBandVisualizationLayer)
  }
}
