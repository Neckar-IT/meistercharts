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
