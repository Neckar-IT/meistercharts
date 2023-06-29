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

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.events.CanvasTouchEventHandler
import com.meistercharts.geometry.Coordinates
import it.neckar.open.kotlin.lang.consumeUntil
import it.neckar.open.time.nowMillis
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.TouchCancelEvent
import com.meistercharts.events.TouchEndEvent
import com.meistercharts.events.TouchMoveEvent
import com.meistercharts.events.TouchStartEvent
import it.neckar.open.unit.si.ms

/**
 * Detects single tab gestures.
 *
 * Detects a touch start event.
 * On a start end, the duration of the tap is compared to the given [maxTapDuration].
 *
 * Every touch move event, cancels the gesture.
 */
class SingleTapGestureSupport(
  /**
   * The max time between start and end that is allowed for a tap
   */
  val maxTapDuration: @ms Double = DefaultMaxTapDuration
) : CanvasTouchEventHandler {
  /**
   * The start coordinates.
   */
  private var startCoordinates: Coordinates? = null

  /**
   * The time of the start event.
   * Used to calculate the duration of the tap
   */
  private var startEventTime: @ms Double? = null

  /**
   * Reset the gesture
   */
  private fun reset(): EventConsumption {
    startCoordinates = null
    startEventTime = null

    return EventConsumption.Ignored
  }

  override fun onStart(event: TouchStartEvent, chartSupport: ChartSupport): EventConsumption {
    event.ifSingleTouch {
      startCoordinates = event.firstChanged.coordinates
      startEventTime = nowMillis()

      //do *not* consume
      return EventConsumption.Ignored
    }

    //Any other event
    return reset()
  }

  override fun onMove(event: TouchMoveEvent, chartSupport: ChartSupport): EventConsumption {
    //Always reset on move
    return reset()
  }

  override fun onEnd(event: TouchEndEvent, chartSupport: ChartSupport): EventConsumption {
    event.ifNoTouch {
      //Last finger lifted
      val currentStart = startEventTime ?: return reset()

      //Calculate the press time
      val pressDuration = nowMillis() - currentStart

      //Check for the duration of the "press"
      if (pressDuration > maxTapDuration) {
        //too long, no tap
        return reset()
      }

      //Calculate the coordinates and notify the actions
      val coordinates = event.firstChanged.coordinates

      //Notify all actions, until the first action returns Consumed
      return tapActions.consumeUntil(EventConsumption.Consumed) {
        it(coordinates)
      } ?: EventConsumption.Ignored
    }

    return reset()
  }

  override fun onCancel(event: TouchCancelEvent, chartSupport: ChartSupport): EventConsumption {
    reset()
    return EventConsumption.Ignored
  }

  /**
   * The actions that are notified on tap
   */
  private val tapActions = mutableListOf<TapAction>()

  /**
   * Registers an action that is notified when a tab has been detected.
   * As soon as a tab action returns [EventConsumption.Consumed], no other tab actions are notified
   */
  fun onTap(action: TapAction) {
    tapActions.add(action)
  }

  companion object {
    /**
     * The default value for the max tap duration.
     * Use used for the single and double tabs
     */
    const val DefaultMaxTapDuration: @ms Double = 250.0
  }
}

/**
 * A tap action.
 */
typealias TapAction = (location: Coordinates) -> EventConsumption
