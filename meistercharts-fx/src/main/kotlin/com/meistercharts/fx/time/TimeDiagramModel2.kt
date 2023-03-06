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
package com.meistercharts.fx.time

import com.meistercharts.algorithms.time.DataPoint
import com.meistercharts.algorithms.time.TimeDiagramModel
import it.neckar.open.unit.si.ms
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Base class for time diagram model
 */
@Deprecated("use TimeDiagramModel instead")
open class TimeDiagramModel2<T>(
  maxDataPointsCount: Int,
  @ms expectedDistanceBetweenDataPoints: Double
) : TimeDiagramModel<T>(maxDataPointsCount, expectedDistanceBetweenDataPoints), Observable {

  private val listeners = CopyOnWriteArrayList<InvalidationListener>()

  override fun addListener(listener: InvalidationListener) {
    listeners.add(listener)
  }

  override fun removeListener(listener: InvalidationListener) {
    listeners.remove(listener)
  }

  override fun notifyUpdated(dataPoints: List<DataPoint<T>>) {
    super.notifyUpdated(dataPoints)
    for (listener in listeners) {
      listener.invalidated(this)
    }
  }
}
