package com.meistercharts.api.category

import com.meistercharts.annotations.Domain


/**
 * Contains all external interfaces that represent the API towards the browser.
 * There is no guaranty that we will receive non-null and well-defined values. Hence, all types are nullable.
 */

/**
 * Hols data for category / series model
 */
external interface CategoriesSeriesData {
  /**
   * The categories to be shown.
   */
  val categories: Array<CategorySeriesData?>?
}

/**
 * Base interface for category related data
 */
external interface BaseCategoryData {
  /**
   * The label of the category
   */
  val label: String?

  /**
   * The image of the category
   */
  val image: String?
}

/**
 * Contains the series data for a single category
 */
external interface CategorySeriesData : BaseCategoryData {
  /**
   * The values that belong to the category
   */
  @Suppress("ArrayPrimitive")
  val values: Array<Double>?
}

/**
 * Contains the bullet chart data for a single category
 */
external interface CategoryBulletChartData : BaseCategoryData {
  /**
   * The values that belong to the category
   */
  val current: @Domain Double

  /**
   * The start of the bar (lower domain value)
   */
  val barStart: @Domain Double

  /**
   * The end of the bar (higher domain value)
   */
  val barEnd: @Domain Double
}
