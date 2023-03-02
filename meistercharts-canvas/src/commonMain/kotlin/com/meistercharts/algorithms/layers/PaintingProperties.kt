package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.paintingProperties
import com.meistercharts.history.SamplingPeriod
import it.neckar.open.unit.si.ms

/**
 * Contains calculators that provide results for properties - the properties are recalculated for *each* render.
 *
 * Attention: This object contains the values for the *last* paint.
 * All values are deleted at the start of each render call (in [com.meistercharts.canvas.DefaultLayerSupport]).
 *
 * Therefore, it is possible to retrieve the values of the last paint *after* the render.
 */
class PaintingProperties {
  private val entries = mutableMapOf<PaintingPropertyKey<*>, Any>()

  /**
   * Stores the given value
   */
  fun <V : Any> store(key: PaintingPropertyKey<V>, value: V) {
    val oldValue = entries.put(key, value)
    if (oldValue != null) {
      throw IllegalStateException("A value for <${key.key}> has already been set: <$oldValue>")
    }
  }

  /**
   * Retrieves the stored value unter the given key
   */
  fun <V> retrieve(key: PaintingPropertyKey<V>): V {
    return (entries[key] ?: throw NoSuchElementException("No entry found for <${key.key}>. ${key.howToFixDescription}")) as V
  }

  /**
   * Retrieves the value - or returns null, if no value has been set
   */
  fun <V> retrieveOrNull(key: PaintingPropertyKey<V>): V? {
    return (entries[key] ?: return null) as V
  }

  /**
   * Must be called before each paint to ensure the properties are always up to date
   */
  fun clear() {
    entries.clear()
  }
}

/**
 * Describes the key for a painting property
 */
@Suppress("DataClassPrivateConstructor")
data class PaintingPropertyKey<V> private constructor(
  val key: String,

  /**
   * Contains a description how this value can/should be set - if not available.
   * This is a property that is only useful during development
   */
  val howToFixDescription: String,
) {
  companion object {
    /**
     * The sampling period that is used for the history layer
     */
    val SamplingPeriod: PaintingPropertyKey<SamplingPeriod> = PaintingPropertyKey("history.render.SamplingPeriod", "Register a HistoryRenderPropertiesCalculatorLayer")

    /**
     * Returns the visible time range (in the window)
     */
    val VisibleTimeRangeX: PaintingPropertyKey<TimeRange> = PaintingPropertyKey("history.render.VisibleTimeRangeX", "Register a HistoryRenderPropertiesCalculatorLayer")

    /**
     * The minimum distance between two data points that is interpreted as gap
     */
    val MinGapDistance: PaintingPropertyKey<@ms Double> = PaintingPropertyKey("history.render.MinGapDistance", "Register a HistoryRenderPropertiesCalculatorLayer")
  }
}

inline fun <V> PaintingPropertyKey<V>.retrieve(layerPaintingContext: LayerPaintingContext): V {
  return retrieve(layerPaintingContext.chartSupport)
}

inline fun <V> PaintingPropertyKey<V>.retrieve(chartSupport: ChartSupport): V {
  return retrieve(chartSupport.paintingProperties)
}

inline fun <V> PaintingPropertyKey<V>.retrieve(paintingProperties: PaintingProperties): V {
  return paintingProperties.retrieve(this)
}
