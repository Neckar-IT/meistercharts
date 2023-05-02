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
package com.meistercharts.events

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Describes whether an event has been consumed.
 * This enum can be used as return type for methods that possible consume events
 */
enum class EventConsumption {
  /**
   * The event has not been consumed but ignored by the handler/listener/...
   */
  Ignored,

  /**
   * The event has been consumed
   */
  Consumed;

  /**
   * Returns true if the event has been consumed
   */
  val consumed: Boolean
    get() = this == Consumed

  companion object {
    /**
     * Returns [Consumed] if the given boolean is true, [Ignored] otherwise
     */
    fun consumeIf(consumed: Boolean): EventConsumption {
      return if (consumed) Consumed else Ignored
    }

    inline fun consumeIf(consumed: () -> Boolean): EventConsumption {
      contract {
        callsInPlace(consumed, InvocationKind.EXACTLY_ONCE)
      }
      return consumeIf(consumed())
    }
  }
}
