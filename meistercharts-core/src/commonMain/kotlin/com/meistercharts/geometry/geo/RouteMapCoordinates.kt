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

import com.meistercharts.geometry.geo.MapCoordinates
import kotlinx.serialization.Serializable

/**
 * Contains two [MapCoordinates] as a Route
 */
@Serializable
data class RouteMapCoordinates(
  /**
   * Coords of the starting point of the Route
   */
  val startMapCoordinates: MapCoordinates,
  /**
   * Coords of the destination point of the Route
   */
  val destinationMapCoordinates: MapCoordinates,

  ) {
  companion object {
    @Suppress("SpellCheckingInspection")
    val fromNeckarItToLizergy: RouteMapCoordinates = RouteMapCoordinates(
      startMapCoordinates = MapCoordinates.neckarIt,
      destinationMapCoordinates = MapCoordinates.lizergy
    )
    val fromEmmendingenToLizergy: RouteMapCoordinates = RouteMapCoordinates(
      startMapCoordinates = MapCoordinates.emmendingen,
      destinationMapCoordinates = MapCoordinates.lizergy
    )
  }
}
