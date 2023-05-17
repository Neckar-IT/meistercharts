package com.meistercharts.algorithms.painter.stripe

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.layout.cache.LayoutVariable
import com.meistercharts.canvas.layout.cache.LayoutVariablesObjectCache
import com.meistercharts.history.DataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.MayBeNoValueOrPending
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Contains the painting variables for a single data series
 */
class StripePainterPaintingVariablesForOneDataSeries<DataSeriesIndexType : DataSeriesIndex, Value1Type, Value2Type, Value3Type, Value4Type>
  (
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
) : LayoutVariable {

  /**
   * The current history configuration
   */
  var historyConfiguration: HistoryConfiguration = HistoryConfiguration.empty

  /**
   * The height of the data series
   */
  var height: @Zoomed Double = Double.NaN

  /**
   * The data series index of the visible data series
   */
  var visibleDataSeriesIndex: DataSeriesIndexType = dataSeriesIndexDefault

  // Contains the previous values
  var previousValue1: Value1Type = value1Default
  var previousValue2: Value2Type = value2Default
  var previousValue3: Value3Type = value3Default
  var previousValue4: Value4Type = value4Default


  //Contains the current values - that will be painted, soon
  var currentValue1: Value1Type = value1Default
  var currentValue2: Value2Type = value2Default
  var currentValue3: Value3Type = value3Default
  var currentValue4: Value4Type = value4Default

  var currentStartX: @Window Double = Double.NaN
  var currentEndX: @Window Double = Double.NaN
  var currentStartTime: @Window Double = Double.NaN

  var currentEndTime: @Window Double = Double.NaN
  var activeTimeStamp: @ms @MayBeNaN Double = Double.NaN


  //Contains the next values - that are painted in the next segment
  var nextValue1: @MayBeNoValueOrPending Value1Type = value1Default
  var nextValue2: @MayBeNoValueOrPending Value2Type = value2Default
  var nextValue3: @MayBeNoValueOrPending Value3Type = value3Default
  var nextValue4: @MayBeNoValueOrPending Value4Type = value4Default

  var nextStartX: @Window Double = Double.NaN
  var nextEndX: @Window Double = Double.NaN
  var nextStartTime: @Window Double = Double.NaN
  var nextEndTime: @Window Double = Double.NaN


  /**
   * Contains the information about the segments.
   * Each segment might have a different length (and span multiple data points)
   */
  val segments: LayoutVariablesObjectCache<SegmentLayoutVariables<Value1Type, Value2Type, Value3Type, Value4Type>> = LayoutVariablesObjectCache {
    SegmentLayoutVariables(value1Default, value2Default, value3Default, value4Default)
  }

  /**
   * Is called *once* in each painting loop
   */
  fun prepareLayout(height: @Zoomed Double, historyConfiguration: HistoryConfiguration, dataSeriesIndex: DataSeriesIndexType) {
    this.height = height
    this.historyConfiguration = historyConfiguration
    this.visibleDataSeriesIndex = dataSeriesIndex
  }

  /**
   * Is called during the layout phase to store the next segment that will then be painted in the painting phase
   */
  fun storeSegment(startX: @Window Double, endX: @Window Double, activeTimeStamp: @ms @MayBeNaN Double, value1ToPaint: Value1Type, value2ToPaint: Value2Type, value3ToPaint: Value3Type, value4ToPaint: Value4Type) {
    val segmentLayoutVariable = segments.addNewElement()

    segmentLayoutVariable.startX = startX
    segmentLayoutVariable.endX = endX
    segmentLayoutVariable.activeTimeStamp = activeTimeStamp
    segmentLayoutVariable.value1ToPaint = value1ToPaint
    segmentLayoutVariable.value2ToPaint = value2ToPaint
    segmentLayoutVariable.value3ToPaint = value3ToPaint
    segmentLayoutVariable.value4ToPaint = value4ToPaint
  }

  override fun reset() {
    historyConfiguration = HistoryConfiguration.empty
    height = Double.NaN

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

    resetNextValues()
  }

  /**
   * Prepares for the next value
   */
  fun prepareForNextValue() {
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
    resetNextValues()
  }

  /**
   * Resets the next values to their defaults
   */
  private fun resetNextValues() {
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
   * Contains the variables for a single segment
   */
  class SegmentLayoutVariables<Value1Type, Value2Type, Value3Type, Value4Type>(
    val defaultValue1ToPaint: Value1Type,
    val defaultValue2ToPaint: Value2Type,
    val defaultValue3ToPaint: Value3Type,
    val defaultValue4ToPaint: Value4Type,

    ) : LayoutVariable {

    var startX: @Window Double = Double.NaN
    var endX: @Window Double = Double.NaN
    var activeTimeStamp: @ms @MayBeNaN Double = Double.NaN

    var value1ToPaint: Value1Type = defaultValue1ToPaint
    var value2ToPaint: Value2Type = defaultValue2ToPaint
    var value3ToPaint: Value3Type = defaultValue3ToPaint
    var value4ToPaint: Value4Type = defaultValue4ToPaint


    override fun reset() {
      startX = Double.NaN
      endX = Double.NaN
      activeTimeStamp = Double.NaN

      value1ToPaint = defaultValue1ToPaint
      value2ToPaint = defaultValue2ToPaint
      value3ToPaint = defaultValue3ToPaint
      value4ToPaint = defaultValue4ToPaint
    }
  }
}
