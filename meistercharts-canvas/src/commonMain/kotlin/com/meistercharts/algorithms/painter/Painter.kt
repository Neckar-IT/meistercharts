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
package com.meistercharts.algorithms.painter

import com.meistercharts.annotations.Window
import it.neckar.open.unit.other.px

/**
 * Base class for a painter
 */
interface Painter {
  /**
   * If set to true the x values should be snapped to full pixel values
   */
  val isSnapXValues: Boolean

  /**
   * If set to true the y values should be snapped to full pixel values
   */
  val isSnapYValues: Boolean

  /**
   * Snaps a position on the x axis if [isSnapXValues] is set to true.
   * Uses round.
   */
  @px
  @Window
  fun snapXPosition(@px @Window xValue: Double): Double

  /**
   * Snaps a width on the x axis if [isSnapXValues] is set to true.
   * Uses ceil.
   */
  @px
  @Window
  fun snapWidth(@px @Window xValue: Double): Double

  /**
   * Snaps a position on the x axis if [isSnapYValues] is set to true.
   * Uses round.
   */
  @px
  @Window
  fun snapYPosition(@px @Window yValue: Double): Double

  /**
   * Snaps a height on the x axis if [isSnapXValues] is set to true.
   * Uses ceil.
   */
  @px
  @Window
  fun snapHeight(@px @Window yValue: Double): Double
}
