/*
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
