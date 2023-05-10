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
package com.meistercharts.canvas.events

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.KeyDownEvent
import com.meistercharts.events.KeyTypeEvent
import com.meistercharts.events.KeyUpEvent

/**
 * Event handler for key events from a canvas
 */
interface CanvasKeyEventHandler {
  /**
   * Is notified when a key has been pressed
   */
  fun onDown(event: KeyDownEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a key has been released
   */
  fun onUp(event: KeyUpEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }

  /**
   * Is notified when a key has been typed
   */
  fun onType(event: KeyTypeEvent, chartSupport: ChartSupport): EventConsumption {
    return EventConsumption.Ignored
  }
}
