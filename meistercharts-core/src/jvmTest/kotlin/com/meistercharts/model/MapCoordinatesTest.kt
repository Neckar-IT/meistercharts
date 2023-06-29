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
package com.meistercharts.model

import assertk.*
import assertk.assertions.*
import com.meistercharts.geometry.geo.Latitude
import com.meistercharts.geometry.geo.Longitude
import com.meistercharts.geometry.geo.MapCoordinates
import org.junit.jupiter.api.Test

@Suppress("TestMethodWithoutAssertion", "SpellCheckingInspection")
class MapCoordinatesTest {

  @Test
  fun `test North East`() {
    val coordsLizergy = MapCoordinates.lizergy
    assertThat(coordsLizergy.latitude.isNorth()).isTrue()
    assertThat(coordsLizergy.latitude.isSouth()).isFalse()
    assertThat(coordsLizergy.longitude.isWest()).isFalse()
    assertThat(coordsLizergy.longitude.isEast()).isTrue()
  }

  @Test
  fun `test North West`() {
    val coords = MapCoordinates(Latitude(49.078814), Longitude(-0.373374))
    assertThat(coords.latitude.isNorth()).isTrue()
    assertThat(coords.latitude.isSouth()).isFalse()
    assertThat(coords.longitude.isWest()).isTrue()
    assertThat(coords.longitude.isEast()).isFalse()
  }

  @Test
  fun `test South West`() {
    val coords = MapCoordinates(Latitude(-10.512792), Longitude(-48.047847))
    assertThat(coords.latitude.isNorth()).isFalse()
    assertThat(coords.latitude.isSouth()).isTrue()
    assertThat(coords.longitude.isWest()).isTrue()
    assertThat(coords.longitude.isEast()).isFalse()
  }

  @Test
  fun `test South East`() {
    val coords = MapCoordinates(Latitude(-23.614329), Longitude(136.368066))
    assertThat(coords.latitude.isNorth()).isFalse()
    assertThat(coords.latitude.isSouth()).isTrue()
    assertThat(coords.longitude.isWest()).isFalse()
    assertThat(coords.longitude.isEast()).isTrue()
  }

  @Test
  fun testZeroCoords() {
    val coordsZero = MapCoordinates( Latitude(0.0), Longitude(0.0))
    assertThat(coordsZero.latitude.isNorth()).isTrue()
    assertThat(coordsZero.longitude.isEast()).isTrue()
  }

  @Test
  fun testGpsConvert() {
    assertThat(MapCoordinates.lizergy.formatMapCoordinatesToGps()).isEqualTo("""48°28'50.7"N 8°24'29.0"E""")
    assertThat(MapCoordinates.neckarIt.formatMapCoordinatesToGps()).isEqualTo("""48°24'49.8"N 9°3'3.1"E""")
    assertThat(MapCoordinates(Latitude(0.0), Longitude(0.0)).formatMapCoordinatesToGps()).isEqualTo("""0°0'0.0"N 0°0'0.0"E""")
  }

}
