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
package com.meistercharts.algorithms.layers.axis

import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.layout.cache.DoubleCache

/**
 * Painting variables for a value axis with offsets
 */
interface ValueAxisWithOffsetPaintingVariables : ValueAxisPaintingVariables {
  /**
   * The ticks for the offsets
   */
  val offsetTicks: @Domain DoubleCache

  /**
   * Current magnitude of the delta between [startDomainValue] and [endDomainValue]
   */
  val deltaMagnitude: Int

  /**
   * The best fitting number of integer digits to be displayed for values
   * Is always as close to [Style.spaceForDigits] as possible, unless when zooming in
   */
  val integerDigits: Int

  /**
   * The best fitting number of fraction digits to be displayed for values
   * Only goes above 0 when zooming in
   */
  val fractionDigits: Int

  /**
   * The step size between offset ticks
   */
  val offsetStep: Double
}

abstract class ValueAxisWithOffsetPaintingVariablesImpl : ValueAxisPaintingVariablesImpl(), ValueAxisWithOffsetPaintingVariables {
  /**
   * The ticks for the offset
   */
  override var offsetTicks: @Domain DoubleCache = DoubleCache()

  /**
   * Current magnitude of the delta between [startDomainValue] and [endDomainValue]
   */
  override var deltaMagnitude: Int = 1

  /**
   * The best fitting number of integer digits to be displayed for values
   * Is always as close to [Style.spaceForDigits] as possible, unless when zooming in
   */
  override var integerDigits: Int = 6

  /**
   * The best fitting number of fraction digits to be displayed for values
   * Only goes above 0 when zooming in
   */
  override var fractionDigits: Int = 0

  /**
   * The step size between offset ticks
   */
  override var offsetStep: Double = 1.0

  override fun reset() {
    super.reset()
    offsetTicks.reset()

    deltaMagnitude = 1
    integerDigits = 6
    fractionDigits = 0
    offsetStep = 1.0
  }
}
