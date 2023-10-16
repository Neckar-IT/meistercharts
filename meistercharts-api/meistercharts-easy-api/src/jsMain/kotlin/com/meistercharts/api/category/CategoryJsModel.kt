/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.api.category

import com.meistercharts.annotations.Domain


/**
 * Contains all external interfaces that represent the API towards the browser.
 * There is no guaranty that we will receive non-null and well-defined values. Hence, all types are nullable.
 */

/**
 * Hols data for category / series model
 */
@JsExport
external interface CategoriesSeriesData {
  /**
   * The categories to be shown.
   */
  val categories: Array<CategorySeriesData?>?
}

/**
 * Base interface for category related data
 */
@JsExport
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
@JsExport
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
@JsExport
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
