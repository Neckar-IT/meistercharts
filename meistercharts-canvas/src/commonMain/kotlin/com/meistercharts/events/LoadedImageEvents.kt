package com.meistercharts.events

import it.neckar.open.dispose.Disposable

/**
 * A non-cancellable Event indicating that an image has been successfully loaded
 */
class ImageLoadedEvent

/**
 * Handles [ImageLoadedEvent]s
 */
object ImageLoadedEventBroker {
  /**
   * Callbacks for [ImageLoadedEvent]s
   */
  private val callbacks = mutableListOf<(event: ImageLoadedEvent) -> Unit>()

  /**
   * Registers a lambda that is invoked when an [ImageLoadedEvent] is fired
   *
   * @return a disposable that can be used to unregister the callback
   */
  fun onLoaded(callback: (event: ImageLoadedEvent) -> Unit): Disposable {
    callbacks.add(callback)

    return Disposable { callbacks.remove(callback) }
  }

  /**
   * Notify this broker that an (any!) image has been loaded
   */
  fun notifyLoaded() {
    val event = ImageLoadedEvent()
    callbacks.forEach { it(event) }
  }
}
