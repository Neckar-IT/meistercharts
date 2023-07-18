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
package com.meistercharts.maps

import it.neckar.open.formatting.format
import it.neckar.open.i18n.DefaultI18nConfiguration
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.unit.other.deg
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * The longitude (180° W - 180° E) of the location in the center of the window
 *
 * Positive longitude means East of the Prime Meridian, and negative longitude means West of the Prime Meridian
 * example: 48.48074780020653 --> East
 * example: -17.7159407 --> West
 *
 * If the Longitude is Zero Degrees (0°) the cardinal direction is East
 *
 */
@JvmInline
@Serializable
value class Longitude(
  val value: @deg Double
) {
  override fun toString(): String {
    return value.toString()
  }

  fun isWest(): Boolean {
    return isEast().not()
  }

  fun isEast(): Boolean {
    return value >= 0.0
  }

  /**
   * returns the Longitude value as a formatted GPS value
   * pattern: {degree}°{minutes}'{seconds}"{E|W}
   *
   * more: https://en.wikipedia.org/wiki/Geographic_coordinate_system
   */
  @Suppress("UNUSED_PARAMETER")
  fun format(i18nConfiguration: I18nConfiguration = DefaultI18nConfiguration): String {
    val longitudeMinutesCalculation = ((value % 1) * 60)
    val longitudeSecondsCalculation = (longitudeMinutesCalculation % 1) * 60
    return buildString {
      append(value.toInt())
      append("°")
      append(longitudeMinutesCalculation.toInt())
      append("'")
      append(longitudeSecondsCalculation.format(1))
      append("\"")
      append(if (isWest()) "W" else if (isEast()) "E" else "")
    }
  }

}
