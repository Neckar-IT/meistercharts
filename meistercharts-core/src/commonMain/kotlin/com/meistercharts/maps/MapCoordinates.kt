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

import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.DefaultI18nConfiguration
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.unit.other.deg
import kotlinx.serialization.Serializable

/**
 * Coordinates on a map
 */
@Serializable
data class MapCoordinates(
  /**
   * The latitude (north to south, usually painted on the y-axis)
   */
  val latitude: Latitude,
  /**
   * The longitude (east to west, usually painted on the x-axis)
   */
  val longitude: Longitude,
) {
  fun format(i18nConfiguration: I18nConfiguration = DefaultI18nConfiguration): String {
    val format = decimalFormat(10, 5)
    return "${format.format(latitude.value, i18nConfiguration)}, ${format.format(longitude.value, i18nConfiguration)}"
  }

  /**
   * returns the following format:
   * 48°28'50.6"N 8°24'11.5"E
   */
  fun formatMapCoordinatesToGps(): String {
    // format default with US pattern because it's the default format of GPS
    return "${latitude.format(I18nConfiguration.US)} ${longitude.format(I18nConfiguration.US)}"
  }

  override fun toString(): String {
    return "MapCoordinates($latitude, $longitude)"
  }

  companion object {
    /**
     * Creates a new instance from double values.
     * Use the constructor with [Latitude] and [Longitude] instead if possible
     */
    operator fun invoke(latitude: Double, longitude: Double): MapCoordinates {
      return MapCoordinates(Latitude(latitude), Longitude(longitude))
    }

    val neckarIt: MapCoordinates = MapCoordinates(
      Latitude(48.4138247),
      Longitude(9.050864314),
    )

    @Suppress("SpellCheckingInspection")
    val emmendingen: MapCoordinates = MapCoordinates( // Emmendingen
      Latitude(48.116979),
      Longitude(7.853423)
    )

    val lizergy: MapCoordinates = MapCoordinates(
      Latitude(48.48074780020653),
      Longitude(8.408058960597911)
    )
  }

  /**
   * Can be used when encoding the object to a GET request
   */
  object QueryParams {
    const val latitude: String = "latitude"
    const val longitude: String = "longitude"
  }
}

/**
 * Returns a new instance with the updated latitude
 */
fun MapCoordinates.withLatitude(latitude: @deg Double): MapCoordinates {
  return MapCoordinates(Latitude(latitude), longitude)
}

/**
 * Returns a new instance with the updated longitude
 */
fun MapCoordinates.withLongitude(longitude: @deg Double): MapCoordinates {
  return MapCoordinates(latitude, Longitude(longitude))
}

