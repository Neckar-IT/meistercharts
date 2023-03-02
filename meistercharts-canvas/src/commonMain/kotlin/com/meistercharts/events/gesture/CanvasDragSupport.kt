package com.meistercharts.events.gesture

import it.neckar.open.unit.number.Positive
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Distance
import it.neckar.open.kotlin.lang.consumeUntil
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.EventConsumption.Consumed
import com.meistercharts.events.EventConsumption.Ignored
import com.meistercharts.events.ModifierCombination
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.events.TouchCancelEvent
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchId
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import kotlin.reflect.KProperty0

/**
 * Support dragging gestures in context of the canvas.
 *
 * Attention!
 * It is necessary to connect this to the mouse/touch events.
 * In a layer this can be done like this:
 *
 * ```
 * override val mouseEventHandler: CanvasMouseEventHandler = canvasDragSupport.connectedMouseEventHandler()
 * override val touchEventHandler: CanvasTouchEventHandler = canvasDragSupport.connectedTouchEventHandler()
 * ```
 *
 * Both of these handlers are then active. So both event sources are supported at the same time
 */
class CanvasDragSupport {
  /**
   * Is set to true if the mouse is down - and everything is prepared for dragging
   */
  var preparedForDragging: Boolean = false
    private set

  /**
   * Set to true if currently dragging
   */
  var dragging: Boolean = false
    private set

  /**
   * The current location of the mouse pointer.
   * Is updated on every drag
   */
  @px
  @Window
  var currentLocation: Coordinates? = null
    private set

  /**
   * When [currentLocation] has been updated
   */
  @ms
  var currentLocationUpdateTime: Double = 0.0
    private set

  /**
   * Used to calculate the pointer speeds while dragging
   */
  val dragSpeedCalculator: MouseSpeedCalculator = MouseSpeedCalculator()

  /**
   * This method is called on down event
   */
  fun prepareForDragging(coordinates: Coordinates, eventTime: @ms Double, chartSupport: ChartSupport): EventConsumption {
    //Reset everything. This may be necessary if the mouse up event happened outside the canvas
    reset()

    //Remember the start time
    updateDragLocation(coordinates, eventTime)

    //Call all handlers
    val draggingAllowed = handlers.consumeUntil {
      it.isDraggingAllowedFromHere(this, coordinates, chartSupport)
    }

    return if (draggingAllowed) {
      preparedForDragging = true
      Consumed
    } else {
      reset()
      Ignored
    }
  }

  /**
   * Is called when a drag event has been recognized
   * Returns true if the event should be consumed
   */
  fun dragging(coordinates: Coordinates, eventTime: Double, chartSupport: ChartSupport): EventConsumption {
    if (!preparedForDragging) {
      //No mouse down detected
      return Ignored
    }

    if (!dragging) {
      //Start dragging
      dragging = true
      //updateDragLocation(coordinates, eventTime)
      //
      //return handlers.consumeUntil(Consumed) {
      //  it.onFirstDrag(this, coordinates, chartSupport)
      //} ?: Ignored
    }

    //Notify about drag
    currentLocation?.let {
      @px val distance = coordinates.delta(it)
      @ms val deltaTime: Double = updateDragLocation(coordinates, eventTime)

      dragSpeedCalculator.add(deltaTime, distance)

      return handlers.consumeUntil(Consumed) { handler ->
        handler.onDrag(this, coordinates, distance, deltaTime, chartSupport)
      } ?: Ignored
    } ?: throw IllegalStateException("No current mouse location available")
  }

  fun finishDragging(coordinates: Coordinates, chartSupport: ChartSupport): EventConsumption {
    if (!dragging && !preparedForDragging) {
      //no dragging - ignore event
      return Ignored
    }

    reset()

    //Notify the handlers
    return handlers.consumeUntil(Consumed) {
      it.onFinish(this, coordinates, chartSupport)
    } ?: Ignored
  }

  /**
   * Resets all values to.
   */
  fun reset() {
    preparedForDragging = false
    dragging = false
    currentLocation = null
    currentLocationUpdateTime = 0.0
  }

  /**
   * Updates the drag location.
   *
   * Returns the delta time
   */
  private fun updateDragLocation(coordinates: Coordinates?, eventTime: @ms Double): @ms Double {
    currentLocation = coordinates

    @ms val delta = eventTime - currentLocationUpdateTime
    currentLocationUpdateTime = eventTime
    return delta
  }

  /**
   * The handlers
   */
  private val handlers: MutableList<Handler> = mutableListOf()

  /**
   * Registers a drag handler that is notified about drag events
   */
  fun handle(handler: Handler) {
    handlers.add(handler)
  }

  /**
   * Callback that is registered at the drag support
   */
  interface Handler {
    /**
     * Is called on mouse down.
     * Implementations should return true if dragging may be started at the given location
     *
     * @return true if dragging can be started from the given location, false otherwise
     */
    fun isDraggingAllowedFromHere(source: CanvasDragSupport, location: @Window Coordinates, chartSupport: ChartSupport): Boolean

    /**
     * Is called if a drag has been detected
     * @return true if the event should be consumed
     */
    fun onDrag(source: CanvasDragSupport, @Window location: Coordinates, @Zoomed distance: Distance, @ms deltaTime: Double, chartSupport: ChartSupport): EventConsumption

    /**
     * Is called if a drag has been finished
     * @return true if the event should be consumed
     */
    fun onFinish(source: CanvasDragSupport, @Window location: Coordinates, chartSupport: ChartSupport): EventConsumption {
      //Usually the event is consumed on finished
      return Consumed
    }
  }
}

/**
 * Creates a mouse event handler that can be used to update the drag handler
 */
fun CanvasDragSupport.connectedMouseEventHandler(modifierCombination: ModifierCombination = ModifierCombination.None): CanvasMouseEventHandler {
  return object : CanvasMouseEventHandler {
    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      if (event.modifierCombination != modifierCombination) {
        return Ignored
      }

      return prepareForDragging(event.coordinates, event.timestamp, chartSupport)
    }

    override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
      return dragging(event.coordinates, event.timestamp, chartSupport)
    }

    override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
      //Ignore the modifier combination on up
      return finishDragging(event.coordinates, chartSupport)
    }
  }
}

/**
 * Creates a touch event handler that can be registered to update the drag handler
 */
fun CanvasDragSupport.connectedTouchEventHandler(
  /**
   * The number of touches required to recognize a drag
   */
  numberOfTouches: @Positive Int
): CanvasTouchEventHandler {
  require(numberOfTouches > 0) {
    "Need at least one touch but got $numberOfTouches"
  }

  return object : CanvasTouchEventHandler {
    /**
     * The id of the first touch that belongs to the drag-gesture.
     * The touch is set when a touch start event is detected.
     *
     * The corresponding touch is used to calculate the delta
     */
    private var firstTouchId: TouchId? = null

    fun reset() {
      this@connectedTouchEventHandler.reset()
      firstTouchId = null
    }

    override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
      if (event.targetTouchesCount != numberOfTouches) {
        reset()
        return Ignored
      }

      //Remember the *first* touch id
      val touch = event.touches.first()
      firstTouchId = touch.touchId

      return prepareForDragging(touch.coordinates, event.timestamp, chartSupport)
    }

    override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
      val touch = event.touches.firstOrNull { it.touchId == firstTouchId }

      if (touch == null) {
        //Our touch has not been found, just ignore this event
        return Ignored
      }

      return dragging(touch.coordinates, event.timestamp, chartSupport)
    }

    override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
      reset()
      return finishDragging(event.firstChanged.coordinates, chartSupport)
    }

    override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
      reset()
      return Ignored
    }
  }
}


/**
 * Returns a delegate that uses the current value of this property to delegate all calls.
 */
fun KProperty0<CanvasDragSupport.Handler?>.delegate(): CanvasDragSupport.Handler {
  return object : CanvasDragSupport.Handler {
    override fun isDraggingAllowedFromHere(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): Boolean {
      return get()?.isDraggingAllowedFromHere(source, location, chartSupport) ?: false
    }

    override fun onDrag(source: CanvasDragSupport, location: Coordinates, distance: Distance, deltaTime: Double, chartSupport: ChartSupport): EventConsumption {
      return get()?.onDrag(source, location, distance, deltaTime, chartSupport) ?: Ignored
    }

    override fun onFinish(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): EventConsumption {
      return get()?.onFinish(source, location, chartSupport) ?: Ignored
    }
  }
}
