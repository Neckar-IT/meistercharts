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
