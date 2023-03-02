package com.meistercharts.canvas.resize

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.MouseCursor
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasMouseEventHandlerBroker
import com.meistercharts.canvas.layout.cache.BoundsLayoutCache
import com.meistercharts.canvas.paintable.ResizeHandlesPaintable
import com.meistercharts.canvas.resizeHandlesSupport
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.gesture.CanvasDragSupport
import com.meistercharts.events.gesture.connectedMouseEventHandler
import it.neckar.open.observable.ObservableObject
import it.neckar.open.unit.other.px

/**
 * This class supports resizing of objects on the canvas using handles
 */
class ResizeByHandlesLayer : AbstractLayer() {

  override val type: LayerType = LayerType.Content

  val style: Style = Style()

  /**
   * Contains the current ui state
   */
  private var uiStateProperty: ObservableObject<ResizeByHandlesLayerState> = ObservableObject(DefaultState)
  var uiState: ResizeByHandlesLayerState by uiStateProperty
    private set

  /**
   * Contains the locations for each handle direction
   *
   * Attention! The order matters! It is well known - within this class.
   * Do not depend on the order outside of this class (this might not be the same order as in the enum [Direction])
   */
  private val handleBounds = BoundsLayoutCache()
  private var handlesVisible = false

  val dragSupport: CanvasDragSupport = CanvasDragSupport().also {
    it.handle(object : CanvasDragSupport.Handler {
      override fun isDraggingAllowedFromHere(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): Boolean {
        if (!handlesVisible) return false

        val handleIndex = handleBounds.findIndex(location) ?: return false
        uiState = uiState.startDragging(handleIndex.toDirection())
        return true
      }

      override fun onDrag(source: CanvasDragSupport, location: Coordinates, distance: Distance, deltaTime: Double, chartSupport: ChartSupport): EventConsumption {
        require(handlesVisible) { "handlesVisible is false" }

        chartSupport.resizeHandlesSupport.notifyResize((uiState as DraggingHandle).handleDirection, distance)
        return EventConsumption.Consumed
      }

      override fun onFinish(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): EventConsumption {
        require(handlesVisible) { "handlesVisible is false" }

        val hoverHandleDirection = handleBounds.findIndex(location)?.toDirection()
        uiState = uiState.finishedDragging(hoverHandleDirection)
        return EventConsumption.Consumed
      }
    })
  }

  /**
   * A mouse event handler that can be registered at the layer
   */
  override val mouseEventHandler: CanvasMouseEventHandler = CanvasMouseEventHandlerBroker().apply {
    /**
     * Update the mouse cursor first
     */
    delegate(
      object : CanvasMouseEventHandler {
        override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
          val coordinates = event.coordinates ?: return super.onMove(event, chartSupport)

          //Update the hovering state
          val handleDirection = getHandleDirection(coordinates)
          uiState = uiState.hoveringAboveHandle(handleDirection)

          //Consume the event, if over a handle
          return if (handleDirection != null) EventConsumption.Consumed else EventConsumption.Ignored
        }
      }
    )

    //Delegate the drag support
    dragSupport.connectedMouseEventHandler()?.let {
      delegate(it)
    }
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

    //Update the mouse cursor depending on the ui state
    uiStateProperty.consumeImmediately {
      chartSupport.markAsDirty()

      chartSupport.cursor = when (it) {
        is DefaultState -> null
        is HoveringOverHandle -> it.handleDirection.getResizeCursor()
        is DraggingHandle -> it.handleDirection.getResizeCursor()
      }
    }


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
    }
  }

  /**
   * Calculates the locations of the handles.
   */
  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)

    val resizeHandlesSupport = paintingContext.chartSupport.resizeHandlesSupport
    val contentBounds = resizeHandlesSupport.resizableContentBounds
    handlesVisible = contentBounds != null

    if (!handlesVisible || contentBounds == null) {
      return
    }

    handleBounds.ensureSize(Direction.cornersAndSides.size)

    val handleDiameter = style.handleDiameter

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

  /**
   * Paints the handles
   */
  override fun paint(paintingContext: LayerPaintingContext) {
    val resizeHandlesSupport = paintingContext.chartSupport.resizeHandlesSupport
    if (resizeHandlesSupport.resizableContentBounds == null) {
      //No bounds found - do not paint
      return
    }

    resizeHandlesPaintable.paint(paintingContext)
  }

  class Style {
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
   * Returns the handle direction at the given coordinates
   */
  fun getHandleDirection(location: Coordinates): Direction? {
    if (!handlesVisible) return null

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
    return minY(direction) + width(direction)
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
