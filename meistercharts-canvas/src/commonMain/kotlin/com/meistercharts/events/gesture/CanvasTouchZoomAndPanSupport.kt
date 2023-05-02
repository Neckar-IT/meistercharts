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
package com.meistercharts.events.gesture

import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.number.PositiveOrZero
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.canvas.events.CanvasTouchEventHandlerBroker
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Distance
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.abs
import it.neckar.open.kotlin.lang.consumeUntil
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.Touch
import com.meistercharts.events.TouchCancelEvent
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchId
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import it.neckar.logging.LoggerFactory

/**
 * This class supports zooming and panning on touch devices.
 *
 * This class does *NOT* support mouse events at all
 *
 * Support dragging gestures in context of the canvas.
 *
 * Attention!
 * It is necessary to connect this to the mouse/touch events.
 * In a layer this can be done like this:
 *
 * ```
 * override val touchEventHandler: CanvasTouchEventHandler = canvasTouchZoomAndPanSupport.connectedTouchEventHandler()
 * ```

 */
class CanvasTouchZoomAndPanSupport {

  /**
   * The minimum distance between two touches - on each axis
   * If the touches are closer to each other, no zoom change will be calculated
   */
  var minDistanceBetweenTouches: @px Double = 25.0

  /**
   * The handlers that are notified about zoom and pan gestures
   */
  val handlers: MutableList<Handler> = mutableListOf()

  fun addHandler(handler: Handler) {
    this.handlers.add(handler)
  }

  fun removeHandler(handler: Handler) {
    this.handlers.add(handler)
  }

  /**
   * Detects double taps
   */
  private val doubleTapGestureSupport = DoubleTapGestureSupport().also {
    it.onDoubleTap { coordinates ->
      handlers.consumeUntil(EventConsumption.Consumed) { handler ->
        handler.doubleTap(coordinates)
      } ?: EventConsumption.Ignored
    }
  }

  /**
   * Contains the coordinates for a touch id
   */
  val touchCoordinates: MutableMap<TouchId, @Window Coordinates> = mutableMapOf()

  /**
   * The center of the two touches
   */
  var center: @Window Coordinates? = null

  /**
   * Resets the current detection
   */
  private fun reset(): EventConsumption {
    touchCoordinates.clear()
    center = null
    return EventConsumption.Ignored
  }

  private val isSingleTouchGestureActive: Boolean
    get() {
      return touchCoordinates.size == 1
    }

  /**
   * Returns true if currently a two touch gesture is active, false otherwise
   */
  private val isTwoTouchGestureActive: Boolean
    get() {
      return touchCoordinates.size == 2
    }

  /**
   * Store the coordinates for *two* finger gestures.
   * Returns the (new) center
   */
  private fun storeCoordinates(touch0: Touch, touch1: Touch): Coordinates {
    val coordinates0 = touch0.coordinates
    val coordinates1 = touch1.coordinates

    touchCoordinates[touch0.touchId] = coordinates0
    touchCoordinates[touch1.touchId] = coordinates1

    return coordinates0.center(coordinates1).also { newCenter ->
      center = newCenter
    }
  }

  /**
   * Stores the coordinates for a *one* finger gesture
   */
  private fun storeCoordinates(touch: Touch) {
    touchCoordinates[touch.touchId] = touch.coordinates
  }

  /**
   * Returns the coordinates for a given touch id.
   * Returns null if no coordinates could be found.
   */
  private fun coordinates(touchId: TouchId): Coordinates? {
    val coordinates = touchCoordinates[touchId]

    if (coordinates == null) {
      logger.warn("WARNING: No touch coordinates found for $touchId")
      logger.info("\t available: ${touchCoordinates.size} coords")
      for (entry in touchCoordinates) {
        logger.info("\t\t${entry.key} - ${entry.value}")
      }
    }

    return coordinates
  }

  /**
   * Returns a touch event handler that can be used to connect this with the canvas
   */
  fun connectedTouchEventHandler(): CanvasTouchEventHandler {
    return CanvasTouchEventHandlerBroker().also {
      //Reset on double tap
      it.delegate(doubleTapGestureSupport)

      //Zoom + Pan
      it.delegate(
        object : CanvasTouchEventHandler {
          override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
            //Reset in all cases!
            reset()

            //Check for pinch and pan
            event.ifTwoTouches {
              //Begin the gesture
              //Save the current touches
              storeCoordinates(event.targetTouches[0], event.targetTouches[1])
              return EventConsumption.Consumed
            }

            //No matching touch count found, just ignore
            return EventConsumption.Ignored
          }

          @Suppress("DuplicatedCode")
          override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
            event.ifTwoTouches {
              val newTouch0 = event.targetTouches[0]
              val newTouch1 = event.targetTouches[1]

              //
              // Remember the old locations / coordinates / distances
              //
              // ATTENTION! Order matters! Remember the old values before `storeCoordinates()` is called
              //
              val oldCenter = center
              requireNotNull(oldCenter) { "the center should have been set in onStart" }

              val oldCoordinates0: Coordinates = coordinates(newTouch0.touchId) ?: return reset()
              val oldCoordinates1: Coordinates = coordinates(newTouch1.touchId) ?: return reset()

              val oldDistanceBetweenTouches = oldCoordinates0 - oldCoordinates1


              //the center of the new touches
              val newCenter = storeCoordinates(newTouch0, newTouch1) //also updates the center
              val newDistanceBetweenTouches = newTouch0.coordinates - newTouch1.coordinates


              //The delta between new and old center
              val deltaCenterBetweenNewAndOld = newCenter - oldCenter

              //Notify about translation
              if (!deltaCenterBetweenNewAndOld.isZero()) {
                handlers.fastForEach { handler ->
                  handler.translate(oldCenter, newCenter, deltaCenterBetweenNewAndOld)
                }
              }

              //Zoom factor change on X axis
              val zoomFactorChangeX: Double = calculateZoomFactorChange(oldDistanceBetweenTouches.x, newDistanceBetweenTouches.x)

              //Zoom factor change on Y axis
              val zoomFactorChangeY: Double = calculateZoomFactorChange(oldDistanceBetweenTouches.y, newDistanceBetweenTouches.y)

              handlers.fastForEach { handler ->
                handler.zoomChange(oldCenter, newCenter, oldDistanceBetweenTouches, newDistanceBetweenTouches, zoomFactorChangeX, zoomFactorChangeY)
              }

              return EventConsumption.Consumed
            }

            //Not a known number of touches, just reset everything
            return reset()
          }

          override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
            if (isTwoTouchGestureActive) {
              //We have been active, just reset and consume the event
              reset()
              return EventConsumption.Consumed
            }

            //Releasing the third finger, also results in the start of a two finger gesture
            event.ifTwoTouches {
              //Save the current touches
              storeCoordinates(event.targetTouches[0], event.targetTouches[1])
              return EventConsumption.Consumed
            }

            return reset()
          }

          override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
            return reset()
          }
        }
      )
    }
  }

  /**
   * Calculates the zoom factor change
   *
   * The zoom factor changes are ignored for very small distances between touches.
   * The relevant distance for each axis is stored in [minDistanceBetweenTouches]
   *
   * Changes near the dead zone must be symmetric:
   * When the distance is shrinking, the zoom factor has be modified exactly until the dead zone has been reacted
   * When the distance is increasing, the zoom factor has to be modified exactly from the dead zone
   * But: The touch events usually don't align exactly with the bounds of the dead zones
   * Therefore the distance has to modified manually to exactly match the bounds
   *
   */
  private fun calculateZoomFactorChange(
    oldDistance: @MayBeNegative @MayBeNaN Double,
    newDistance: @MayBeNegative @MayBeNaN Double
  ): Double {
    if (oldDistance.isNaN() || newDistance.isNaN()) {
      return 1.0
    }

    @PositiveOrZero val oldDistanceAbs = oldDistance.abs()
    @PositiveOrZero val newDistanceAbs = newDistance.abs()

    val oldDistanceLargerThanDeadZone = oldDistanceAbs >= minDistanceBetweenTouches
    val newDistanceLargerThanDeadZone = newDistanceAbs >= minDistanceBetweenTouches

    //only modify the zoom factor if at least one of the distances is larger than the dead zone
    return if (oldDistanceLargerThanDeadZone || newDistanceLargerThanDeadZone) {
      val oldDelta = oldDistanceAbs.coerceAtLeast(minDistanceBetweenTouches)
      val newDelta = newDistanceAbs.coerceAtLeast(minDistanceBetweenTouches)

      //Calculate the zoom factor change
      (1 / oldDelta * newDelta)
    } else {
      //Both distances (old and new) between touches are within the dead zone, ignore
      1.0
    }
  }

  /**
   * Is notified about changes (zoom and pan)
   */
  interface Handler {
    /**
     * Translation happened!
     */
    fun translate(oldCenter: @Window Coordinates, newCenter: @Window Coordinates, deltaCenter: @Zoomed Distance): EventConsumption

    /**
     * Zoom gesture detected.
     */
    fun zoomChange(
      oldCenter: @Window Coordinates,
      newCenter: @Window Coordinates,
      /**
       * The old distance between the last touches
       */
      oldDistanceBetweenTouches: @Zoomed Distance,
      /**
       * The new distance between the current touches
       */
      newDistanceBetweenTouches: @Zoomed Distance,
      /**
       * The zoom factor change.
       *
       * Examples:
       * * 1.0: Nothing has changed
       * * 0.5: Zoomed out
       */
      zoomFactorChangeX: @pct Double,
      /**
       * The zoom factor change on the y axis
       *
       * Examples:
       * * 1.0: Nothing has changed
       * * 0.5: Zoomed out
       */
      zoomFactorChangeY: @pct Double,
    ): EventConsumption

    /**
     * A double tap has been detected at the given location
     */
    fun doubleTap(tapLocation: @Window Coordinates): EventConsumption
  }


  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.events.gesture.CanvasTouchZoomAndPanSupport")
  }
}
