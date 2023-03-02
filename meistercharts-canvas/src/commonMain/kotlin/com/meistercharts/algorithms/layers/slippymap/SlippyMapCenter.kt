package com.meistercharts.algorithms.layers.slippymap

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Distance
import com.meistercharts.model.Latitude
import com.meistercharts.model.Longitude
import com.meistercharts.model.MapCoordinates
import com.meistercharts.model.Zoom
import it.neckar.open.unit.other.deg

/**
 * Ensures that the given coordinates are shown in the center of the window
 * when zoom and translation equal their default values.
 *
 * Possible values for the latitude are between 90° N to 90° S.
 *
 * Possible values for the longitude are between 180° W to 180° E.
 */
class SlippyMapCenter(
  /**
   * The coordinates of the center
   */
  val coordinates: MapCoordinates,

  /**
   * The default zoom to be used
   */
  val defaultSlippyMapZoom: Int = SlippyMapDefaultZoom
) : ZoomAndTranslationDefaults {
  constructor(
    latitude: Latitude,
    longitude: Longitude,

    /**
     * The default zoom to be used
     */
    defaultSlippyMapZoom: Int = SlippyMapDefaultZoom

  ) : this(MapCoordinates(latitude, longitude), defaultSlippyMapZoom)

  val latitude: Latitude
    get() = coordinates.latitude

  val longitude: Longitude
    get() = coordinates.longitude

  private val defaultZoom = toCanvasZoom(defaultSlippyMapZoom)

  override fun defaultZoom(chartCalculator: ChartCalculator): Zoom {
    return defaultZoom
  }

  override fun defaultTranslation(chartCalculator: ChartCalculator): @Zoomed Distance {
    @DomainRelative val relativeX = coordinates.longitudeDomainRelative()
    @DomainRelative val relativeY = coordinates.latitudeDomainRelative()
    @Zoomed val distanceX = chartCalculator.domainRelative2zoomedX(relativeX)
    @Zoomed val distanceY = chartCalculator.domainRelative2zoomedY(relativeY)
    @Zoomed val centerOffsetX = chartCalculator.chartState.windowWidth * 0.5
    @Zoomed val centerOffsetY = chartCalculator.chartState.windowHeight * 0.5
    return Distance.of(-distanceX + centerOffsetX, -distanceY + centerOffsetY)
  }

  companion object {
    val neckarItCenter: SlippyMapCenter = SlippyMapCenter(MapCoordinates.neckarIt, 15)

    @Suppress("SpellCheckingInspection")
    val emmendingen: SlippyMapCenter = SlippyMapCenter(MapCoordinates.emmendingen, 12)
  }
}


fun SlippyMapCenter.withLatitude(latitude: @deg Double): SlippyMapCenter {
  return SlippyMapCenter(Latitude(latitude), coordinates.longitude, defaultSlippyMapZoom)
}

fun SlippyMapCenter.withLongitude(longitude: @deg Double): SlippyMapCenter {
  return SlippyMapCenter(coordinates.latitude, Longitude(longitude), defaultSlippyMapZoom)
}

