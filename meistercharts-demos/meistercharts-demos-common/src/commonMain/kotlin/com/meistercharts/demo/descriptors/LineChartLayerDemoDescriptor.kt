/*
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
package com.meistercharts.demo.descriptors


import kotlin.uuid.Uuid
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.layers.linechart.LinesChartModel
import com.meistercharts.algorithms.layers.linechart.PointStyle
import com.meistercharts.algorithms.layers.linechart.SimpleLineModel
import com.meistercharts.algorithms.layers.linechart.SimpleLinesChartModel
import com.meistercharts.algorithms.layers.linechart.addLineChart
import com.meistercharts.color.Color
import com.meistercharts.algorithms.painter.DirectLinePainter
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.FixedContentAreaWidth
import com.meistercharts.canvas.LineJoin
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section
import com.meistercharts.painter.CirclePointPainter
import com.meistercharts.painter.FancyPointPainter
import com.meistercharts.painter.PointPainter
import com.meistercharts.painter.PointStylePainter
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.random
import it.neckar.open.provider.MultiProvider
import kotlin.math.max
import kotlin.math.min

/**
 */
class LineChartLayerDemoDescriptor : MeisterchartsDemoDescriptor<Int> {
  override val uuid: Uuid = Uuid.parse("b3820e9a-1402-4f4b-b563-5c3b9171bf42")
  override val name: String = "Line chart"
  override val description: String = "A line chart layer"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<Int>> = listOf(
    PredefinedConfiguration(Uuid.parse("70365b1a-4cbe-4a65-9943-375bfd06cc68"), 1, "1 line"),
    PredefinedConfiguration(Uuid.parse("9f5c5420-4f2b-4689-819f-9ccb97458b5a"), 4, "4 lines"),
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<Int>?): MeisterchartsDemo {
    require(configuration != null) { "LineChartLayerDemoDescriptor.prepareDemo requires a predefined configuration" }

    val linesCount = configuration.payload
    require(linesCount > 0) { "linesCount must be greater than 0 but was $linesCount" }

    val model = lineChartModel(linesCount)

    return MeisterchartsDemo(this) {
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

          lineChartLayer.configuration.linePainters = MultiProvider.always(DirectLinePainter(false, snapYValues = false))
          lineChartLayer.configuration.lineStyles = MultiProvider.forListModulo(lineStyles)

          val pointPainter0 = PointStylePainter(PointStyle.Dot, 5.0, false, false)
          val pointPainter1: PointPainter = CirclePointPainter(false, false).also {
            it.fill = Color.lightblue
            it.stroke = Color.red
            it.lineWidth = 2.0
          }
          val pointPainter2: PointPainter = FancyPointPainter(false, false)

          lineChartLayer.configuration.pointPainters = MultiProvider.forListModulo(listOf(pointPainter0, pointPainter1, pointPainter2))


          section("Line Style 0")
          configurableLine(lineStyles, 0)

          section("Line Style 1")
          configurableLine(lineStyles, 1)

          section("Line Style 2")
          configurableLine(lineStyles, 2)


          //configurableEnum("Point style", configurableTrace.pointStyle, enumEntries()) {
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

  private fun MeisterchartsDemo.configurableLine(lineStyles: MutableList<LineStyle>, lineIndex: Int) {
    configurableColorPicker("Color", lineStyles[lineIndex].color()) {
      onChange {
        lineStyles[lineIndex] = lineStyles[lineIndex].copy(color = it.asProvider())
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
