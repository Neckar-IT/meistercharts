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

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.barchart.StackedBarPaintable
import com.meistercharts.algorithms.layers.barchart.StackedBarWithLabelPaintable
import com.meistercharts.algorithms.layers.circular.CircularChartPaintable
import com.meistercharts.algorithms.layers.compass.GaugePaintable
import com.meistercharts.algorithms.layers.legend.StackedPaintablesPaintable
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.algorithms.tooltip.balloon.BalloonTooltipPaintable
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.Button
import com.meistercharts.canvas.paintable.ButtonState
import com.meistercharts.canvas.paintable.CirclePaintable
import com.meistercharts.canvas.paintable.CombinedPaintable
import com.meistercharts.canvas.paintable.DebugPaintable
import com.meistercharts.canvas.paintable.LabelPaintable
import com.meistercharts.canvas.paintable.ObjectFit
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.canvas.paintable.RectanglePaintable
import com.meistercharts.canvas.paintable.SymbolAndImagePaintable
import com.meistercharts.canvas.paintable.SymbolAndTextKeyPaintable
import com.meistercharts.canvas.paintable.TankPaintable
import com.meistercharts.canvas.paintable.TransparentPaintable
import com.meistercharts.canvas.paintable.toButtonPainter
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.slider.Slider
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.section
import com.meistercharts.design.neckarit.NeckarItFlowPaintable
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.painter.CirclePointPainter
import com.meistercharts.range.ValueRange
import com.meistercharts.resources.Icons
import com.meistercharts.resources.LocalResourcePaintable
import com.meistercharts.resources.svg.SvgPaintableProviders
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.http.Url
import it.neckar.open.i18n.TextKey
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.asDoubleProvider

/**
 *
 */
class PaintableDemoDescriptor : MeisterchartsDemoDescriptor<() -> Paintable> {
  override val uuid: Uuid = Uuid.parse("65fa2d41-35f9-49fc-be98-51697b3bd1ae")
  override val name: String = "Paintable"
  override val category: DemoCategory = DemoCategory.Paintables
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val description: String = "Shows the different paint methods of a paintable, including paintInBoundingBox"

  override val predefinedConfigurations: List<PredefinedConfiguration<() -> Paintable>> = createPaintableConfigurations()

  override fun prepareDemo(configuration: PredefinedConfiguration<() -> Paintable>?): MeisterchartsDemo {
    require(configuration != null) { "configuration required" }

    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val paintable = configuration.payload()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var x: Double = 0.0
            var y: Double = 0.0

            var boundingBoxFactorWidth: Double = 1.4
            var boundingBoxFactorHeight: Double = 1.7

            val boundingBoxFactorWidthFormatted: String
              get() {
                return decimalFormat2digits.format(boundingBoxFactorWidth)
              }

            val boundingBoxFactorHeightFormatted: String
              get() {
                return decimalFormat2digits.format(boundingBoxFactorHeight)
              }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              //"Normal" paint method in center
              gc.saved {
                gc.translateToCenter()
                gc.paintMark(color = Color.silver())

                gc.saved {
                  paintable.paint(paintingContext, x, y)
                }

                gc.translate(x, y)
                gc.paintMark(color = Color.lightgray())

                val boundingBox = paintable.boundingBox(paintingContext)
                gc.stroke(Color.lightgray)
                gc.fill(Color.black)
                gc.strokeRect(boundingBox)
                gc.font(FontDescriptorFragment.XS)
                gc.fillText("paint", boundingBox.topLeft(), Direction.BottomLeft)
              }

              val paintableBoundingBox = paintable.boundingBox(paintingContext)

              //Paint in bounding box - size of paintable
              gc.saved {
                gc.translate(10.0, 20.0)
                gc.paintMark(color = Color.silver())

                gc.saved {
                  paintable.paintInBoundingBox(paintingContext, Coordinates.origin, Direction.TopLeft, boundingBoxSize = paintableBoundingBox.size, objectFit = ObjectFit.ContainNoGrow)
                }

                val boundingBox = Rectangle(Coordinates.origin, paintableBoundingBox.size)
                gc.stroke(Color.red)
                gc.fill(Color.red)
                gc.strokeRect(boundingBox)
                gc.font(FontDescriptorFragment.XS)
                gc.fillText("paintInBoundingBox(paintableSize)", boundingBox.topLeft(), Direction.BottomLeft)
              }

              //Paint in bounding box, grow
              gc.saved {
                gc.translate(10.0, gc.height - 20.0)
                gc.paintMark(color = Color.silver())

                val boundingBox = Rectangle.bottomLeft(paintableBoundingBox.size.times(boundingBoxFactorWidth, boundingBoxFactorHeight))

                gc.saved {
                  paintable.paintInBoundingBox(paintingContext, Coordinates.origin, Direction.BottomLeft, boundingBox.size, ObjectFit.ContainNoGrow)
                }

                gc.stroke(Color.red)
                gc.fill(Color.red)
                gc.strokeRect(boundingBox)
                gc.font(FontDescriptorFragment.XS)
                gc.fillText("ContainNoGrow (paintableSize * ($boundingBoxFactorWidthFormatted, $boundingBoxFactorHeightFormatted)", boundingBox.topLeft(), Direction.BottomLeft)
              }

              //In Bounding box fill
              gc.saved {
                gc.translate(gc.width - 10.0, 20.0)
                gc.paintMark(color = Color.silver())
                val boundingBox = Rectangle.topRight(paintableBoundingBox.size.times(boundingBoxFactorWidth, boundingBoxFactorHeight))

                gc.saved {
                  paintable.paintInBoundingBox(paintingContext, Coordinates.origin, Direction.TopRight, boundingBox.size, ObjectFit.Fill)
                }

                gc.stroke(Color.red)
                gc.fill(Color.red)
                gc.strokeRect(boundingBox)
                gc.font(FontDescriptorFragment.XS)
                gc.fillText("Fill (paintableSize * ($boundingBoxFactorHeightFormatted, $boundingBoxFactorHeightFormatted)", boundingBox.topLeft(), Direction.BottomLeft)
              }

              //Contain
              gc.saved {
                gc.translate(gc.width - 10.0, gc.height - 20.0)
                gc.paintMark(color = Color.silver())
                val boundingBox = Rectangle.bottomRight(paintableBoundingBox.size.times(boundingBoxFactorWidth, boundingBoxFactorHeight))

                gc.saved {
                  paintable.paintInBoundingBox(paintingContext, Coordinates.origin, Direction.BottomRight, boundingBox.size, ObjectFit.Contain)
                }

                gc.stroke(Color.red)
                gc.fill(Color.red)
                gc.strokeRect(boundingBox)
                gc.font(FontDescriptorFragment.XS)
                gc.fillText("Contain (paintableSize * ($boundingBoxFactorHeightFormatted, $boundingBoxFactorWidthFormatted)", boundingBox.topLeft(), Direction.BottomLeft)
              }
            }
          }
          layers.addLayer(layer)

          section("Paint (middle)")
          configurable("X", layer::x) {
            min = -200.0
            max = 200.0
          }
          configurable("Y", layer::y) {
            min = -200.0
            max = 200.0
          }

          section("Bounding box")
          configurable("Width", layer::boundingBoxFactorWidth) {
            max = 5.0
          }
          configurable("Height", layer::boundingBoxFactorHeight) {
            max = 5.0
          }

          if (paintable is DebugPaintable) {
            section("Debug-Paintable")
            configurable("Width", paintable::width) {
              max = 500.0
            }
            configurable("Height", paintable::height) {
              max = 500.0
            }

            configurable("Alignment Point X", paintable::alignmentPointX) {
              min = -100.0
              max = 100.0
            }
            configurable("Alignment Point Y", paintable::alignmentPointY) {
              min = -100.0
              max = 100.0
            }
          }
        }
      }
    }
  }

  companion object {
    fun createPaintableConfigurations(): List<PredefinedConfiguration<() -> Paintable>> {
      return listOf(
        PredefinedConfiguration(Uuid.parse("f90bc5d4-b939-4568-85cb-01b2beef95ae"), { DebugPaintable() }, "Debug"),
        PredefinedConfiguration(Uuid.parse("242bec3f-d595-442f-803f-a9679b2fe0ac"), { LabelPaintable({ _, _ -> "Hello World!" }) }, "Label"),

        //PredefinedConfiguration(uuidFrom("0ee78961-a333-48f6-b2db-97765bbcece7"), {
        //  PathPaintableProvider(path = SVGPathParser.from(ElektroMeisterResources.SwitchSvg).parse(), pathSize = Size.PX_24, Color.orange).get(Size.PX_24)
        //}, "Elektromeister: Switch"),
        //
        //PredefinedConfiguration(uuidFrom("f6a4a651-a1e9-4af6-9ec9-71b5102e9946"), {
        //  PathPaintableProvider(path = SVGPathParser.from(ElektroMeisterResources.LightsSvg).parse(), pathSize = Size.PX_24, Color.orange).get(Size.PX_24)
        //}, "Elektromeister: Lights"),
        //
        PredefinedConfiguration(Uuid.parse("efd233c3-db8e-48d1-8d49-6f84cb8d2d37"), { BalloonTooltipPaintable(RectanglePaintable(Size.PX_60, Color.orange)) }, "Balloon Tooltip"),
        PredefinedConfiguration(Uuid.parse("9b9a70cc-21f1-48a8-b399-2a0f4dc344bb"), {
          StackedPaintablesPaintable(
            SizedProvider.forValues(
              RectanglePaintable(10.0, 10.0, Color.red),
              RectanglePaintable(30.0, 20.0, Color.blue),
              RectanglePaintable(25.0, 30.0, Color.green),
            )
          )
        }, "VerticalLegendPaintable"),
        PredefinedConfiguration(Uuid.parse("d10faf63-8cdc-4d2a-a53f-18a4f0b93925"), { UrlPaintable.naturalSize(Url.relative("nit-logo-n-neg_360x360_facebook_insta.png")) }, "Url - natural"),
        PredefinedConfiguration(Uuid.parse("8859b4ed-9e8c-4db9-852f-e983dc8f5047"), { UrlPaintable.fixedSize(Url.relative("nit-logo-n-neg_360x360_facebook_insta.png"), Size.PX_120) }, "Url - fixed"),

        PredefinedConfiguration(Uuid.parse("e75b1c19-a193-4a6a-bb51-7bbafcc4cd64"), {
          SvgPaintableProviders.mapMarker.get(Size.PX_120, Color.silver, alignmentPoint = Coordinates(-60.0, -120.0))
        }, "SVG MapMarker"),
        PredefinedConfiguration(Uuid.parse("f84a449d-1ea4-48a1-ac8a-563dce03c499"), { SvgPaintableProviders.neckarItQr.get(Size.PX_120, Color.orange) }, "SVG QR"),
        PredefinedConfiguration(Uuid.parse("ad8817f9-c0c7-47ba-afcb-958720eacd9b"), {
          val mapMarker = SvgPaintableProviders.mapMarker.get(Size.PX_120, Color.silver, alignmentPoint = Coordinates(-60.0, -120.0))
          val warning = SvgPaintableProviders.warning.get(mapMarker.size.times(0.43), Color.red)

          CombinedPaintable(mapMarker, warning, Distance(-60.0, -mapMarker.size.width * 0.2 - 120.0))
        }, "Combined"),
        PredefinedConfiguration(Uuid.parse("ac8d2f96-c717-4135-a083-a30b225d0c19"), { CirclePaintable(Color.orangered, 72.0) }, "Circle"),
        PredefinedConfiguration(Uuid.parse("ef4406fb-5b85-4493-a4b8-5b771275588e"), {
          CirclePointPainter(true, true).also {
            it.fill = Color.green
            it.lineWidth = 24.0
            it.pointSize = 40.0
          }
        }, "CirclePoint"),
        PredefinedConfiguration(Uuid.parse("9b1dfad5-10fa-4579-bef1-f2a1fb984f24"), { LocalResourcePaintable(Url.relative("cable.png")) }, "LocalResource"),
        PredefinedConfiguration(Uuid.parse("f5c9a325-f28f-48f8-a2ec-db7b651ff41f"), { NeckarItFlowPaintable(Size.PX_120) }, "NECKAR.IT Flow"),
        PredefinedConfiguration(Uuid.parse("b98c4cce-987a-41a1-88e7-fe195f366502"), { Button({ _: ButtonState -> SvgPaintableProviders.ok.get(Size.PX_30, Color.red) }.toButtonPainter(), 43.0, 20.0) }, "Button"),
        PredefinedConfiguration(Uuid.parse("ad9a1666-8e20-4d3a-bab0-da96e1c9074a"), { CircularChartPaintable(DoublesProvider.forDoubles(0.1, 0.2, 0.3, 0.4)) }, "CircularChart"),
        PredefinedConfiguration(Uuid.parse("2f053c40-7ca0-4765-8c91-ee46e5090d1d"), { GaugePaintable({ ValueRange.percentage }, { 0.75 }, Size.PX_120) }, "Gauge"),
        PredefinedConfiguration(Uuid.parse("00305236-51e2-4aeb-ad0a-ab0e8b45e0aa"), { com.meistercharts.charts.OverflowIndicatorPainter.topIndicatorTriangle(Color.darkgray, Color.white) }, "Top Indicator"),
        PredefinedConfiguration(Uuid.parse("b8f3dd24-57cd-412a-80de-2d77ca18d86b"), { com.meistercharts.charts.OverflowIndicatorPainter.bottomIndicatorTriangle(Color.darkgray, Color.white) }, "Bottom Indicator"),
        PredefinedConfiguration(Uuid.parse("1fa9dea9-5135-458b-8dfd-32542ec68750"), { com.meistercharts.charts.OverflowIndicatorPainter.leftIndicatorTriangle(Color.darkgray, Color.white) }, "Left Indicator"),
        PredefinedConfiguration(Uuid.parse("131413bd-d6b0-4115-a4c1-be380f17c4a3"), { com.meistercharts.charts.OverflowIndicatorPainter.rightIndicatorTriangle(Color.darkgray, Color.white) }, "Right Indicator"),
        PredefinedConfiguration(Uuid.parse("3ce38130-932d-4692-9d23-0de365e097b8"), { com.meistercharts.charts.OverflowIndicatorPainter.rightIndicatorTriangle(Color.darkgray, Color.white, arrowHeadLength = Double.NaN) }, "Overflow Indicator with NaN"),
        PredefinedConfiguration(Uuid.parse("af0c6ef3-c76a-4a64-ba9a-6c42b52ef500"), { RectanglePaintable(120.0, 84.0, Color.green) }, "Rectangle"),
        PredefinedConfiguration(Uuid.parse("93dfe92e-c9c7-49db-ab4d-8b58823d4e68"), { StackedBarPaintable(width = 15.0, height = 120.0) }, "Stacked Bar"),
        PredefinedConfiguration(Uuid.parse("e2abba47-dbd3-43b2-ac6a-14913c0619df"), {
          StackedBarPaintable(
            width = 15.0,
            height = 120.0
          ) {
            valuesProvider = DoublesProvider.Companion.forDoubles(-1.0, -3.0, 5.0, 7.0)
            valueRange = ValueRange.linear(-10.0, 20.0)
          }
        }, "Stacked Bar - negative"),
        PredefinedConfiguration(Uuid.parse("37eb2d3f-4bd3-49f2-a94a-ab728ad4ea64"), { StackedBarWithLabelPaintable(width = 15.0, height = 120.0) }, "Stacked Bar + Label"),
        PredefinedConfiguration(Uuid.parse("90f83e5f-7b8c-4028-9495-bee0fde004d8"), { SymbolAndImagePaintable(CirclePaintable(Color.blue, 15.0), Icons.error()) }, "Symbol + Image"),
        PredefinedConfiguration(Uuid.parse("11241ddd-3089-4022-a8ec-41671f0e5426"), { SymbolAndTextKeyPaintable(CirclePaintable(Color.blue, 15.0), TextKey.simple("Hello World")) }, "Symbol + Text"),
        PredefinedConfiguration(Uuid.parse("41e578e3-78b8-4323-bd25-fdf0795b6ce7"), { TransparentPaintable(Size.PX_120) }, "Transparent"),
        PredefinedConfiguration(Uuid.parse("9a271eb1-0aa6-435f-8cfc-092950dbe1b5"), { TankPaintable() }, "Tank"),
        PredefinedConfiguration(Uuid.parse("07bce84e-805e-461d-9f5d-c153fdd16a7f"), { LocalResourcePaintable(Url.relative("tank/sensor-ultrasonic.png"), Size(272.0, 320.0).times(0.5), Coordinates(-272.0 * 0.5 * 0.5, -286.0 * 0.5)) }, "tank/sensor-ultrasonic.png"),
        PredefinedConfiguration(Uuid.parse("b2c6ecd7-36b4-4f64-8d0c-b4771c8fcd61"), { LocalResourcePaintable(Url.relative("tank/sensor-vibration.png"), Size(273.0, 818.0).times(0.5), Coordinates(-273.0 * 0.5 * 0.5, -286.0 * 0.5)) }, "tank/sensor-vibration.png"),

        //No size!
        //Deliberately this module's own copy under a demos-owned path: the identical image also exists as
        //solar/panel-vertical.png in meistercharts-pv-planning, and both modules end up in the same fat jar.
        PredefinedConfiguration(Uuid.parse("2d15d959-8339-4dd2-bb05-47fa75a08f41"), { LocalResourcePaintable(Url.relative("solar-panel-vertical.png"), Size(1134.0, 1860.0).times(0.15)) }, "solar-panel-vertical.png"),
        PredefinedConfiguration(Uuid.parse("acca67c5-dc3b-4888-bc90-eca92411d9f3"), {

          var sliderPosition = 0.5

          Slider(
            configuration = Slider.Configuration(150.0.asDoubleProvider(), handlePosition = { sliderPosition }, handlePositionChanged = { sliderPosition = it }),
          )
        }, "Slider"),
      )
    }
  }
}
