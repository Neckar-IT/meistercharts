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
package com.meistercharts

import com.meistercharts.canvas.Image
import com.meistercharts.canvas.MeisterchartFactory
import com.meistercharts.canvas.MeisterChartsPlatformState
import com.meistercharts.font.FontMetricsCache
import com.meistercharts.loop.RenderLoopSupport
import it.neckar.open.collections.Cache
import it.neckar.open.collections.cache
import it.neckar.open.http.Url

/**
 * Contains all static references to all meistercharts related classes
 */
object Meistercharts {
  /**
   * Offers access to the game loop
   */
  val renderLoop: RenderLoopSupport = RenderLoopSupport()

  /**
   * Contains information about the platform state - especially the active instances
   */
  val platformState: MeisterChartsPlatformState = MeisterChartsPlatformState()


  /**
   * The image cache.
   * Key is the URL.
   */
  val imageCache: Cache<Url, Image> = cache("ImageCache", 100)

  /**
   * The current font metrics cache.
   * The value is initialized in the Platform.init() method.
   *
   * Should be accessed by [com.meistercharts.font.FontMetricsCache.Companion.get]
   */
  var fontMetricsCache: FontMetricsCache? = null

  /**
   * The current factory. Should be set once at startup - specific for each platform
   */
  var meisterchartFactory: MeisterchartFactory? = null

  /**
   * The device pixel ratio for the main screen
   */
  var mainScreenDevicePixelRatio: Double = 1.0

}
