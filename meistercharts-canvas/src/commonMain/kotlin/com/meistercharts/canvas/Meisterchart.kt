package com.meistercharts.canvas

import com.meistercharts.canvas.layer.LayerSupport
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.OnDispose

/**
 * The(!) MeisterChart interface
 */
interface Meisterchart : Disposable, OnDispose {
  val chartSupport: ChartSupport

  /**
   * Contains a description that helps to understand why this MeisterCharts instance has been created and where it is used.
   * Only for debugging purposes.
   */
  val description: String

  val layerSupport: LayerSupport
    get() {
      return chartSupport.layerSupport
    }

  override fun dispose() {
    chartSupport.dispose()
  }

  override fun onDispose(action: () -> Unit) {
    chartSupport.onDispose(action)
  }

  /**
   * Marks the canvas as dirty
   */
  fun markAsDirty(reason: DirtyReason) {
    chartSupport.markAsDirty(reason)
  }
}
