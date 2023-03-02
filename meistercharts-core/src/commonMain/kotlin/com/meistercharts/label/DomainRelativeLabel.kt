package com.meistercharts.label

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.DomainRelative
import it.neckar.open.unit.other.pct

/**
 * Represents a label with domain value
 */
@Deprecated("No longer used")
data class DomainRelativeLabel(
  /**
   * The domain relative value
   */
  @pct
  @DomainRelative val value: Double,

  /**
   * The data for the label
   */
  val labelData: LabelData
) {

  fun withColor(newColor: Color): DomainRelativeLabel {
    return DomainRelativeLabel(value, labelData.withColor(newColor))
  }
}
