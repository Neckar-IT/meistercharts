package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.layers.ValueAxisWithOffsetLayer.Style
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
