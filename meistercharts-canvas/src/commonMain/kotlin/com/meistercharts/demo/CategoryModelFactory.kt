package com.meistercharts.demo

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.model.Category
import com.meistercharts.algorithms.model.CategorySeriesModel
import com.meistercharts.algorithms.model.DefaultCategorySeriesModel
import com.meistercharts.algorithms.model.Series
import com.meistercharts.animation.Easing
import com.meistercharts.history.generator.repeatingValues
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextService
import it.neckar.open.i18n.resolve

/**
 * Creates a [CategorySeriesModel] and related model information
 */
interface CategoryModelFactory {
  /**
   * Creates the [CategorySeriesModel] itself
   */
  fun createModel(): CategorySeriesModel

  /**
   * The value range to be used for the value axis
   */
  fun createValueRange(): ValueRange
}

/**
 * A [CategoryModelFactory] for cylinder pressure
 */
object CylinderPressureCategoryModelFactory : CategoryModelFactory {
  override fun createModel(): CategorySeriesModel {
    return DefaultCategorySeriesModel(
      listOf(
        Category(TextKey.simple("Cylinder 1")),
        Category(TextKey.simple("Cylinder 2")),
        Category(TextKey.simple("Cylinder 3")),
        Category(TextKey.simple("Cylinder 4")),
      ),
      listOf(
        object : Series {
          val name: TextKey = TextKey.simple("Pressure 1")
          override fun size(): Int = 4
          override fun valueAt(index: Int): Double {
            return when (index) {
              0 -> Easing.linear
              1 -> Easing.sin
              2 -> Easing.smooth
              3 -> Easing.out
              else -> Easing.inOut
            }.let {
              repeatingValues(it, 2_000.0, 2500.0 * index, 0.5) * 100.0
            }
          }

          override fun seriesName(textService: TextService, i18nConfiguration: I18nConfiguration): String {
            return name.resolve(textService, i18nConfiguration)
          }
        },
        object : Series {
          val name: TextKey = TextKey.simple("Pressure 2")
          override fun size(): Int = 4
          override fun valueAt(index: Int): Double {
            return when (index) {
              0 -> Easing.outInBack
              1 -> Easing.inBounce
              2 -> Easing.outBounce
              3 -> Easing.inOutBounce
              else -> Easing.outInBounce
            }.let {
              repeatingValues(it, 3_000.0, 3333.0 * index, 0.75) * 100.0
            }
          }

          override fun seriesName(textService: TextService, i18nConfiguration: I18nConfiguration): String {
            return name.resolve(textService, i18nConfiguration)
          }
        },
        object : Series {
          val name: TextKey = TextKey.simple("Pressure 3")
          override fun size(): Int = 4
          override fun valueAt(index: Int): Double {
            return when (index) {
              0 -> Easing.inQuad
              1 -> Easing.outQuad
              2 -> Easing.inOutQuad
              3 -> Easing.outIn
              else -> Easing.linear
            }.let {
              repeatingValues(it, 4_000.0, 1111.0 * index, 0.6) * 100.0
            }
          }

          override fun seriesName(textService: TextService, i18nConfiguration: I18nConfiguration): String {
            return name.resolve(textService, i18nConfiguration)
          }
        },
      )
    )
  }

  override fun createValueRange(): ValueRange {
    return ValueRange.linear(0.0, 140.0)
  }
}
