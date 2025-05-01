package com.meistercharts.maps

/**
 *
 */
object MapCoordinatesExample {
  val neckarIt: MapCoordinates = MapCoordinates(
    Latitude(48.4138247),
    Longitude(9.050864314),
  )

  val emmendingen: MapCoordinates = MapCoordinates( // Emmendingen
    Latitude(48.116979),
    Longitude(7.853423)
  )

  val lizergy: MapCoordinates = MapCoordinates(
    Latitude(48.48074780020653),
    Longitude(8.408058960597911)
  )
}

val MapCoordinates.Companion.Example: MapCoordinatesExample
  get() = MapCoordinatesExample
