package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.model.CategoryIndex
import com.meistercharts.charts.CategoryLineChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableList
import com.meistercharts.painter.CategoryLinePainter
import com.meistercharts.painter.CircleCategoryPointPainter
import com.meistercharts.painter.Cross45DegreesCategoryPointPainter
import com.meistercharts.painter.CrossCategoryPointPainter
import com.meistercharts.painter.DotCategoryPointPainter
import com.meistercharts.painter.XyCategoryLinePainter
import com.meistercharts.painter.emptyCategoryLinePainter
import com.meistercharts.painter.emptyCategoryPointPainter
import it.neckar.open.provider.MultiProvider
import com.meistercharts.style.BoxStyle

/**
 * Demonstrates category lines and points
 */
class CategoryLineChartGestaltWithLinesAndPointsDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Category Line Chart: lines and points"
  override val category: DemoCategory = DemoCategory.Gestalt

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      val gestalt = CategoryLineChartGestalt()

      meistercharts {
        gestalt.configure(this)

        val xyLinePainter = XyCategoryLinePainter(snapXValues = false, snapYValues = false)
        var lineStyle = LineStyle()
        var boxStyle = BoxStyle.gray
        var linePainter: CategoryLinePainter = xyLinePainter

        gestalt.categoryLinesLayer.style.linePainters = MultiProvider { linePainter }

        gestalt.categoryLinesLayer.style.lineStyles = MultiProvider { lineStyle }
        gestalt.crossWireLabelsLayer.style.valueLabelBoxStyle = MultiProvider { boxStyle }

        configure {

          configurableList(
            "Cross wire category", CategoryIndex(0), listOf(
              CategoryIndex(0),
              CategoryIndex(1),
              CategoryIndex(2),
              CategoryIndex(3),
              null,
            )
          ) {

            converter {
              it?.value?.toString() ?: "-"
            }

            onChange {
              gestalt.categoryLinesLayer.style.activeCategoryIndex = it
              markAsDirty()
            }
          }

          configurableList("Line painter", "XY", listOf("XY", "empty")) {
            onChange {
              linePainter = if (it == "empty") {
                emptyCategoryLinePainter
              } else {
                xyLinePainter
              }
              markAsDirty()
            }
          }

          configurableColorPicker("Line color", lineStyle.color) {
            onChange {
              lineStyle = lineStyle.copy(color = it)
              boxStyle = boxStyle.copy(fill = it)
              markAsDirty()
            }
          }

          configurableDouble("Line width", lineStyle.lineWidth) {
            min = 0.0
            max = 20.0
            onChange {
              lineStyle = lineStyle.copy(lineWidth = it)
              markAsDirty()
            }
          }

          configurableList("Dashes", lineStyle.dashes, Dashes.predefined) {
            onChange {
              lineStyle = lineStyle.copy(dashes = it)
              markAsDirty()
            }
          }

          val dotCategoryPointPainter = DotCategoryPointPainter(snapXValues = false, snapYValues = false)
          val crossCategoryPointPainter = CrossCategoryPointPainter(snapXValues = false, snapYValues = false)
          val cross45CategoryPointPainter = Cross45DegreesCategoryPointPainter(snapXValues = false, snapYValues = false)
          val circleCategoryPointPainter = CircleCategoryPointPainter(snapXValues = false, snapYValues = false)

          val categoryPointPainters = listOf(emptyCategoryPointPainter, dotCategoryPointPainter, circleCategoryPointPainter, crossCategoryPointPainter, cross45CategoryPointPainter)

          configurableList("Point painter", dotCategoryPointPainter, categoryPointPainters) {
            converter {
              when (it) {
                is DotCategoryPointPainter -> "Dot"
                is CircleCategoryPointPainter -> "Circle"
                is CrossCategoryPointPainter -> "Cross"
                is Cross45DegreesCategoryPointPainter -> "Cross45"
                else -> "None"
              }
            }

            onChange {
              gestalt.categoryLinesLayer.style.pointPainters = MultiProvider.always(it)
              markAsDirty()
            }
          }

          configurableDouble("Point size", circleCategoryPointPainter.circlePointPainter.pointSize) {
            min = 1.0
            max = 50.0
            onChange {
              circleCategoryPointPainter.circlePointPainter.pointSize = it
              dotCategoryPointPainter.pointStylePainter.pointSize = it
              crossCategoryPointPainter.pointStylePainter.pointSize = it
              cross45CategoryPointPainter.pointStylePainter.pointSize = it
              markAsDirty()
            }
          }

          configurableDouble("Point line width", circleCategoryPointPainter.circlePointPainter.lineWidth) {
            min = 0.1
            max = 10.0
            onChange {
              circleCategoryPointPainter.circlePointPainter.lineWidth = it
              dotCategoryPointPainter.pointStylePainter.lineWidth = it
              crossCategoryPointPainter.pointStylePainter.lineWidth = it
              cross45CategoryPointPainter.pointStylePainter.lineWidth = it
              markAsDirty()
            }
          }

          configurableColorPicker("Point fill", circleCategoryPointPainter.circlePointPainter.fill) {
            onChange {
              circleCategoryPointPainter.circlePointPainter.fill = it
              dotCategoryPointPainter.pointStylePainter.color = it
              crossCategoryPointPainter.pointStylePainter.color = it
              cross45CategoryPointPainter.pointStylePainter.color = it
              markAsDirty()
            }
          }

          configurableColorPicker("Point stroke", circleCategoryPointPainter.circlePointPainter::stroke) {
          }

        }
      }
    }
  }
}
