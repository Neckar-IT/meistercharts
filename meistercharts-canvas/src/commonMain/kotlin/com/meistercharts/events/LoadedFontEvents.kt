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

import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable

/**
 * A non-cancellable Event indicating that a font-face has been loaded successfully.
 */
class FontLoadedEvent

/**
 * Handles [FontLoadedEvent]s
 */
object FontLoadedEventBroker {
  /**
   * Callbacks for [FontLoadedEvent]s
   */
  private val callbacks = mutableListOf<(event: FontLoadedEvent) -> Unit>()

  /**
   * Registers a lambda that is invoked when an [FontLoadedEvent] is fired
   *
   * @return a disposable that can be used to unregister the callback
   */
  fun onLoaded(callback: (event: FontLoadedEvent) -> Unit): Disposable {
    callbacks.add(callback)

    return Disposable { callbacks.remove(callback) }
  }

  /**
   * Notify this broker that an (any!) font has been loaded
   */
  fun notifyLoaded() {
    val event = FontLoadedEvent()
    callbacks.fastForEach { it(event) }
  }
}
