package com.meistercharts.custom.rainsensor

import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Distance
import com.meistercharts.model.Size
import com.meistercharts.resources.LocalResourcePaintable
import it.neckar.open.unit.other.deg

/**
 * Contains the resources for the rain sensor gestalt/layer
 */
object RainSensorResources {
  val rainDrop: LocalResourcePaintable = LocalResourcePaintable("rain-detector/rain.png", Size.of(42, 81).divide(4.0))
  val snowFlake: LocalResourcePaintable = LocalResourcePaintable("rain-detector/snow.png", Size.of(80, 80).divide(4.0))
  val sun: LocalResourcePaintable = LocalResourcePaintable("rain-detector/sun.png", Size.of(1786, 2084).divide(4.0))

  /**
   * The fixed size of the roof
   */
  val roofSize: @Domain Size = Size.of(6894, 4324)

  /**
   * TODO how to calculate???
   */
  const val roofAngle: @deg Double = 30.0

  /**
   * The origin of the window
   */
  val windowOrigin: @DomainRelative Coordinates = Coordinates.of(3560.0, 1695.0).normalize(roofSize)

  /**
   * Where the window ends
   */
  val windowEnd: @DomainRelative Coordinates = Coordinates.of(5629.0, 2886.0).normalize(roofSize)

  /**
   * The height of the window
   */
  val windowHeight: @DomainRelative Double = 1 / roofSize.height * 170.0

  /**
   * The length of the window
   */
  val windowDelta: Distance = windowEnd.delta(windowOrigin)


  /**
   * The resource of the roof
   */
  val roofResource: LocalResourcePaintable = LocalResourcePaintable("rain-detector/house.png", roofSize.divide(4.0))

  val sensorResource: LocalResourcePaintable = LocalResourcePaintable("rain-detector/sensor-with-cable.png", Size.of(376, 292).divide(4.0))
  val sensorSize: @DomainRelative Size = Size.of(376, 292).normalize(roofSize)

  val sensorWithHeatResource: LocalResourcePaintable = LocalResourcePaintable("rain-detector/sensor-with-cable-and-heat.png", Size.of(376, 416).divide(4.0))
  val sensorWithHeatSize: @DomainRelative Size = Size.of(376, 416).normalize(roofSize)

  /**
   * Icons
   */

  val iconRain: LocalResourcePaintable = LocalResourcePaintable("rain-detector/icon-rain.png", Size.of(95, 178).divide(4.0))
  val iconSnow: LocalResourcePaintable = LocalResourcePaintable("rain-detector/icon-snow.png", Size.of(166, 167).divide(4.0))
  val iconSun: LocalResourcePaintable = LocalResourcePaintable("rain-detector/icon-sun.png", Size.of(224, 200).divide(4.0))

}
