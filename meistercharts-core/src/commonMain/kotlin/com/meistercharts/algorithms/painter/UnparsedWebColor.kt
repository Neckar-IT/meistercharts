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
package com.meistercharts.algorithms.painter

/**
 * Represents an unparsed web color.
 * Which might or might not be parsed to a [Color] object.
 *
 * This instance can be used to be set as a color in a [CanvasPaint].
 */
data class UnparsedWebColor(override val web: String) : Color, CanvasPaint {
  init {
    require(web.isNotEmpty()) { "empty string not allowed" }
    if (web.startsWith("#")) {
      require(!web.contains(' ')) { "Must not contain spaces: <$web>" }
    }
  }

  override fun toCanvasPaint(x0: Double, y0: Double, x1: Double, y1: Double): CanvasPaint {
    return this
  }

  /**
   * Parses this color
   */
  override fun toRgba(): RgbaColor {
    return Color.parseHexOrRgba(web)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Color) return false

    return web == other.web
  }

  override fun hashCode(): Int {
    return web.hashCode()
  }

  override fun toString(): String {
    return web
  }
}
