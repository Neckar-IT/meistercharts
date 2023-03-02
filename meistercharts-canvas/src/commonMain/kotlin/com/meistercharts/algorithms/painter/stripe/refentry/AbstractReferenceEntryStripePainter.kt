package com.meistercharts.algorithms.painter.stripe.refentry

import com.meistercharts.algorithms.painter.stripe.AbstractStripePainter
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId

/**
 * Abstract base class for referenceEntry stripe painters
 */
abstract class AbstractReferenceEntryStripePainter : AbstractStripePainter<ReferenceEntryDataSeriesIndex, ReferenceEntryId, ReferenceEntryDifferentIdsCount, ReferenceEntryData?>(), ReferenceEntryStripePainter {
  /**
   * Provides the current aggregation mode
   */
  abstract val configuration: Configuration

  override fun paintingVariables(): ReferenceEntryStripePainterPaintingVariables {
    return paintingVariables
  }

  /**
   * The painting properties that are held
   */
  private val paintingVariables = DefaultReferenceEntryStripePainterPaintingVariables()


  override fun relevantValuesHaveChanged(value1: ReferenceEntryId, value2: ReferenceEntryDifferentIdsCount, value3: ReferenceEntryData?): Boolean {
    //Ignore value3: ReferenceEntriesDataMap - these values must never change for the same [ReferenceEntryId]

    val currentId = paintingVariables.currentValue1
    val currentCount = paintingVariables.currentValue2

    //TODO only check one of the two?
    return currentId != value1 || currentCount != value2
  }

  open class Configuration {
  }
}
