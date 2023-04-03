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
package com.meistercharts.resources

import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmOverloads

/**
 * Loads a local resource
 */
expect class LocalResourcePaintable @JvmOverloads constructor(
  relativePath: String,
  /**
   * The size - if set to null the natural size will be used
   */
  size: @px Size? = null,

  /**
   * The alignment point for the bounding box
   */
  alignmentPoint: Coordinates = Coordinates.origin
) : Paintable {
  fun withSize(size: Size): LocalResourcePaintable

  companion object
}

/**
 * Creates a paintable for a relative path to a local resource
 */
fun Paintable.Companion.localResource(
  relativePath: String,
  size: Size? = null,
  basePoint: Coordinates = Coordinates.none
): LocalResourcePaintable {
  return LocalResourcePaintable(relativePath, size, basePoint)
}

/**
 * Creates a paintable for a relative path to a local resource. Will return null if the resource does not exist.
 */
fun Paintable.Companion.localResourceOrNull(
  relativePath: String,
  size: Size? = null,
  basePoint: Coordinates = Coordinates.none
): LocalResourcePaintable? {
  return try {
    localResource(relativePath, size, basePoint)
  } catch (ignore: Exception) {
    null
  }
}
