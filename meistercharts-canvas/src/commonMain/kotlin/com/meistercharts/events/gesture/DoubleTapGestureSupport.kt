package com.meistercharts.events.gesture

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.model.Coordinates
import it.neckar.open.kotlin.lang.consumeUntil
import it.neckar.open.time.nowMillis
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.TouchCancelEvent
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent
import com.meistercharts.events.gesture.SingleTapGestureSupport.Companion.DefaultMaxTapDuration
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 * Detects double tab gestures.
 *
 * Detects a touch start event.
 * On a start end, the duration of the tap is compared to the given [maxPressDuration].
 *
 * Every touch move event, cancels the gesture.
 *
 * This support never consumes start and move events. Only end events of the *second* tap are consumed.
 */
class DoubleTapGestureSupport(
  /**
   * The max time between start and end that is allowed for a tap
   */
  val maxPressDuration: @ms Double = DefaultMaxTapDuration,

  /**
   * The max time between the end of the first tap and start of the second tap
   */
  val maxDelayDuration: @ms Double = 400.0,

  /**
   * The maximum allowed distance between start of first tap and start of second tap.
   */
  val maxDistanceBetweenTaps: @px Double = 20.0,

  ) : CanvasTouchEventHandler {

  /**
   * The start coordinates of the *first* tap.
   * These coordinates are used as location for the gesture
   */
  private var start0Coordinates: Coordinates? = null

  /**
   * The time of the first start event.
   * Used to calculate the duration of the first tap
   */
  private var start0EventTime: @ms Double? = null

  /**
   * The time of the first end event
   * Used to calculate the delay between two two taps
   */
  private var end0EventTime: @ms Double? = null

  /**
   * The time of the second start event.
   * Used to calculate the duration of the second tap
   */
  private var start1EventTime: @ms Double? = null


  /**
   * Reset the gesture
   */
  private fun reset(): EventConsumption {
    start0Coordinates = null

    start0EventTime = null
    end0EventTime = null
    start1EventTime = null

    return EventConsumption.Ignored
  }

  /**
   * Contains true if the first tap has been detected
   */
  private val state: State
    get() {
      return if (end0EventTime == null) {
        State.LookingForFirstTap
      } else {
        State.LookingForSecondTap
      }
    }

  override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
    event.ifSingleTouch {
      return when (state) {
        State.LookingForFirstTap -> {
          //Prepare for first tap
          start0Coordinates = event.firstChanged.coordinates
          start0EventTime = nowMillis()
          EventConsumption.Ignored //only consume the end of the second tap
        }
        State.LookingForSecondTap -> {
          @ms val now = nowMillis()

          //Check delay
          @ms val currentEnd0EventTime = end0EventTime ?: return reset()

          //The delay between end of first tab and start
          @ms val delay = now - currentEnd0EventTime
          if (delay > maxDelayDuration) {
            //Restart the gesture, save as *first* tap
            reset()
            start0Coordinates = event.firstChanged.coordinates
            start0EventTime = nowMillis()

            return EventConsumption.Ignored
          }

          //check distance
          val start1Coordinates = event.firstChanged.coordinates
          val currentStart0Coordinates = start0Coordinates ?: return reset()

          val distance = start1Coordinates.distanceTo(currentStart0Coordinates)
          if (distance > maxDistanceBetweenTaps) {
            return reset()
          }

          start1EventTime = now
          EventConsumption.Ignored //only consume the end of the second tap
        }
      }
    }

    //Any other event
    reset()
    return EventConsumption.Ignored
  }

  override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
    //Always reset on move
    reset()
    return EventConsumption.Ignored
  }

  override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
    event.ifNoTouch {
      //Last finger lifted
      return when (state) {
        State.LookingForFirstTap -> {
          val now = nowMillis()

          //Possibly detecting the first tap
          @ms val currentStartTime = start0EventTime ?: return reset()

          //Calculate the press time
          @ms val pressDuration = now - currentStartTime

          //Check for the duration of the "press"
          if (pressDuration > maxPressDuration) {
            //too long, no tap
            return reset()
          }

          //First tap detected
          end0EventTime = now

          EventConsumption.Ignored //only the second tap is consumed
        }
        State.LookingForSecondTap -> {
          val now = nowMillis()

          //Possibly detecting the second tap
          @ms val currentStartTime = start1EventTime ?: return reset()

          //Calculate the press time
          @ms val pressDuration = now - currentStartTime

          //Check for the duration of the "press"
          if (pressDuration > maxPressDuration) {
            //too long, no tap
            return reset()
          }

          //Calculate the coordinates and notify the actions
          val coordinates = start0Coordinates ?: return reset()

          //prepare for the next
          reset()

          //Notify all actions, until the first action returns Consumed
          return tapActions.consumeUntil(EventConsumption.Consumed) {
            it(coordinates)
          } ?: EventConsumption.Ignored
        }
      }
    }

    reset()
    return EventConsumption.Ignored
  }

  override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
    reset()
    return EventConsumption.Ignored
  }

  /**
   * The actions that are notified on tap
   */
  private val tapActions = mutableListOf<DoubleTapAction>()

  /**
   * Registers an action that is notified when a tab has been detected.
   * As soon as a tab action returns [EventConsumption.Consumed], no other tab actions are notified
   */
  fun onDoubleTap(action: DoubleTapAction) {
    tapActions.add(action)
  }

  /**
   * The current state of the gesture support
   */
  enum class State {
    /**
     * Looking for the first tap
     */
    LookingForFirstTap,

    /**
     * Looking for the second tap (the first tap has been recognized)
     */
    LookingForSecondTap
  }
}

/**
 * A tap action.
 */
typealias DoubleTapAction = (location: Coordinates) -> EventConsumption
