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
package com.meistercharts.algorithms.time

import it.neckar.open.collections.fastForEach
import it.neckar.open.unit.si.ms

/**
 * Base class for time diagram model
 */
open class TimeDiagramModel<T>(
  /**
   * The max data points for this model.
   * If more points are added the oldest data points are removed automatically
   */
  maxDataPointsCount: Int,
  /**
   * The expected distance between two data points (on average)
   */
  @ms expectedDistanceBetweenDataPoints: Double
) : BaseTimeModel<T>(maxDataPointsCount, expectedDistanceBetweenDataPoints) {
  /**
   * Retrieves the last data point - if there is one
   */
  val lastDataPoint: DataPoint<T>?
    get() = dataPoints.lastOrNull()

  /**
   * Retrieves the first data point - if there is one
   */
  val firstDataPoint: DataPoint<T>?
    get() = dataPoints.firstOrNull()

  private val valueChangeListeners: MutableList<(model: TimeDiagramModel<T>, dataPoints: List<DataPoint<T>>) -> Unit> = mutableListOf()

  /**
   * Adds a change listener
   */
  fun addChangeListener(action: (model: TimeDiagramModel<T>, dataPoints: List<DataPoint<T>>) -> Unit) {
    valueChangeListeners.add(action)
  }

  /**
   * Removes a change listener
   */
  fun removeChangeListener(action: (model: TimeDiagramModel<T>, dataPoints: List<DataPoint<T>>) -> Unit) {
    valueChangeListeners.remove(action)
  }

  /**
   * Updates the listeners whenever the data points have been updated
   */
  override fun notifyUpdated(dataPoints: List<DataPoint<T>>) {
    valueChangeListeners.fastForEach {
      it(this, dataPoints)
    }
  }
}

