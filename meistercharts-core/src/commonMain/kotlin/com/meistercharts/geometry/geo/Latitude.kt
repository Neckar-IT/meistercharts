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
package com.meistercharts.geometry.geo

import it.neckar.open.formatting.format
import it.neckar.open.i18n.DefaultI18nConfiguration
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.unit.other.deg
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * The latitude (90° N - 90° S) of the location in the center of the window
 *
 * Positive latitude is above the equator (N), and negative latitude is below the equator (S).
 * example: 48.48074780020653 --> North
 * example: -10.343241 --> South
 *
 * If the Latitude is Zero Degrees (0°) the cardinal direction is North
 *
 */
@JvmInline
@Serializable
value class Latitude(
  val value: @deg Double,
) {
  override fun toString(): String {
    return value.toString()
  }

  fun isSouth(): Boolean {
    return isNorth().not()
  }

  fun isNorth(): Boolean {
    return value >= 0.0
  }

  /**
   * returns the Latitude value as a formatted GPS value
   * pattern: {degree}°{minutes}'{seconds}"{S|N}
   *
   * more: https://en.wikipedia.org/wiki/Geographic_coordinate_system
   */
  fun format(i18nConfiguration: I18nConfiguration = DefaultI18nConfiguration): String {
    val latitudeMinutesCalculation = ((value % 1) * 60)
    val latitudeSecondsCalculation = (latitudeMinutesCalculation % 1) * 60
    return buildString {
      append(value.toInt())
      append("°")
      append(latitudeMinutesCalculation.toInt())
      append("'")
      append(latitudeSecondsCalculation.format(1, i18nConfiguration = i18nConfiguration))
      append("\"")
      append(if (isSouth()) "S" else if (isNorth()) "N" else "")
    }
  }

}
