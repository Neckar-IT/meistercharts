package com.meistercharts.algorithms.painter.stripe

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.MayBeNoValueOrPending

/**
 * Abstract base class for stripe painters.
 *
 * A stripe painter paints a (horizontal) stripe with several stripe segments.
 * A segment might span multiple "value segments". This allows optimized painting of a single area for multiple recordings of the same value.
 */
abstract class AbstractStripePainter<DataSeriesIndexType : DataSeriesIndex, Value1Type, Value2Type, Value3Type> : StripePainter<DataSeriesIndexType, Value1Type, Value2Type, Value3Type> {
  /**
   * Returns the painting variables for this stripe painter
   */
  abstract fun paintingVariables(): StripePainterPaintingVariables<DataSeriesIndexType, Value1Type, Value2Type, Value3Type>

  override fun begin(paintingContext: LayerPaintingContext, height: @Zoomed Double, dataSeriesIndex: DataSeriesIndexType, historyConfiguration: HistoryConfiguration) {
    val paintingVariables = paintingVariables()

    paintingVariables.calculate(height, dataSeriesIndex, historyConfiguration)
  }

  override fun valueChange(paintingContext: LayerPaintingContext, startX: @Window Double, endX: @Window Double, newValue1: Value1Type, newValue2: Value2Type, newValue3: Value3Type) {
    val paintingVariables = paintingVariables()

    if (relevantValuesHaveChanged(newValue1, newValue2, newValue3).not()) {
      //Values have not changed, just update the end - but do not paint
      paintingVariables.currentEndX = endX
      return
    }

    relevantValuesChanged(paintingVariables, newValue1, newValue2, newValue3, startX, endX)

    paintSegment(paintingContext)
  }

  /**
   * Is called if the relevant values have changed
   */
  protected open fun relevantValuesChanged(
    paintingVariables: StripePainterPaintingVariables<DataSeriesIndexType, Value1Type, Value2Type, Value3Type>,
    newValue1: Value1Type,
    newValue2: Value2Type,
    newValue3: Value3Type,
    startX: @Window Double,
    endX: @Window Double,
  ) {
    //Remember the updated properties - for the next paint
    paintingVariables.nextValue1 = newValue1
    paintingVariables.nextValue2 = newValue2
    paintingVariables.nextValue3 = newValue3

    paintingVariables.nextStartX = startX
    paintingVariables.nextEndX = endX
    //Paint the *current* value until the next start
    paintingVariables.currentEndX = startX
  }

  /**
   * Paints the current segment
   */
  fun paintSegment(paintingContext: LayerPaintingContext) {
    val paintingVariables = paintingVariables()

    @MayBeNoValueOrPending val value1ToPaint = paintingVariables.currentValue1
    @MayBeNoValueOrPending val value2ToPaint = paintingVariables.currentValue2
    @MayBeNoValueOrPending val value3ToPaint = paintingVariables.currentValue3

    @Window val startX = paintingVariables.currentStartX
    @Window val endX = paintingVariables.currentEndX

    try {
      paintSegment(paintingContext, startX, endX, value1ToPaint, value2ToPaint, value3ToPaint)
    } finally {
      //Switch to *next*
      paintingVariables.prepareForNextValue()
    }
  }

  /**
   * This method is only called if it is necessary to paint (usually because the value has changed).
   *
   * The [paintingVariables] can be used to fetch additional values (if necessary).
   */
  abstract fun paintSegment(
    paintingContext: LayerPaintingContext,
    startX: @Window Double,
    endX: @Window Double,
    value1ToPaint: Value1Type,
    value2ToPaint: Value2Type,
    value3ToPaint: Value3Type,
  )

  override fun finish(paintingContext: LayerPaintingContext) {
    //Paint the last value
    paintSegment(paintingContext)
  }

  /**
   * Returns true if the *relevant* values have changed.
   */
  abstract fun relevantValuesHaveChanged(
    value1: Value1Type,
    value2: Value2Type,
    value3: Value3Type,
  ): Boolean
}
