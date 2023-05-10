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
