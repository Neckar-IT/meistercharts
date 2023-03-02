package com.meistercharts.canvas

import it.neckar.open.observable.ObservableDouble
import it.neckar.open.observable.ReadOnlyObservableDouble
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug

/**
 * Contains methods related to the device pixel ratio.
 */
class DevicePixelRatioSupport {
  private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.canvas.DevicePixelRatioSupport")

  /**
   * The device pixel ratio
   */
  val devicePixelRatioProperty: ReadOnlyObservableDouble = ObservableDouble(1.0).also {
    it.consumeImmediately {
      logger.debug { "devicePixelRatio set to $it" }
    }
  }

  /**
   * The device pixel ratio
   */
  val devicePixelRatio: Double by devicePixelRatioProperty

  /**
   * Updates the device pixel ratio.
   * This method will only be called from the LayerSupport
   */
  fun updateDevicePixelRatio(devicePixelRatio: Double) {
    (devicePixelRatioProperty as ObservableDouble).value = devicePixelRatio
  }
}
