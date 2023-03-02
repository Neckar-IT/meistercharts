package com.meistercharts.algorithms.layers

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.MouseCursor
import com.meistercharts.canvas.mouseCursorSupport
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport

/**
 * Abstract base class for [Layer] that has several commonly used methods
 */
abstract class AbstractLayer : Layer {
  /**
   * The [DisposeSupport] for the layer
   */
  protected val disposeSupport: DisposeSupport = DisposeSupport()

  /**
   * Calls [disposeSupport#dispose]
   */
  override fun removed() {
    disposeSupport.dispose()
  }

  /**
   * Registers the disposable at the dispose support of this layer
   */
  fun Disposable.disposeOnRemove() {
    disposeSupport.onDispose(this)
  }

  /**
   * Is set to true if [initialize] has been called.
   *
   * This variable is *not* set in the [initialize] method. Instead, it is automatically set after [layout] has
   * been called the first time, which itself calls [initialize].
   */
  var initialized: Boolean = false
    private set

  /**
   * Initialize method that is called the first time [layout] is called.
   *
   * This method is only called once.
   */
  open fun initialize(paintingContext: LayerPaintingContext) {}

  override fun layout(paintingContext: LayerPaintingContext) {
    super.layout(paintingContext)
    initializeIfNecessary(paintingContext)
    paintingVariables()?.calculate(paintingContext)
  }

  /**
   * Calls the [initialize] method if not already initialized
   */
  protected fun initializeIfNecessary(paintingContext: LayerPaintingContext) {
    if (!initialized) {
      initialize(paintingContext)
      initialized = true
    }
  }

  /**
   * The mouse cursor for this layer
   *
   * Hint: This method requires the layer and chart support. It can not be moved somewhere else
   */
  var ChartSupport.cursor: MouseCursor?
    get() {
      return mouseCursorSupport.cursorProperty(this@AbstractLayer).value
    }
    set(value) {
      mouseCursorSupport.cursorProperty(this@AbstractLayer).value = value
    }
}
