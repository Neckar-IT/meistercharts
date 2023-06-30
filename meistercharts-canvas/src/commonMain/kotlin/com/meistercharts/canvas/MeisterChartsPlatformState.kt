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
package com.meistercharts.canvas

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.open.collections.fastForEach
import it.neckar.open.dispose.Disposable

/**
 * Contains the state of the platform
 */
class MeisterChartsPlatformState {
  /**
   * Holds the active instances
   */
  private val activeInstances: MutableSet<MeisterChart> = mutableSetOf()

  val hasInstances: Boolean
    get() = activeInstances.isNotEmpty()

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
    logger.debug { "New instance ${meisterChart.chartSupport.chartId}" }

    activeInstances.add(meisterChart)

    if (activeInstances.size == 1) {
      platformStateListeners.fastForEach {
        it.firstInstanceCreated(meisterChart)
      }
    }

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
        logger.debug { "Last instance disposed: ${meisterChart.chartSupport.chartId}" }
        it.lastInstanceDisposed()
      }
    }
  }

  /**
   * The listeners
   */
  private val platformStateListeners = mutableListOf<PlatformStateListener>()

  /**
   * Register a new listener
   */
  fun onPlatformStateUpdate(listener: PlatformStateListener): Disposable {
    platformStateListeners.add(listener)

    return Disposable {
      platformStateListeners.remove(listener)
    }
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.canvas.MeisterChartsPlatformState")
  }
}

/**
 * Listener that is notified about platform actions
 */
interface PlatformStateListener {
  /**
   * Is called when the first instance has been created.
   *
   * Note: This method is called every time the last instance has been disposed and a new instance is created.
   */
  fun firstInstanceCreated(meisterChart: MeisterChart) {}

  /**
   * Is called when an instance has been created.
   *
   * Note: This method is called for *every* instance that is created. Also for the *first* one.
   */
  fun instanceCreated(meisterChart: MeisterChart) {}

  /**
   * Is called when the last instance has been disposed
   */
  fun lastInstanceDisposed() {}
}
