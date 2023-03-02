package com.meistercharts.model

import it.neckar.open.unit.other.deg
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
