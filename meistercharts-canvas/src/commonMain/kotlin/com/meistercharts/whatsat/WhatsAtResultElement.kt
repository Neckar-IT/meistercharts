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
import com.meistercharts.geometry.Rectangle

/**
 * Represents one elements for a [WhatsAtResult].
 */
data class WhatsAtResultElement<T>(
  /**
   * The type for the result.
   */
  val type: ResultElementType<T>,

  /**
   * The exact location of the element - *not* the required location
   */
  val location: @Window Coordinates? = null,

  /**
   * The optional bounding box of the element (if there is any)
   */
  val boundingBox: @Window Rectangle? = null,

  /**
   * The (optional) label
   */
  val label: String? = null,
  /**
   * The (optional) value
   */
  val value: Number? = null,
  /**
   * The (optional) formatted value
   */
  val valueFormatted: String? = null,

  /**
   * Additional (more specific) data
   */
  val data: T,
)
