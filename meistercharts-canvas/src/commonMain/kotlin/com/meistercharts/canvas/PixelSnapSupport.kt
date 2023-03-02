package com.meistercharts.canvas

import it.neckar.open.observable.ObservableObject
import it.neckar.open.unit.other.px

/**
 * Supports snapping of pixels (if enabled)
 */
class PixelSnapSupport(
  /**
   * The initial snap configuration
   */
  snapConfiguration: SnapConfiguration = SnapConfiguration.None
) {
  /**
   * Backing observable property that contains the current snap configuration
   */
  val snapConfigurationProperty: ObservableObject<SnapConfiguration> = ObservableObject(snapConfiguration)

  /**
   * The snap configuration
   */
  var snapConfiguration: SnapConfiguration by snapConfigurationProperty

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapXValue(@px value: Double): Double {
    return snapConfiguration.snapXValue(value)
  }

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapXSize(@px value: Double): Double {
    return snapConfiguration.snapXSize(value)
  }

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapYValue(@px value: Double): Double {
    return snapConfiguration.snapYValue(value)
  }

  /**
   * Snaps the value to *physical* pixels
   */
  @px
  fun snapYSize(@px value: Double): Double {
    return snapConfiguration.snapYSize(value)
  }
}
