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
package com.meistercharts.canvas.resize

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.MouseCursor
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasMouseEventHandlerBroker
import com.meistercharts.canvas.layout.buffer.BoundsMultiBuffer
import com.meistercharts.canvas.paintable.ResizeHandlesPaintable
import com.meistercharts.canvas.resizeHandlesSupport
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.gesture.CanvasDragSupport
import com.meistercharts.events.gesture.connectedMouseEventHandler
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseMoveEvent
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.open.dispose.disposeOn
import it.neckar.open.observable.ObservableObject
import it.neckar.open.unit.other.px

/**
 * This class supports resizing of objects on the canvas using handles
 */
class ResizeByHandlesLayer : AbstractLayer() {

  override val type: LayerType = LayerType.Content

  val configuration: Configuration = Configuration()

  /**
   * Contains the current ui state
   */
  private val uiStateProperty: ObservableObject<ResizeByHandlesLayerState> = ObservableObject(DefaultState)

  /**
   * The current ui state
   */
  var uiState: ResizeByHandlesLayerState by uiStateProperty
    private set

  /**
   * Contains the locations for each handle direction
   *
   * Attention! The order matters! It is well known - within this class.
   * Do not depend on the order outside of this class (this might not be the same order as in the enum [Direction])
   */
  private val handleBounds = BoundsMultiBuffer()
  private var handlesVisible = false

  /**
   * Ends a gesture whose release never arrived. Going through [DefaultState] separates it from the gesture the press
   * starts - a press on the same handle would otherwise assign an equal state and notify nobody.
   */
  private fun endRunningGesture() {
    if (uiState is DraggingHandle) {
      uiState = DefaultState
    }
  }

  private val dragSupport: CanvasDragSupport = CanvasDragSupport().also {
    it.handle(object : CanvasDragSupport.Handler {
      override fun isDraggingAllowedFromHere(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): Boolean {
        val handleDirection = findHandleDirection(location) ?: return false
        uiState = DraggingHandle(handleDirection)
        return true
      }

      override fun onDrag(source: CanvasDragSupport, location: Coordinates, distance: Distance, deltaTime: Double, chartSupport: ChartSupport): EventConsumption {
        //The drag support outlives the gesture: the resizable can vanish under a pointer that is still down
        val dragging = uiState as? DraggingHandle ?: return EventConsumption.Ignored

        chartSupport.resizeHandlesSupport.notifyResize(dragging.handleDirection, source.totalDistanceTo(location))
        return EventConsumption.Consumed
      }

      override fun onFinish(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): EventConsumption {
        //A release that ends no gesture of this layer belongs to the layers below
        if (uiState !is DraggingHandle) return EventConsumption.Ignored

        //The gesture is over - what is left is the hover under the pointer
        uiState = DefaultState.hoveringAboveHandle(findHandleDirection(location))
        return EventConsumption.Consumed
      }
    })
  }

  /**
   * Three delegates, one job each. The drag support sits between them: a press has to end the leftover gesture before
   * it starts the next, and the hover behind it consumes moves above a handle, which would hide them from the drag.
   */
  override val mouseEventHandler: CanvasMouseEventHandler = CanvasMouseEventHandlerBroker().apply {
    delegate(
      object : CanvasMouseEventHandler {
        /**
         * See [endRunningGesture]. The press itself belongs to whoever it hits, on a handle or not.
         */
        override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
          endRunningGesture()
          return EventConsumption.Ignored
        }
      }
    )

    delegate(dragSupport.connectedMouseEventHandler())

    delegate(
      object : CanvasMouseEventHandler {
        override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
          //A move without coordinates is the pointer leaving the canvas - above no handle, like a move away from one
          val handleDirection = event.coordinates?.let { findHandleDirection(it) }
          uiState = uiState.hoveringAboveHandle(handleDirection)

          //An armed handle belongs to this layer alone - the layers below must not react to the move that armed it
          return if (handleDirection != null) EventConsumption.Consumed else EventConsumption.Ignored
        }
      }
    )
  }

  /**
   * Returns the mouse cursor for the given handle direction
   */
  private fun Direction.getResizeCursor(): MouseCursor {
    return when (this) {
      Direction.CenterLeft -> MouseCursor.ResizeWest
      Direction.CenterRight -> MouseCursor.ResizeEast

      Direction.TopLeft -> MouseCursor.ResizeNorthWest
      Direction.TopCenter -> MouseCursor.ResizeNorth
      Direction.TopRight -> MouseCursor.ResizeNorthEast

      Direction.BottomLeft -> MouseCursor.ResizeSouthWest
      Direction.BottomCenter -> MouseCursor.ResizeSouth
      Direction.BottomRight -> MouseCursor.ResizeSouthEast
      else -> throw IllegalArgumentException("Unsupported direction <${this}>")
    }
  }

  override fun initialize(paintingContext: LayerPaintingContext) {
    val chartSupport = paintingContext.chartSupport

    //Update the mouse cursor depending on the ui state - tied to the chart-support lifecycle
    uiStateProperty.consumeImmediately {
      chartSupport.markAsDirty(DirtyReason.UiStateChanged)

      chartSupport.cursor = when (it) {
        is DefaultState -> null
        is HoveringOverHandle -> it.handleDirection.getResizeCursor()
        is DraggingHandle -> it.handleDirection.getResizeCursor()
      }
    }.disposeOn(chartSupport)


    //Notify the handlers about the changes
    uiStateProperty.consumeChanges { oldValue, newValue ->
      when (oldValue) {
        is DraggingHandle -> chartSupport.resizeHandlesSupport.notifyResizingFinished()
        is HoveringOverHandle -> chartSupport.resizeHandlesSupport.notifyDisarmed()
        DefaultState -> {
        } //Ignore
      }

      when (newValue) {
        DefaultState -> {
        }

        is HoveringOverHandle -> {
          chartSupport.resizeHandlesSupport.notifyArmed(newValue.handleDirection)
        }

        is DraggingHandle -> {
          chartSupport.resizeHandlesSupport.notifyBeginResize(newValue.handleDirection)
        }
      }
    }.disposeOn(chartSupport)
  }

  /**
   * Calculates the locations of the handles.
   */
  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)

    val contentBounds = paintingContext.chartSupport.resizeHandlesSupport.resizableContentBounds(paintingContext.loopIndex)
    handlesVisible = contentBounds != null

    if (contentBounds == null) {
      //No handles left to gesture on or hover above - the element was deleted or deselected
      uiState = DefaultState
      return
    }

    handleBounds.resize(Direction.cornersAndSides.size)

    val handleDiameter = configuration.handleDiameter

    //
    // Attention! The order matters! Must be the same order as in [toIndex()] below
    //

    //top left
    handleBounds.centered(0, contentBounds.left, contentBounds.top, handleDiameter, handleDiameter)
    //top right
    handleBounds.centered(1, contentBounds.right, contentBounds.top, handleDiameter, handleDiameter)
    //bottom left
    handleBounds.centered(2, contentBounds.left, contentBounds.bottom, handleDiameter, handleDiameter)
    //bottom right
    handleBounds.centered(3, contentBounds.right, contentBounds.bottom, handleDiameter, handleDiameter)

    //top center
    handleBounds.centered(4, contentBounds.centerX, contentBounds.top, handleDiameter, handleDiameter)
    //bottom center
    handleBounds.centered(5, contentBounds.centerX, contentBounds.bottom, handleDiameter, handleDiameter)
    //left center
    handleBounds.centered(6, contentBounds.left, contentBounds.centerY, handleDiameter, handleDiameter)
    //right center
    handleBounds.centered(7, contentBounds.right, contentBounds.centerY, handleDiameter, handleDiameter)
  }

  /**
   * Paints the handles
   */
  val resizeHandlesPaintable: ResizeHandlesPaintable = ResizeHandlesPaintable(object : HandleBoundsProvider {
    override fun minX(direction: Direction): Double {
      return handleBounds.x(direction.toIndex())
    }

    override fun minY(direction: Direction): Double {
      return handleBounds.y(direction.toIndex())
    }

    override fun width(direction: Direction): Double {
      return handleBounds.width(direction.toIndex())
    }

    override fun height(direction: Direction): Double {
      return handleBounds.height(direction.toIndex())
    }
  })

  override fun paint(paintingContext: LayerPaintingContext) {
    if (handlesVisible.not()) {
      return
    }

    resizeHandlesPaintable.paint(paintingContext)
  }

  @ConfigurationDsl
  class Configuration {
    /**
     * The size (diameter) of one handle
     */
    var handleDiameter: @px Double = 12.0
  }

  /**
   * Returns the index for the given direction - only valid within this class!
   * Do *not* assume the order is the same anywhere outside this class
   */
  private fun Direction.toIndex(): Int {
    return when (this) {
      Direction.TopLeft -> 0
      Direction.TopRight -> 1

      Direction.BottomLeft -> 2
      Direction.BottomRight -> 3

      Direction.TopCenter -> 4
      Direction.BottomCenter -> 5

      Direction.CenterLeft -> 6
      Direction.CenterRight -> 7
      else -> throw IllegalArgumentException("center is not supported")
    }
  }

  private fun Int.toDirection(): Direction {
    return when (this) {
      0 -> Direction.TopLeft
      1 -> Direction.TopRight
      2 -> Direction.BottomLeft
      3 -> Direction.BottomRight
      4 -> Direction.TopCenter
      5 -> Direction.BottomCenter
      6 -> Direction.CenterLeft
      7 -> Direction.CenterRight
      else -> throw IllegalArgumentException("index $this is not supported")
    }
  }

  /**
   * Returns the handle direction at the given coordinates - null when no handle is there, or none is shown at all
   */
  private fun findHandleDirection(location: Coordinates): Direction? {
    if (handlesVisible.not()) return null

    val foundIndex = handleBounds.findIndex(location) ?: return null
    return foundIndex.toDirection()
  }
}

/**
 * Provides the handle bounds.
 *
 * Does *not* support [Direction.Center] - only the values from [Direction.cornersAndSides] are allowed.
 */
interface HandleBoundsProvider {
  /**
   * Returns the left value
   */
  fun minX(direction: Direction): @Zoomed Double

  /**
   * Returns the top value
   */
  fun minY(direction: Direction): @Zoomed Double

  fun width(direction: Direction): @Zoomed Double
  fun height(direction: Direction): @Zoomed Double

  fun maxX(direction: Direction): @Zoomed Double {
    return minX(direction) + width(direction)
  }

  fun maxY(direction: Direction): @Zoomed Double {
    return minY(direction) + height(direction)
  }
}


/**
 * Adds a layer that supports the resize handles
 */
fun Layers.addResizeByHandlesLayer(): ResizeByHandlesLayer {
  return ResizeByHandlesLayer().also {
    addLayer(it)
  }
}
