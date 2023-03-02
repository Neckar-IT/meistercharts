package com.meistercharts.canvas

import com.meistercharts.algorithms.MutableChartState
import com.meistercharts.algorithms.axis.AxisSelection
import it.neckar.open.dispose.OnDispose
import it.neckar.open.async.Async
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Binds the window size to the canvas size
 */
fun interface WindowSizeBindingStrategy {
  /**
   * Binds the window size of the given chart state to the canvas size
   */
  fun bind(
    /**
     * The chart state that should be updated ([MutableChartState.windowSize]).
     */
    chartState: MutableChartState,
    /**
     * The canvas that is resized - consume the resize events of this canvas
     */
    canvas: Canvas,
    /**
     * The on dispose of the current chart.
     * Can be used if necessary to ensure everything is disposed correctly
     */
    onDispose: OnDispose
  )
}

/**
 * Updates the window size immediately to the value of the canvas size
 */
object ImmediateWindowSizeBindingStrategy : WindowSizeBindingStrategy {
  override fun bind(chartState: MutableChartState, canvas: Canvas, onDispose: OnDispose) {
    canvas.sizeProperty.consumeImmediately {
      chartState.windowSize = it
    }
  }

  override fun toString(): String {
    return "ImmediateWindowSizeBindingStrategy"
  }
}

/**
 * Updates the window size after a certain delay to the value of the canvas size
 */
class DelayedWindowSizeBindingStrategy(
  var delay: Duration = 500.0.milliseconds,
  /**
   * The axis that will be bound *delayed*
   */
  var axisSelection: AxisSelection,
) : WindowSizeBindingStrategy {
  override fun bind(chartState: MutableChartState, canvas: Canvas, onDispose: OnDispose) {
    val async = Async().also { onDispose.onDispose(it) }

    canvas.sizeProperty.consumeChanges { oldValue, newValue ->
      if (oldValue.atLeastOneZero() && !newValue.atLeastOneZero()) {
        //Apply immediately if resizing from zero and cancel scheduled resizing
        async.remove(asyncKey)
        chartState.windowSize = newValue
        return@consumeChanges
      }

      //Apply the new value for the unbound axes immediately
      chartState.windowSize = chartState.windowSize.with(newValue, axisSelection.negate())

      //Delay the new value for the bound axes
      async.throttleLast(delay, asyncKey) {
        chartState.windowSize = chartState.windowSize.with(canvas.size, axisSelection) // use the current canvas.size! Not 'newValue'!
      }
    }

    //apply the first size immediately
    chartState.windowSize = canvas.size
  }

  override fun toString(): String {
    return "DelayedWindowSizeBindingStrategy: delay=$delay, axes=$axisSelection"
  }

  companion object {
    /**
     * The key that is used for the async throttleLast calls
     */
    private const val asyncKey: String = "size"
  }
}
