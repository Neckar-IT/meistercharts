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
package com.meistercharts.whatsat

import com.meistercharts.annotations.Window
import com.meistercharts.geometry.Coordinates

/**
 * Contains information about stuff that is currently at a given location
 */
data class WhatsAtResult(
  /**
   * The location for this result (from the request)
   */
  val location: @Window Coordinates,
  /**
   * The precision for this result (from the request)
   */
  val precision: WhatsAtSupport.Precision,

  /**
   * The elements
   */
  val elements: List<WhatsAtResultElement<*>>,
)
