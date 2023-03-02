package com.meistercharts.charts.bullet

import com.meistercharts.algorithms.LinearValueRange
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.algorithms.model.CategoryModel
import com.meistercharts.annotations.Domain

/**
 * Category model for bullet chart
 */
interface CategoryModelBulletChart : CategoryModel {
  /**
   * Returns the current value for the given category index
   */
  fun currentValue(categoryIndex: CategoryIndex): @Domain Double

  /**
   * Returns the value range
   */
  fun barRange(categoryIndex: CategoryIndex): @Domain LinearValueRange?
}
