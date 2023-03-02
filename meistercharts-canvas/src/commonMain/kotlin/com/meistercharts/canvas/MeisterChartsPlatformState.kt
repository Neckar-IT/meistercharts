package com.meistercharts.canvas

import it.neckar.open.collections.fastForEach

/**
 * Contains the state of the platform
 */
object MeisterChartsPlatformState {
  /**
   * Holds the active instances
   */
  private val activeInstances: MutableSet<MeisterChart> = mutableSetOf()

  /**
   * Returns the active chart instances
   */
  fun activeInstances(): List<MeisterChart> {
    return activeInstances.toList()
  }

  /**
   * Registers a new instance
   */
  fun newInstance(meisterChart: MeisterChart) {
    activeInstances.add(meisterChart)

    platformStateListeners.fastForEach {
      it.instanceCreated(meisterChart)
    }
  }

  /**
   * Is called if an instance has been disposed
   */
  fun instanceDisposed(meisterChart: MeisterChart) {
    if (!activeInstances.remove(meisterChart)) {
      throw IllegalStateException("cannot remove an inactive MeisterChart instance")
    }

    if (activeInstances.isEmpty()) {
      platformStateListeners.fastForEach {
        it.lastInstanceDisposed()
      }
    }
  }

  /**
   * The listeners
   */
  private val platformStateListeners = mutableListOf<PlatformStateListener>()
}

/**
 * Listener that is notified about platform actions
 */
interface PlatformStateListener {
  /**
   * Is called when an instance has been created
   */
  fun instanceCreated(meisterChart: MeisterChart)

  /**
   * Is called when the last instance has been disposed
   */
  fun lastInstanceDisposed()
}
