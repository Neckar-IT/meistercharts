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
package com.meistercharts.algorithms

import it.neckar.open.unit.number.Positive

/**
 * Represents the environment in which the application is running.
 *
 */
data class Environment(
  /**
   * Whether the environment supports more than a single touch at a time
   */
  @Deprecated("This property should not be necessary. See ADL 0057. Just register both touch and mouse listeners") val multiTouchSupported: Boolean,

  /**
   * This value represents how many of the screen's actual pixels should be used to draw a single logical pixel.
   * This value changes if the browser zoom is used and must not be cached.
   *
   * More details on how the device pixel ratio is used are documented in the `HtmlCanvasRenderingContext`.
   */
  val devicePixelRatio: @Positive Double,
)

/**
 * Retrieve information about the current environment. This might be slow. Do not use directly.
 * Retrieves information about the current environment. This method may be slow and should not be called directly.
 * Use `com.meistercharts.canvas.ChartSupport.devicePixelRatio` or [environment] instead.
 *
 * The implementation should only return a new object if the environment has changed. Return the old environment object if nothing has changed.
 */
expect fun extractEnvironment(oldEnvironment: Environment): Environment

/**
 * Updates the environment. This method should not be called too often.
 */
fun updateEnvironment() {
  environment = forcedEnvironment ?: extractEnvironment(environment)
}

/**
 * Forces the given environment. This method should not be used in most cases.
 */
fun forceEnvironment(newForcedEnvironment: Environment?) {
  forcedEnvironment = newForcedEnvironment
  environment = newForcedEnvironment ?: extractEnvironment(environment)
}

/**
 * If this variable is set, the `updateEnvironment` method uses the value.
 */
var forcedEnvironment: Environment? = null
  private set

/**
 * Contains the current environment. This instance will be updated automatically (from the LayerSupport).
 */
var environment: Environment = extractEnvironment(Environment(false, 1.0))
  private set

