package com.meistercharts.algorithms.model

import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

/**
 * A model that organizes stuff in categories.
 */
interface CategoryModel {
  /**
   * The number of [Category] instances
   */
  val numberOfCategories: Int

  /**
   * Returns the name of the category at the given index
   */
  fun categoryNameAt(categoryIndex: CategoryIndex, textService: TextService, i18nConfiguration: I18nConfiguration): String
}
