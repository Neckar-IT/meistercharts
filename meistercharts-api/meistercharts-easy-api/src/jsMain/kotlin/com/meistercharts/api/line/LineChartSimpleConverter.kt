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
package com.meistercharts.api.line

import com.meistercharts.algorithms.layers.linechart.LineStyle as ModelLineStyle
import com.meistercharts.api.LineStyle as JsLineStyle
import com.meistercharts.algorithms.painter.DirectLineLivePainter
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.algorithms.painter.SplineLinePainter
import it.neckar.open.charting.api.sanitizing.sanitize
import com.meistercharts.api.PointConnectionStyle
import com.meistercharts.api.PointConnectionType
import com.meistercharts.api.PointType
import com.meistercharts.api.toColor
import com.meistercharts.api.toModel
import com.meistercharts.painter.CategoryLinePainter
import com.meistercharts.painter.CategoryPointPainter
import com.meistercharts.painter.CircleCategoryPointPainter
import com.meistercharts.painter.Cross45DegreesCategoryPointPainter
import com.meistercharts.painter.CrossCategoryPointPainter
import com.meistercharts.painter.DotCategoryPointPainter
import com.meistercharts.painter.XyCategoryLinePainter
import com.meistercharts.painter.emptyCategoryLinePainter
import com.meistercharts.painter.emptyCategoryPointPainter
import it.neckar.open.kotlin.lang.getModuloOrNull
import it.neckar.open.provider.MultiProvider

/**
 * Converter for the line chart
 */
object LineChartSimpleConverter {
  fun <IndexContext> toPointPainters(jsLineStyles: Array<LineChartLineStyle?>): MultiProvider<IndexContext, CategoryPointPainter> {
    val categoryPointPainters = jsLineStyles.map { toCategoryPointPainter(it) }
    return MultiProvider.forListModulo(categoryPointPainters, emptyCategoryPointPainter)
  }

  private fun toCategoryPointPainter(jsLineStyle: LineChartLineStyle?): CategoryPointPainter {
    if (jsLineStyle == null) {
      return emptyCategoryPointPainter
    }

    val pointTypes = jsLineStyle.pointType?.map { it?.let { PointType.valueOf(it.toString()) } } ?: emptyList()
    val pointColors1 = jsLineStyle.pointColor1?.map { it?.let { it.toColor() } } ?: emptyList()
    val pointColors2 = jsLineStyle.pointColor2?.map { it?.let { it.toColor() } } ?: emptyList()
    val pointSizes = jsLineStyle.pointSize?.toList() ?: emptyList()
    val pointLineWidths = jsLineStyle.pointLineWidth?.toList() ?: emptyList()

    return CategoryPointPainter { gc, x, y, categoryIndex, seriesIndex, value ->
      val categoryPointPainter: CategoryPointPainter = when (pointTypes.getModuloOrNull(categoryIndex.value)) {
        PointType.None -> {
          emptyCategoryPointPainter
        }

        PointType.Dot -> {
          DotCategoryPointPainter(snapXValues = false, snapYValues = false).apply {
            pointColors1.getModuloOrNull(categoryIndex.value)?.let { pointStylePainter.color = it }
            pointSizes.getModuloOrNull(categoryIndex.value)?.let { pointStylePainter.pointSize = it }
            pointLineWidths.getModuloOrNull(categoryIndex.value)?.let { pointStylePainter.lineWidth = it }
          }
        }

        PointType.Cross   -> {
          CrossCategoryPointPainter(snapXValues = false, snapYValues = false).apply {
            pointColors1.getModuloOrNull(categoryIndex.value)?.let { pointStylePainter.color = it }
            pointSizes.getModuloOrNull(categoryIndex.value)?.let { pointStylePainter.pointSize = it }
            pointLineWidths.getModuloOrNull(categoryIndex.value)?.let { pointStylePainter.lineWidth = it }
          }
        }

        PointType.Cross45 -> {
          Cross45DegreesCategoryPointPainter(snapXValues = false, snapYValues = false).apply {
            pointColors1.getModuloOrNull(categoryIndex.value)?.let { pointStylePainter.color = it }
            pointSizes.getModuloOrNull(categoryIndex.value)?.let { pointStylePainter.pointSize = it }
            pointLineWidths.getModuloOrNull(categoryIndex.value)?.let { pointStylePainter.lineWidth = it }
          }
        }

        PointType.Circle -> {
          CircleCategoryPointPainter(snapXValues = false, snapYValues = false).apply {
            pointColors1.getModuloOrNull(categoryIndex.value)?.let { circlePointPainter.fill = it }
            pointColors2.getModuloOrNull(categoryIndex.value)?.let { circlePointPainter.stroke = it }
            pointSizes.getModuloOrNull(categoryIndex.value)?.let { circlePointPainter.pointSize = it }
          }
        }

        null -> {
          emptyCategoryPointPainter
        }
      }
      categoryPointPainter.paintPoint(gc, x, y, categoryIndex, seriesIndex, value)
    }
  }

  fun <IndexContext> toLinePainters(jsLineStyles: Array<LineChartLineStyle?>): MultiProvider<IndexContext, CategoryLinePainter> {
    val categoryLinePainters = jsLineStyles.map { toCategoryLinePainter(it?.lineStyle) }
    return MultiProvider.forListModulo(categoryLinePainters, emptyCategoryLinePainter)
  }

  private fun toCategoryLinePainter(jsLineStyle: JsLineStyle?): CategoryLinePainter {
    if (jsLineStyle == null) {
      return emptyCategoryLinePainter
    }
    if (jsLineStyle.type?.sanitize() == PointConnectionStyle.None) {
      return emptyCategoryLinePainter
    }
    return XyCategoryLinePainter(snapXValues = false, snapYValues = false)
  }

  fun <IndexContext> toLineStyles(jsLineStyles: Array<LineChartLineStyle?>): MultiProvider<IndexContext, ModelLineStyle> {
    val categoryLineStyles = jsLineStyles.map { toCategoryLineStyle(it?.lineStyle) }
    return MultiProvider.forListModulo(categoryLineStyles, ModelLineStyle())
  }

  private fun toCategoryLineStyle(jsLineStyle: JsLineStyle?): ModelLineStyle {
    if (jsLineStyle == null) {
      return ModelLineStyle.Continuous
    }
    return jsLineStyle.toModel()
  }

  fun <IndexContext> toLinePainters(oldLinePainters: MultiProvider<IndexContext, CategoryLinePainter>, jsLineStyles: Array<LineChartLineStyle?>): MultiProvider<IndexContext, CategoryLinePainter> {
    val linePainters = jsLineStyles.mapIndexed { index, jsLineStyle ->

      //Workaround to fix enum
      val pointConnectionType: PointConnectionType? = jsLineStyle?.pointConnectionType?.let {
        PointConnectionType.valueOf(it.toString())
      }

      when (pointConnectionType) {
        PointConnectionType.Direct -> XyCategoryLinePainter(false, false, DirectLinePainter(false, false))
        PointConnectionType.Spline -> XyCategoryLinePainter(false, false, SplineLinePainter(false, false))
        null -> oldLinePainters.valueAt(index)
      }
    }

    return MultiProvider.forListModulo(linePainters)
  }
}
