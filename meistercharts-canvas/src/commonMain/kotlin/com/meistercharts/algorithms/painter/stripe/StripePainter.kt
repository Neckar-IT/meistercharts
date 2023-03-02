package com.meistercharts.algorithms.painter.stripe

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.MayBeNoValueOrPending

/**
 * Base interface for stripe painters.
 * In most cases, (at most) two values are used for the stripes.
 * Therefore, this class takes two value arguments.
 *
 * If a fourth one is (really) needed, the [valueChange] method should be extended
 *
 *
 * @param DataSeriesIndexType: Type of: the data series index
 * @param ValueType1: Type of: the first relevant value
 * @param ValueType2: Type of: the second relevant value
 * @param ValueType3: Type of: the third relevant value
 */
interface StripePainter<DataSeriesIndexType : DataSeriesIndex, ValueType1, ValueType2, ValueType3> {
  /**
   * Begins a new set of stripe segments
   */
  fun begin(
    paintingContext: LayerPaintingContext,
    /**
     * The height of the stripe
     */
    height: @Zoomed Double,
    /**
     * The data series index the stripe belongs to
     */
    dataSeriesIndex: DataSeriesIndexType,
    /**
     * The history enum that is started to be painted
     */
    historyConfiguration: HistoryConfiguration,
  )

  /**
   * Adds a value change event at the given x location
   *
   * Call [finish] when done.
   */
  fun valueChange(
    paintingContext: LayerPaintingContext,
    /**
     * The start location of the stripe segment
     */
    startX: @Window Double,

    /**
     * The end location of the stripe segment
     */
    endX: @Window Double,

    /**
     * The updated value
     */
    newValue1: ValueType1,

    /**
     * The second updated value (usually some kind of merged type - e.g. "most of the time")
     */
    newValue2: ValueType2,

    /**
     * The third updated value (usually some kind of context information - e.g. a map with additional information required to paint)
     */
    newValue3: ValueType3,
  )

  /**
   * Finished the enum bar - up until the given x value
   */
  fun finish(paintingContext: LayerPaintingContext)
}
