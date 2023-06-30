package com.meistercharts.loop

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.trace
import it.neckar.open.annotations.TestOnly
import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable
import it.neckar.open.kotlin.lang.ifNaN
import it.neckar.open.time.nowMillis
import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.Relative
import it.neckar.open.unit.si.ms

/**
 * Render loop support
 */
class RenderLoopSupport {
  /**
   * The refresh listeners are notified on every refresh (more often than the paint listeners)
   */
  internal val renderLoopListeners = mutableListOf<RenderLoopListener>()

  /**
   * These refresh listeners will be removed before the next call.
   * Used to avoid concurrent modification exceptions
   */
  internal val renderLoopListenersToRemove = mutableListOf<RenderLoopListener>()


  /**
   * Registers a refresh listener that is notified whenever refresh is called
   * @return A disposable that can be used to unregister the listener
   */
  fun onRender(renderLoopListener: RenderLoopListener): Disposable {
    renderLoopListeners.add(renderLoopListener)

    return Disposable {
      removeOnRender(renderLoopListener)
    }
  }

  /**
   * Unregisters the refresh listener.
   *
   * Attention: The refresh listener is unregistered on the *next* run to avoid concurrent modifications of the listeners list.
   */
  fun removeOnRender(renderLoopListener: RenderLoopListener) {
    //Schedule for removal
    renderLoopListenersToRemove.add(renderLoopListener)
  }

  /**
   * The backing field for the current frame timestamp.
   * Will be set to [Double.NaN] when no frame is currently being painted.
   */
  private var currentFrameTimestampOrNaN: @ms @MayBeNaN Double = Double.NaN

  /**
   * Updates the current frame timestamp. Must only be used for tests
   */
  @TestOnly
  internal fun setCurrentFrameTimestampForTestsOnly(currentFrameTimestamp: @ms @MayBeNaN Double) {
    currentFrameTimestampOrNaN = currentFrameTimestamp
  }

  /**
   * Returns the timestamp of the current frame.
   * This field can be used as kind of "shortcut" to access the current frame timestamp
   *
   * ATTENTION: Will throw an exception if called from outside the frame paint method!
   */
  val currentFrameTimestamp: @ms @IsFinite Double
    get() {
      return currentFrameTimestampOrNaN.ifNaN { throw IllegalStateException("Not currently in a frame") }
    }

  /**
   * Render the next loop.
   * This method must not be called manually!
   *
   * This method will be called by a timer / animation frame.
   */
  fun nextLoop(relativeHighRes: @ms @Relative Double) {
    @ms val now = nowMillis()

    currentFrameTimestampOrNaN = now

    //Cleanup
    renderLoopListeners.removeAll(renderLoopListenersToRemove)
    renderLoopListenersToRemove.clear()

    try {
      logger.trace { "Render Loop. relative: $relativeHighRes" }

      renderLoopListeners.fastForEach { it.render(now, relativeHighRes) }
    } finally {
      //Outside the loop, reset the current frame timestamp
      currentFrameTimestampOrNaN = Double.NaN
    }
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.loop.RenderLoopSupport")
  }
}
