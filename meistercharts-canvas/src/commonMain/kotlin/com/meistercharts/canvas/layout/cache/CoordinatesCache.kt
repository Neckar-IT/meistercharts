package com.meistercharts.canvas.layout.cache

import com.meistercharts.annotations.Window
import it.neckar.open.provider.CoordinatesProvider
import it.neckar.open.provider.CoordinatesProvider1

/**
 * Caches coordinates (x,y) without creating any objects
 *
 * Must only be used for layout objects in layers.
 *
 * Is *NOT* thread safe!
 */
class CoordinatesCache : LayoutVariableWithSize {
  /**
   * The x locations
   */
  @PublishedApi
  internal var xValues: @Window DoubleCache = DoubleCache()

  /**
   * The y locations
   */
  @PublishedApi
  internal var yValues: @Window DoubleCache = DoubleCache()

  override fun reset() {
    xValues.reset()
    yValues.reset()
  }

  /**
   * Ensures all elements have the given size
   * Recreates the arrays if necessary
   */
  override fun ensureSize(size: Int) {
    xValues.ensureSize(size)
    yValues.ensureSize(size)
  }

  override val size: Int
    get() = xValues.size


  fun set(index: Int, x: @Window Double, y: @Window Double) {
    this.xValues[index] = x
    this.yValues[index] = y
  }

  /**
   * Sets the x value for the given index
   */
  fun x(index: Int, value: Double) {
    this.xValues[index] = value
  }

  /**
   * Returns the x value for the given index
   */
  fun x(index: Int): Double {
    return this.xValues[index]
  }

  /**
   * Sets the y value for the given index
   */
  fun y(index: Int, value: Double) {
    this.yValues[index] = value
  }

  /**
   * Returns the y value for the given index
   */
  fun y(index: Int): Double {
    return this.yValues[index]
  }

  /**
   * Iterates over all elements
   */
  inline fun fastForEachIndexed(
    action: (
      index: Int, x: @Window Double, y: @Window Double,
    ) -> Unit,
  ) {

    xValues.fastForEachIndexed { index, x ->
      val y = yValues[index]

      action(index, x, y)
    }
  }

  /**
   * Creates a new coordinates provider that uses the values from the cache
   */
  fun asCoordinatesProvider(): @Window CoordinatesProvider {
    return object : CoordinatesProvider {
      override fun size(): Int {
        return this@CoordinatesCache.size
      }

      override fun xAt(index: Int): Double {
        return this@CoordinatesCache.x(index)
      }

      override fun yAt(index: Int): Double {
        return this@CoordinatesCache.y(index)
      }
    }
  }

  fun asCoordinatesProvider1(): @Window CoordinatesProvider1<Any> {
    return asCoordinatesProvider().as1()
  }
}
