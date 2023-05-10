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
package com.meistercharts.algorithms.layers

/**
 * Identifies the type of layer.
 *
 * The type of layer is used to order the different layers
 */
enum class LayerType {
  /**
   * For layers that only execute calculations. These are executed at first (even before the background layers) and should
   * never paint anything or receive any events
   */
  Calculations,

  /**
   * Background layers that always have to be in the back
   */
  Background,

  /**
   * The "default" layer category - above the background and below the notification layers
   */
  Content,

  /**
   * Notification layers are painted above the "default" layers
   */
  Notification;


  /**
   * Returns true if this type is below the given type
   */
  fun below(type: LayerType): Boolean {
    return this.ordinal < type.ordinal
  }

  /**
   * Returns true if this type is the same as or below the given type
   */
  fun sameOrBelow(type: LayerType): Boolean {
    return this.ordinal <= type.ordinal
  }
}
