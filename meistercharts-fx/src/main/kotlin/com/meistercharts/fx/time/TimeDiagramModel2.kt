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
