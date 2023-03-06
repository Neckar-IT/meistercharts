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
