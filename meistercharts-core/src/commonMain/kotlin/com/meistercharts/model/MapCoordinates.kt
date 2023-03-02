package com.meistercharts.model

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
  val latitude: Latitude,
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

  companion object {
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

