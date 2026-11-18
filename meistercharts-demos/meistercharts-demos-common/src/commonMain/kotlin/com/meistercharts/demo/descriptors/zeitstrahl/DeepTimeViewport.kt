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
package com.meistercharts.demo.descriptors.zeitstrahl

import kotlin.math.floor
import kotlin.math.log2

/**
 * The visible window of the deep time axis as center and seconds per pixel.
 *
 * Replaces zoom and translation of MeisterCharts: between the age of the universe and one day the zoom factor reaches 5·10¹²,
 * and a position computed as `(t - centerSeconds) / secondsPerPixel` keeps the resolution of `t` itself.
 */
class DeepTimeViewport(
  /** Seconds since 1970-01-01T00:00:00Z of the present, the latest window center. */
  val presentSeconds: Double,
) {
  var centerSeconds: Double = (BigBangSeconds + presentSeconds) / 2
    private set

  var secondsPerPixel: Double = 1.0
    private set

  var windowWidth: Double = 0.0
    private set

  /** The zoom level of the detail levels and of the lane query; the full span on a narrow window would reach 49. */
  val zoomLevel: ZoomLevel
    get() = ZoomLevel(floor(log2(secondsPerPixel)).toInt().coerceIn(ZoomLevel.MinValue, ZoomLevel.MaxValue))

  val visibleFrom: Double
    get() = secondsAt(0.0)

  val visibleTo: Double
    get() = secondsAt(windowWidth)

  val visibleSeconds: Double
    get() = secondsPerPixel * windowWidth

  fun xOf(seconds: Double): Double = windowWidth / 2 + (seconds - centerSeconds) / secondsPerPixel

  fun secondsAt(x: Double): Double = centerSeconds + (x - windowWidth / 2) * secondsPerPixel

  /** Half-open: a span that ends where the window starts lies outside, like the neighbour section of a periodization. */
  fun overlaps(start: Double, end: Double): Boolean = end > visibleFrom && start < visibleTo

  /** The pixels of [start] to [end] inside the window. */
  fun visibleXRange(start: Double, end: Double): ClosedFloatingPointRange<Double> = maxOf(xOf(start), 0.0)..minOf(xOf(end), windowWidth)

  /** The first width shows the whole span from the Big Bang to the present. */
  fun updateWindowWidth(width: Double) {
    // MeisterCharts paints once with width 0 before the first layout of the canvas.
    if (width <= 0.0 || width == windowWidth) {
      return
    }
    val showsWholeSpan = windowWidth == 0.0
    windowWidth = width
    if (showsWholeSpan) {
      secondsPerPixel = maxSecondsPerPixel
    }
    clamp()
  }

  /** Zooms by [factor] so that the time below [x] stays below [x]; a factor above 1 zooms out. */
  fun zoomAround(x: Double, factor: Double) {
    val secondsAtX = secondsAt(x)
    secondsPerPixel = (secondsPerPixel * factor).coerceIn(minSecondsPerPixel, maxSecondsPerPixel)
    centerSeconds = secondsAtX - (x - windowWidth / 2) * secondsPerPixel
    clamp()
  }

  fun showWindow(from: Double, to: Double) {
    secondsPerPixel = (to - from) / windowWidth
    centerSeconds = (from + to) / 2
    clamp()
  }

  fun panBy(deltaX: Double) {
    centerSeconds -= deltaX * secondsPerPixel
    clamp()
  }

  /** One day across the window. */
  private val minSecondsPerPixel: Double
    get() = ProlepticGregorianCalendar.SecondsPerDay / windowWidth

  /** The span from the Big Bang to the present plus a margin of 10 %. */
  private val maxSecondsPerPixel: Double
    get() = (presentSeconds - BigBangSeconds) * 1.1 / windowWidth

  private fun clamp() {
    secondsPerPixel = secondsPerPixel.coerceIn(minSecondsPerPixel, maxSecondsPerPixel)
    centerSeconds = centerSeconds.coerceIn(BigBangSeconds, presentSeconds)
  }

  companion object {
    /** 13.787 billion years before 1970 */
    const val BigBangSeconds: Double = -4.35075697224e17
  }
}
