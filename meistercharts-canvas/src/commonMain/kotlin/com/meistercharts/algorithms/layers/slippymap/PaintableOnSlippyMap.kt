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
package com.meistercharts.algorithms.layers.slippymap

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.maps.Latitude
import com.meistercharts.maps.Longitude
import com.meistercharts.maps.MapCoordinates
import com.meistercharts.maps.latitude2DomainRelative
import com.meistercharts.maps.longitude2DomainRelative

/**
 * Paints [paintable] at the given map coordinates
 */
class PaintableOnSlippyMap<out T : Paintable>(
  var location: MapCoordinates,

  /**
   * The paintable that is painted at the given coordinates
   */
  val paintable: T
) {

  constructor(
    latitude: Latitude,
    longitude: Longitude,
    paintable: T
  ) : this(MapCoordinates(latitude, longitude), paintable)

  /**
   * Paints the paintable at the given location
   */
  fun paint(paintingContext: LayerPaintingContext) {
    @Window val windowX = paintingContext.chartCalculator.domainRelative2windowX(location.longitude2DomainRelative())
    @Window val windowY = paintingContext.chartCalculator.domainRelative2windowY(location.latitude2DomainRelative())
    paintable.paint(paintingContext, windowX, windowY)
  }
}
