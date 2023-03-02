package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.linechart.LinesChartModel
import com.meistercharts.algorithms.layers.linechart.PointStyle
import com.meistercharts.algorithms.layers.linechart.SimpleLineModel
import com.meistercharts.algorithms.layers.linechart.SimpleLinesChartModel
import com.meistercharts.algorithms.layers.linechart.addLineChart
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.FixedContentAreaWidth
import com.meistercharts.canvas.LineJoin
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section
import com.meistercharts.painter.CirclePointPainter
import com.meistercharts.painter.FancyPointPainter
import com.meistercharts.painter.PointPainter
import com.meistercharts.painter.PointStylePainter
import it.neckar.open.kotlin.lang.random
import it.neckar.open.provider.MultiProvider
import kotlin.math.max
import kotlin.math.min

/**
 */
class LineChartLayerDemoDescriptor : ChartingDemoDescriptor<Int> {
  override val name: String = "Line chart"
  override val description: String = "## Line Chart Layer"
  override val category: DemoCategory = DemoCategory.Layers

  override val predefinedConfigurations: List<PredefinedConfiguration<Int>> = listOf(
    PredefinedConfiguration(1, "1 line"),
    PredefinedConfiguration(4, "4 lines"),
  )

  override fun createDemo(configuration: PredefinedConfiguration<Int>?): ChartingDemo {
    require(configuration != null)

    val linesCount = configuration.payload
    require(linesCount > 0) { "linesCount must be greater than 0 but was $linesCount" }

    val model = lineChartModel(linesCount)

    return ChartingDemo {
      meistercharts {
        zoomAndTranslationDefaults(ZoomAndTranslationDefaults.tenPercentMargin)
        contentAreaSizingStrategy = FixedContentAreaWidth(1000.0)

        configure {
          // resizeBehavior = KeepOriginOnResize // FIXME -> translates to -infinity/-infinity

          layers.addClearBackground()
          val lineChartLayer = layers.addLineChart(model)

          //The line style that is configured
          val lineStyles = mutableListOf(
            LineStyle(Color.orange, 2.0, Dashes.SmallDashes, LineJoin.Round),
            LineStyle(Color.blue, 3.0, Dashes.LargeDashes, LineJoin.Bevel),
            LineStyle(Color.green, 1.5, null, LineJoin.Miter)
          )

          lineChartLayer.style.linePainters = MultiProvider.always(DirectLinePainter(false, snapYValues = false))
          lineChartLayer.style.lineStyles = MultiProvider.forListModulo(lineStyles)

          val pointPainter0 = PointStylePainter(PointStyle.Dot, 5.0, false, false)
          val pointPainter1: PointPainter = CirclePointPainter(false, false).also {
            it.fill = Color.lightblue
            it.stroke = Color.red
            it.lineWidth = 2.0
          }
          val pointPainter2: PointPainter = FancyPointPainter(false, false)

          lineChartLayer.style.pointPainters = MultiProvider.forListModulo(listOf(pointPainter0, pointPainter1, pointPainter2))


          section("Line Style 0")
          configurableLine(lineStyles, 0)

          section("Line Style 1")
          configurableLine(lineStyles, 1)

          section("Line Style 2")
          configurableLine(lineStyles, 2)


          //configurableEnum("Point style", configurableTrace.pointStyle, PointStyle.values()) {
          //  onChange {
          //    configurableTrace.pointStyle = it
          //    markAsDirty()
          //  }
          //}
          //

        }
      }
    }
  }

  private fun ChartingDemo.configurableLine(lineStyles: MutableList<LineStyle>, lineIndex: Int) {
    configurableColorPicker("Color", lineStyles[lineIndex].color) {
      onChange {
        lineStyles[lineIndex] = lineStyles[lineIndex].copy(color = it)
        markAsDirty()
      }
    }

    configurableDouble("Line width", lineStyles[lineIndex].lineWidth) {
      min = 0.0
      max = 50.0
      onChange {
        lineStyles[lineIndex] = lineStyles[lineIndex].copy(lineWidth = it)
        markAsDirty()
      }
    }

    configurableList("Dashes", lineStyles[lineIndex].dashes, Dashes.predefined) {
      onChange {
        lineStyles[lineIndex] = lineStyles[lineIndex].copy(dashes = it)
        markAsDirty()
      }
    }
  }

  private fun lineChartModel(linesCount: Int): LinesChartModel {
    val dataPointsCount = 100

    val lineModels = mutableListOf<SimpleLineModel>()

    for (linesIndex in 0 until linesCount) {
      @DomainRelative val xValues = DoubleArray(dataPointsCount)
      @DomainRelative val yValues = DoubleArray(dataPointsCount)

      @DomainRelative val rand1 = random.nextDouble()
      @DomainRelative val rand2 = random.nextDouble()
      @DomainRelative val minY = min(rand1, rand2)
      @DomainRelative val maxY = max(rand1, rand2)

      for (dataPointIndex in 0 until dataPointsCount) {
        xValues[dataPointIndex] = dataPointIndex * 0.01
        yValues[dataPointIndex] = random.nextDouble(minY, maxY)
      }

      val lineModel = SimpleLineModel(xValues, yValues)
      lineModels.add(lineModel)
    }

    return SimpleLinesChartModel(lineModels)
  }
}
