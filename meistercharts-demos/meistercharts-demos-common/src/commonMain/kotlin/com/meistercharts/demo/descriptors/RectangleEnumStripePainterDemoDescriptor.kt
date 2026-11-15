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
@file:Suppress("RETURN_VALUE_NOT_USED") // Demo-scope subscriptions tied to the demo lifetime

package com.meistercharts.demo.descriptors

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.resize.ResetToDefaultsOnWindowResize
import com.meistercharts.zoom.FittingWithMargin
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.ContentAreaLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.stripe.enums.EnumAggregationMode
import com.meistercharts.algorithms.painter.stripe.enums.RectangleEnumStripePainter
import com.meistercharts.canvas.BindContentAreaSize2ContentViewport
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.translateToContentAreaOrigin
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.historyConfiguration
import it.neckar.geometry.Direction
import com.meistercharts.model.Insets
import it.neckar.open.observable.ObservableDouble

/**
 * A simple hello world demo
 */
class RectangleEnumStripePainterDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("e4061245-220b-47fa-9eb8-2a56653d71d1")
  override val name: String = "Rectangle Enum Bar Stripe Painter"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val category: DemoCategory = DemoCategory.Painters

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val margin = Insets(20.0, 20.0, 20.0, 70.0)

        zoomAndTranslationDefaults {
          FittingWithMargin(margin)
        }
        contentAreaSizingStrategy = BindContentAreaSize2ContentViewport()

        val historyConfiguration = historyConfiguration {
          enumDataSeries(DataSeriesId(0), "Enum with 5 options", HistoryEnum.createSimple("Simple", listOf("zero", "one", "two", "three", "four", "five", "six")))
        }

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addLayer(ContentAreaLayer())

          val enumStripePainter = RectangleEnumStripePainter().also {
            it.configuration.aggregationMode = EnumAggregationMode.ByOrdinal
          }
          val booleanStripePainter = RectangleEnumStripePainter().also {
          }

          val heightProperty = ObservableDouble(20.0)

          configurableDouble("Enum Bar Height", heightProperty) {
            max = 150.0
          }

          configurable("Aggregation Type", enumStripePainter.configuration::aggregationMode)

          val painterLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val dataSeriesIndex = EnumDataSeriesIndex.zero

            override fun layout(paintingContext: LayerPaintingContext) {
              super.layout(paintingContext)
              layoutEnumValues(paintingContext)
              layoutBooleanValues(paintingContext)
            }

            private fun layoutEnumValues(paintingContext: LayerPaintingContext) {
              val enumOrdinalMostTime = HistoryEnumOrdinal(4)

              enumStripePainter.layoutBegin(paintingContext, heightProperty.value, EnumDataSeriesIndex.zero, historyConfiguration)
              enumStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 0.0, 100.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet(0b101), enumOrdinalMostTime)
              enumStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 100.0, 180.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet(0b111111), enumOrdinalMostTime)
              enumStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 180.0, 250.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.second, enumOrdinalMostTime)
              enumStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 250.0, 320.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.third, enumOrdinalMostTime)
              enumStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 320.0, 350.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.NoValue, enumOrdinalMostTime)
              enumStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 350.0, 450.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.first, enumOrdinalMostTime)
              enumStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 450.0, 460.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet(0b11), enumOrdinalMostTime)
              enumStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 460.0, 465.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet(0b101), enumOrdinalMostTime)
              enumStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 465.0, 500.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.third, enumOrdinalMostTime)
              enumStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 500.0, 550.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.Pending, enumOrdinalMostTime)
              enumStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 550.0, 620.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.third, enumOrdinalMostTime)
              enumStripePainter.layoutFinish(paintingContext, dataSeriesIndex)
            }

            private fun layoutBooleanValues(paintingContext: LayerPaintingContext) {
              val enumOrdinalMostTime = HistoryEnumOrdinal(1)

              booleanStripePainter.layoutBegin(paintingContext, heightProperty.value, EnumDataSeriesIndex.zero, historyConfiguration)
              booleanStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 0.0, 100.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.first, enumOrdinalMostTime)
              booleanStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 100.0, 150.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.first, enumOrdinalMostTime)
              booleanStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 150.0, 250.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.second, enumOrdinalMostTime)
              booleanStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 250.0, 300.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.first, enumOrdinalMostTime)
              booleanStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 300.0, 350.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.NoValue, enumOrdinalMostTime)
              booleanStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 350.0, 450.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.second, enumOrdinalMostTime)
              booleanStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 450.0, 500.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.second, enumOrdinalMostTime)
              booleanStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 500.0, 510.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.NoValue, enumOrdinalMostTime)
              booleanStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 510.0, 515.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.first, enumOrdinalMostTime)
              booleanStripePainter.layoutValueChange(paintingContext, dataSeriesIndex, 515.0, 600.0, 5000.0, 6000.0, Double.NaN, HistoryEnumSet.second, enumOrdinalMostTime)
              booleanStripePainter.layoutFinish(paintingContext, dataSeriesIndex)
            }


            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartCalculator = paintingContext.chartCalculator

              gc.fillText("Rect Enum Bar Stripe Painter below...", 0.0, 0.0, Direction.TopLeft)
              gc.translateToContentAreaOrigin(chartCalculator)

              gc.paintMark()
              enumStripePainter.paint(paintingContext, dataSeriesIndex)

              gc.translate(0.0, heightProperty.value + 10.0)
              booleanStripePainter.paint(paintingContext, dataSeriesIndex)
            }


          }
          layers.addLayer(painterLayer)
        }
      }
    }
  }
}
