package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.layers.AxisPaintingVariables
import com.meistercharts.algorithms.layers.AxisPaintingVariablesImpl
import com.meistercharts.algorithms.layout.EquisizedBoxLayout

interface CategoryAxisPaintingVariables : AxisPaintingVariables {
  /**
   * The layout that is used
   */
  var categoryLayout: EquisizedBoxLayout
}


abstract class CategoryAxisPaintingVariablesImpl : CategoryAxisPaintingVariables, AxisPaintingVariablesImpl() {

  /**
   * The layout
   */
  override var categoryLayout: EquisizedBoxLayout = EquisizedBoxLayout.empty

  /**
   * Resets all variables to their default values
   */
  override fun reset() {
    super.reset()

    categoryLayout = EquisizedBoxLayout.empty
  }

}
