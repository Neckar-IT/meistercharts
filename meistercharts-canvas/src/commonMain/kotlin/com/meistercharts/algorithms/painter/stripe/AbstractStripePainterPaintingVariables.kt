package com.meistercharts.algorithms.painter.stripe

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.stripe.StripePainterPaintingVariables.SegmentLayoutVariables
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.PaintingLoopIndex
import com.meistercharts.canvas.layout.cache.LayoutVariableObjectCache
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.MayBeNoValueOrPending
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Abstract base class
 */
abstract class AbstractStripePainterPaintingVariables<DataSeriesIndexType : DataSeriesIndex, Value1Type, Value2Type, Value3Type, Value4Type>(
  /**
   * Defaults value for data series index - will be set initially and on reset
   */
  val dataSeriesIndexDefault: DataSeriesIndexType,
  /**
   * The default value for value1* - will be set initially and on reset
   */
  val value1Default: Value1Type,
  /**
   * The default value for value2* - will be set initially and on reset
   */
  val value2Default: Value2Type,

  /**
   * The default value for value3 - will be set initially and on reset
   */
  val value3Default: Value3Type,

  /**
   * The default value for value4 - will be set initially and on reset
   */
  val value4Default: Value4Type,

  ) : StripePainterPaintingVariables<DataSeriesIndexType, Value1Type, Value2Type, Value3Type, Value4Type> {

  override var historyConfiguration: HistoryConfiguration = HistoryConfiguration.empty
    protected set

  override var height: Double = Double.NaN
    protected set

  override var visibleDataSeriesIndex: DataSeriesIndexType = dataSeriesIndexDefault
    protected set

  override var currentValue1: Value1Type = value1Default
    protected set
  override var currentValue2: Value2Type = value2Default
    protected set
  override var currentValue3: Value3Type = value3Default
    protected set
  override var currentValue4: Value4Type = value4Default
    protected set

  override var previousValue1: Value1Type = value1Default
    protected set
  override var previousValue2: Value2Type = value2Default
    protected set
  override var previousValue3: Value3Type = value3Default
    protected set
  override var previousValue4: Value4Type = value4Default
    protected set

  override var currentStartX: @Window Double = Double.NaN
    protected set
  override var currentEndX: @Window Double = Double.NaN
  override var currentStartTime: @Window Double = Double.NaN
    protected set
  override var currentEndTime: @Window Double = Double.NaN
  override var activeTimeStamp: @ms @MayBeNaN Double = Double.NaN

  override var nextValue1: @MayBeNoValueOrPending Value1Type = value1Default
  override var nextValue2: @MayBeNoValueOrPending Value2Type = value2Default
  override var nextValue3: @MayBeNoValueOrPending Value3Type = value3Default
  override var nextValue4: @MayBeNoValueOrPending Value4Type = value4Default

  override var nextStartX: @Window Double = Double.NaN
  override var nextEndX: @Window Double = Double.NaN
  override var nextStartTime: @Window Double = Double.NaN
  override var nextEndTime: @Window Double = Double.NaN

  override var loopIndex: PaintingLoopIndex = PaintingLoopIndex.Unknown
    protected set

  override fun prepareLayout(paintingContext: LayerPaintingContext, height: @Zoomed Double, dataSeriesIndex: DataSeriesIndexType, historyConfiguration: HistoryConfiguration) {
    reset()
    loopIndex = paintingContext.loopIndex

    this.height = height
    this.visibleDataSeriesIndex = dataSeriesIndex
    this.historyConfiguration = historyConfiguration
  }

  /**
   * Prepares for the next value
   */
  override fun prepareForNextValue() {
    //Save current to previous
    previousValue1 = currentValue1
    previousValue2 = currentValue2
    previousValue3 = currentValue3
    previousValue4 = currentValue4

    //Save next to current
    currentValue1 = nextValue1
    currentValue2 = nextValue2
    currentValue3 = nextValue3
    currentValue4 = nextValue4

    currentStartX = nextStartX
    currentEndX = nextEndX
    currentStartTime = nextStartTime
    currentEndTime = nextEndTime

    //Reset next
    resetNext()
  }

  /**
   * Resets the next values to their defaults
   */
  fun resetNext() {
    //reset the next
    nextValue1 = value1Default
    nextValue2 = value2Default
    nextValue3 = value3Default
    nextValue4 = value4Default

    nextStartX = Double.NaN
    nextEndX = Double.NaN
    nextStartTime = Double.NaN
    nextEndTime = Double.NaN
  }

  /**
   * Contains the information about the segments
   */
  override val segments: LayoutVariableObjectCache<SegmentLayoutVariables<Value1Type, Value2Type, Value3Type, Value4Type>> = LayoutVariableObjectCache {
    SegmentLayoutVariables(value1Default, value2Default, value3Default, value4Default)
  }

  override fun storeSegment(
    startX: @Window Double,
    endX: @Window Double,
    activeTimeStamp: @ms @MayBeNaN Double,
    value1ToPaint: Value1Type,
    value2ToPaint: Value2Type,
    value3ToPaint: Value3Type,
    value4ToPaint: Value4Type,
  ) {
    val segmentLayoutVariable = segments.addNewElement()

    segmentLayoutVariable.startX = startX
    segmentLayoutVariable.endX = endX
    segmentLayoutVariable.activeTimeStamp = activeTimeStamp
    segmentLayoutVariable.value1ToPaint = value1ToPaint
    segmentLayoutVariable.value2ToPaint = value2ToPaint
    segmentLayoutVariable.value3ToPaint = value3ToPaint
    segmentLayoutVariable.value4ToPaint = value4ToPaint
  }

  /**
   * Reset all values
   */
  fun reset() {
    loopIndex = PaintingLoopIndex.Unknown
    segments.clear() //reset the size to 0, will be added if necessary

    visibleDataSeriesIndex = dataSeriesIndexDefault

    currentValue1 = value1Default
    currentValue2 = value2Default
    currentValue3 = value3Default
    currentValue4 = value4Default

    currentStartX = Double.NaN
    currentEndX = Double.NaN
    currentStartTime = Double.NaN
    currentEndTime = Double.NaN

    previousValue1 = value1Default
    previousValue2 = value2Default
    previousValue3 = value3Default
    previousValue4 = value4Default

    activeTimeStamp = Double.NaN

    resetNext()
  }
}
